package com.github.tehilim.psalmproject.udp;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.*;

import static java.nio.charset.StandardCharsets.ISO_8859_1;

class UDPFileServerTest {

    @Test
    void run() {
        try (DatagramSocket socket = new DatagramSocket(8081)) {
            byte[] bytes =
                    ( "GET / HTTP/1.1\r\n"
                    + "Host: www.httpoverudp.nl\r\n"
                    + "Range:\r\n"
                    + "       0-511\r\n"
                    + "\r\nPAYLOAD"
                    ).getBytes(ISO_8859_1);
            DatagramPacket request = new DatagramPacket(bytes, bytes.length, InetAddress.getLocalHost(), 8080);
            socket.send(request);
            byte[] buffer = new byte[600];
            DatagramPacket response = new DatagramPacket(buffer, buffer.length);
            socket.setSoTimeout(2);
            socket.receive(response);
            System.out.println(new String(response.getData(), 0, response.getLength(), ISO_8859_1));
        } catch (UnknownHostException ex) {
            ex.printStackTrace();
        } catch (SocketException ex) {
            ex.printStackTrace();
        } catch (SocketTimeoutException e) {
            System.out.println("Exit due to timeout.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
