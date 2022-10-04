package com.github.tehilim.psalmproject.udp;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.nio.charset.StandardCharsets.ISO_8859_1;

public final class UDPFileServer implements Runnable {
    public static byte HT = 9;
    public static byte LF = 10;
    public static byte CR = 13;
    public static byte SP = 32;

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newCachedThreadPool();
        boolean running = true;
        try (DatagramSocket serverSocket = new DatagramSocket(8080)) {
            while (running) {
                final DatagramPacket request = new DatagramPacket(new byte[576], 576);
                try {
                    serverSocket.receive(request);
                    UDPFileServer udpFileServer = new UDPFileServer(serverSocket, request);
                    executorService.submit(udpFileServer);
                    running = !udpFileServer.exitRequested();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    private final DatagramSocket serverSocket;

    /**
     * It is probably best to have a UDP payload of at most 512 bytes.
     */
    private final DatagramPacket request;

    private final List<String> lines = new ArrayList<>();

    private final Map<String, String> requestParts = new HashMap<>();

    private final Map<String, String> headers = new LinkedHashMap<>();

    private final int payloadIndex;

    private boolean exitRequested = false;

    public UDPFileServer(DatagramSocket serverSocket, DatagramPacket request) {
        this.serverSocket = serverSocket;
        this.request = request;
        this.payloadIndex = parseRequest();
    }

    @Override
    public void run() {
        try {
            // Example:
            //
            // GET /file HTTP/1.1
            // Host: www.myserver.com
            // Range: 0-511

            // Validate the request
            if (!"GET".equals(requestParts.get("method"))) {
                // TODO: Send HTTP/1.1 501 Not Implemented
                System.err.println("HTTP/1.1 501 Not Implemented");
                return;
            }
            if (!"HTTP/1.1".equals(requestParts.get("version"))) {
                // TODO: Send HTTP/1.1 505 Version Not Supported
                System.err.println("HTTP/1.1 505 Version Not Supported");
                return;
            }
            String uri = requestParts.get("uri");
            if (uri == null || uri.length() == 0 || "/".equals(uri)) {
                uri = "/index.html";
            }
            String host = headers.get("host");
            Path hostPath = Paths.get(host);
            // The (virtual) host must be an existing directory on the server
            if (!Files.exists(hostPath) || !Files.isDirectory(hostPath)) {
                // TODO: Send HTTP/1.1 404 Not Found
                System.err.println("HTTP/1.1 404 Not Found (" + hostPath + ")");
                return;
            }
            // Always interpret the uri relative to the host (as direct file or file in subdirectory)
            if (uri.charAt(0) != '/') {
                uri = '/' + uri;
            }
            Path fullPath = Paths.get(host, uri);
            // Can only serve files (not directories) that actually exist
            if (!Files.exists(fullPath) || Files.isDirectory(fullPath)) {
                // TODO: Send HTTP/1.1 404 Not Found
                System.err.println("HTTP/1.1 404 Not Found (" + fullPath + ")");
                return;
            }

            // Who (request.getAddress() & request.getPort()) requested which
            // part of which file (request.getData()) ?
            InetAddress inetAddress = request.getAddress();
            int port = request.getPort();
            String range = headers.get("range");
            long longFileSize = Files.size(fullPath);
            if (longFileSize > Integer.MAX_VALUE) {
                // TODO: Send HTTP/1.1 500 Internal Server Error
                System.err.println("HTTP/1.1 500 Internal Server Error (" + longFileSize + " > Integer.MAX_VALUE)");
                return;
            }
            int fileSize = (int) longFileSize;
            int rangeStart = 0, rangeEnd = fileSize - 1;
            if (range != null) {
                range = range.trim();
                int dashIndex = range.indexOf('-');
                if (dashIndex <= 0) {
                    // TODO: Send HTTP/1.1 400 Bad Request
                    System.err.println("HTTP/1.1 400 Bad Request (no dash)");
                    return;
                }
                try {
                    rangeStart = Integer.parseUnsignedInt(range, 0, dashIndex, 10);
                } catch (Exception e) {
                    // TODO: Send HTTP/1.1 400 Bad Request
                    System.err.println("HTTP/1.1 400 Bad Request (range start)");
                    e.printStackTrace();
                    return;
                }
                try {
                    int i = Integer.parseUnsignedInt(range, dashIndex + 1, range.length(), 10);
                    // Only update rangeEnd if the requested last byte is before the end of the file.
                    if (i < rangeEnd) {
                        rangeEnd = i;
                    }
                } catch (Exception e) {
                    // TODO: Send HTTP/1.1 400 Bad Request
                    System.err.println("HTTP/1.1 400 Bad Request (range end)");
                    e.printStackTrace();
                    return;
                }
            }
            if (rangeEnd < rangeStart || rangeStart >= fileSize) {
                // TODO: Send HTTP/1.1 416 Range Not Satisfiable
                // Content-Range: bytes */fileSize
                System.err.println("HTTP/1.1 416 Range Not Satisfiable");
                return;
            }

            File file = fullPath.toFile();
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
            int rangeLength = rangeEnd - rangeStart + 1;
            byte[] payload = new byte[rangeLength];
            randomAccessFile.seek(rangeStart);
            int bytesRead = randomAccessFile.read(payload, 0, rangeLength);
            // Note: the number of bytes read may be lower than the number requested

            // Send HTTP/1.1 206 Partial Content
            byte[] headers =
                ( "HTTP/1.1 206 Partial Content\r\n"
                + "Content-Range: bytes " + rangeStart + "-" + (rangeStart + bytesRead - 1) + "/" + fileSize + "\r\n"
                + "Content-Length: " + bytesRead + "\r\n"
                + "Content-Type: application/octet-stream\r\n"
                + "\r\n"
                ).getBytes(ISO_8859_1);
            byte[] response = new byte[headers.length + payload.length];
            System.arraycopy(headers, 0, response, 0, headers.length);
            System.arraycopy(payload, 0, response, headers.length, payload.length);
            DatagramPacket responsePacket = new DatagramPacket(response, response.length, inetAddress, port);
            if (serverSocket != null) {
                serverSocket.send(responsePacket);
            }
        } catch (Exception e) {
            // TODO: TODO: Send HTTP/1.1 500 Internal Server Error
            System.err.println("HTTP/1.1 500 Internal Server Error (" + e.getMessage() + ")");
            e.printStackTrace();
            return;
        }
    }

    /**
     * Parses the HTTP 1.1 request provided in the byte array and
     * identifies the individual header lines and the start index of the payload.
     *
     * @return the start index of the payload
     */
    private int parseRequest() {
        // split headers and payload data by searching for 0D 0A 0D 0A
        // everything before it are 0D0A-separated ASCII headers, everything after it is payload
        byte[] bytes = request.getData();
        int index = 0;
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < bytes.length - 4; i++) {
            if (bytes[i] == CR && bytes[i+1] == LF) {
                if (bytes[i+2] == CR && bytes[i+3] == LF) {
                    String line = new String(bytes, index, i + 2 - index, ISO_8859_1);
                    updateHeaders(line);
                    // Last line
                    lines.add(line);
                    // Start of payload
                    index = i + 4;
                    break;
                } else {
                    if (bytes[i+2] != SP && bytes[i+2] != HT) {
                        String line = new String(bytes, index, i + 2 - index, ISO_8859_1);
                        updateHeaders(line);
                        lines.add(line);
                        // Skip past the LF as well
                        i++;
                        // Start of new line
                        index = i + 1;
                    }
                }
            }
        }
        return index;
    }

    private void updateHeaders(String line) {
        // The first line is not a header (it is the request)
        if (lines.isEmpty()) {
            int firstSpaceIndex = line.indexOf(' ');
            int lastSpaceIndex  = line.lastIndexOf(' ');
            if (0 < firstSpaceIndex && firstSpaceIndex < lastSpaceIndex) {
                String method = line.substring(0, firstSpaceIndex);
                String uri = line.substring(firstSpaceIndex + 1, lastSpaceIndex);
                String httpVersion = line.substring(lastSpaceIndex + 1);
                requestParts.put("method", method.trim());
                requestParts.put("uri", uri.trim());
                requestParts.put("version", httpVersion.trim());
            }
        } else {
            // After the first line, there are only headers
            int colonIndex = line.indexOf(':');
            // Only parse the header if the key is at least one character long
            if (colonIndex > 0) {
                String key = line.substring(0, colonIndex);
                String value = line.substring(colonIndex + 1);
                if (headers.containsKey(key)) {
                    // If the header is already present, consider this an
                    // extra value for that header (comma separated)
                    value = headers.get(key) + " , " + value;
                }
                // Don't include leading or trailing whitespace in the value
                headers.put(key.toLowerCase(), value.trim());
            }
        }
    }

    public boolean exitRequested() {
        return exitRequested;
    }
}
