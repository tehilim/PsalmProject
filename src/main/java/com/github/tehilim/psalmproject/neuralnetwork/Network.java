package com.github.tehilim.psalmproject.neuralnetwork;

import java.util.Arrays;

public final class Network {
    private int id;
    private final Layer[] layers;

    /**
     * Creates a artificical neural network having layers with the specified number of nodes.
     *
     * @param nodesPerLayer for each layer (including the input layer and the output layer
     *                      the number of nodes per layer.
     */
    public Network(int id, int... nodesPerLayer) {
        this.id = id;
        this.layers = new Layer[nodesPerLayer.length - 1];
        for (int i = 0; i < layers.length; i++) {
            layers[i] = new Layer(nodesPerLayer[i], nodesPerLayer[i + 1]);
        }
    }

    public Network(int id, Network source) {
        setId(id);
        this.layers = new Layer[source.layers.length];
        for (int i = 0; i < layers.length; i++) {
            layers[i] = new Layer(source.layers[i]);
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float[] getOutputsFor(float... data) {
        for (Layer layer : layers) {
            // Output of one layer is input for the next layer
            data = layer.getOutputsFor(data);
        }
        // Return the final result
        return data;
    }

    public void mutate(int maxWeightsPerLayer) {
        for (Layer layer : layers) {
            layer.mutate(maxWeightsPerLayer);
        }
    }

    public void crossover(Network otherNetwork) {
        if (layers.length != otherNetwork.layers.length) {
            throw new IllegalArgumentException("the networks do not have identical structure");
        }
        for (int i = 0; i < layers.length; i++) {
            layers[i].crossover(otherNetwork.layers[i]);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Network network = (Network) o;
        return Arrays.equals(layers, network.layers);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(layers);
    }
}
