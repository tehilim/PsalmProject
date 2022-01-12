package com.github.tehilim.psalmproject.neuralnetwork;

public class Network {
    private final Layer[] layers;

    /**
     * Creates a artificical neural network having layers with the specified number of nodes.
     *
     * @param nodesPerLayer for each layer (including the input layer and the output layer
     *                      the number of nodes per layer.
     */
    public Network(int... nodesPerLayer) {
        this.layers = new Layer[nodesPerLayer.length - 1];
        for (int i = 0; i < layers.length; i++) {
            layers[i] = new Layer(nodesPerLayer[i], nodesPerLayer[i + 1]);
        }
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
}
