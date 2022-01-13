package com.github.tehilim.psalmproject.neuralnetwork;

import java.util.Arrays;
import java.util.Comparator;

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
        Network[] candidates = new Network[20];
        byte[] errorCount = new byte[20];
        for (int i = 0; i < candidates.length; i++) {
            candidates[i] = new Network(i,2, 2, 1);
        }
        while (true) {
            // Check the candidates for errors
            for (Network network : candidates) {
                errorCount[network.getId()] = 0;
                if (network.getOutputsFor(0.0f, 0.0f)[0] <= 0.67f) errorCount[network.getId()]++;
                if (network.getOutputsFor(1.0f, 0.0f)[0] >= 0.33f) errorCount[network.getId()]++;
                if (network.getOutputsFor(0.0f, 1.0f)[0] >= 0.33f) errorCount[network.getId()]++;
                if (network.getOutputsFor(1.0f, 1.0f)[0] <= 0.67f) errorCount[network.getId()]++;
            }
            // Sort the candidates by number of errors
            Arrays.sort(candidates, Comparator.comparingInt(network -> errorCount[network.getId()]));
            // Relabel the candidates
            for (int i = 0; i < candidates.length; i++) {
                candidates[i].setId(i);
            }
            if (errorCount[0] == 0) {
                break;
            } else {
                candidates[candidates.length - 1] = new Network(candidates.length - 1, 2,2,1);
                // Replace the worst network by a 'child' of the two best networks
                int lastIndex = candidates.length - 2;
                int firstIndex = 1;
                while (lastIndex > firstIndex) {
                    Network newNetwork = new Network(lastIndex, candidates[firstIndex - 1]);
                    newNetwork.crossover(candidates[firstIndex]);
                    newNetwork.mutate(1);
                    candidates[lastIndex] = newNetwork;
                    lastIndex--;
                    firstIndex++;
                }
            }
        }
        System.out.println("=================================================");
        System.out.println("0 XOR 0 = " + candidates[0].getOutputsFor(0.0f, 0.0f)[0]);
        System.out.println("1 XOR 0 = " + candidates[0].getOutputsFor(1.0f, 0.0f)[0]);
        System.out.println("0 XOR 1 = " + candidates[0].getOutputsFor(0.0f, 1.0f)[0]);
        System.out.println("1 XOR 1 = " + candidates[0].getOutputsFor(1.0f, 1.0f)[0]);
    }
}
