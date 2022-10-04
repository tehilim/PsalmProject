package com.github.tehilim.psalmproject.udp;

import org.junit.jupiter.api.Test;

import java.net.DatagramPacket;

import static java.nio.charset.StandardCharsets.ISO_8859_1;

class UDPFileServerTest {

    @Test
    void run() {
        byte[] bytes =
            ( "GET / HTTP/1.1\r\n"
            + "Host: www.httpoverudp.nl\r\n"
            + "Range:\r\n"
            + "       0-511\r\n"
            + "\r\nPAYLOAD"
            ).getBytes(ISO_8859_1);
        DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
        UDPFileServer server = new UDPFileServer(null, packet);
        server.run();
    }
}
