package com.github.tehilim.psalmproject.neuralnetwork;

public class LearnXOR implements Runnable {
    private final String[] args;

    public LearnXOR(String... args) {
        this.args = args;
    }

    public static void main(String... args) {
        new LearnXOR(args).run();
    }

    @Override
    public void run() {
        Network network = new Network(2, 2, 1);
        while (network.getOutputsFor(0.0f, 0.0f)[0] <= 0.5f
            || network.getOutputsFor(1.0f, 0.0f)[0] >= 0.5f
            || network.getOutputsFor(0.0f, 1.0f)[0] >= 0.5f
            || network.getOutputsFor(1.0f, 1.0f)[0] <= 0.5f
            ) {
            network.mutate(1);
        }
        System.out.println("=================================================");
        System.out.println("0 XOR 0 = " + network.getOutputsFor(0.0f, 0.0f)[0]);
        System.out.println("1 XOR 0 = " + network.getOutputsFor(1.0f, 0.0f)[0]);
        System.out.println("0 XOR 1 = " + network.getOutputsFor(0.0f, 1.0f)[0]);
        System.out.println("1 XOR 1 = " + network.getOutputsFor(1.0f, 1.0f)[0]);
    }
}
