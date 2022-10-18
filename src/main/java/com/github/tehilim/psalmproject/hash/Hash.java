package com.github.tehilim.psalmproject.hash;

import java.util.List;

public class Hash implements Runnable {
    private final List<String> args;

    public static void main(String... args) {
        new Hash(List.of(args)).run();
    }

    private Hash(List<String> args) {
        this.args = args;
    }

    public void run() {

    }

    private int hash(byte[] data) {
        int hash = 0; // It's better to have a prime number here
        for (byte b : data) {
            long sum = hash + b; // this might be subtraction because even bytes are signed in Java
            if (sum > Integer.MAX_VALUE) {
                sum = sum & Integer.MAX_VALUE;
            }
            hash = (int) sum;
        }
        return hash;
    }
}
