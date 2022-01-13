package com.github.tehilim.psalmproject.neuralnetwork;

import java.util.Arrays;
import java.util.Random;

public class Layer {
    private float[][] weights;

    public Layer(int inputSize, int outputSize) {
        if (inputSize <= 0 || outputSize <= 0) {
            throw new IllegalArgumentException("at least 1 input and at least 1 output required");
        }
        initializeWeights(inputSize, outputSize);
    }

    public Layer(Layer otherLayer) {
        initializeWeights(otherLayer.weights[0].length - 1, otherLayer.weights.length);
        copyWeightsFrom(otherLayer);
    }

    private void initializeWeights(int inputSize, int outputSize) {
        // Let the sub-arrays be the columns of the weight matrix; +1 for the bias
        weights = new float[outputSize][inputSize + 1];

        Random random = new Random(System.currentTimeMillis());
        for (int i = 0; i < weights.length; i++) {
            for (int j = 0; j < weights[i].length; j++) {
                weights[i][j] = random.nextFloat() * (random.nextBoolean() ? 1 : -1);
            }
        }
    }

    public void crossover(Layer otherLayer) {
        if (otherLayer.weights.length != weights.length) {
            throw new IllegalArgumentException("layers must have equal size");
        }
        if (otherLayer.weights[0].length != weights[0].length) {
            throw new IllegalArgumentException("layers must have equal size");
        }
        Random random = new Random(System.currentTimeMillis());
        for (int i = 0; i < weights.length; i++) {
            for (int j = 0; j < weights[i].length; j++) {
                if (random.nextBoolean()) {
                    weights[i][j] = otherLayer.weights[i][j];
                }
            }
        }
    }

    public void copyWeightsFrom(Layer otherLayer) {
        if (otherLayer.weights.length != weights.length) {
            throw new IllegalArgumentException("layers must have equal size");
        }
        if (otherLayer.weights[0].length != weights[0].length) {
            throw new IllegalArgumentException("layers must have equal size");
        }
        for (int i = 0; i < weights.length; i++) {
            System.arraycopy(otherLayer.weights[i], 0, weights[i], 0, weights[i].length);
        }
    }

    public float[] getOutputsFor(float... inputs) {
        float[] firstColumn = weights[0];
        if (inputs.length != firstColumn.length - 1) {
            throw new IllegalArgumentException(firstColumn.length + " inputs required");
        }

        float[] outputs = new float[weights.length];
        for (int i = 0; i < weights.length; i++) {
            // Calculate cross product / matrix multiplication
            float[] column = weights[i];
            float crossProduct = 0.0f;
            for (int j = 0; j < inputs.length; j++) {
                crossProduct += column[j] * inputs[j];
            }
            // Add the bias
            crossProduct += column[inputs.length];
            // Linear between 0.0 and 1.0, flat otherwise
            if (crossProduct < 0.0f) {
                crossProduct = 0.0f;
            } else if (crossProduct > 1.0f) {
                crossProduct = 1.0f;
            }
            outputs[i] = crossProduct;
        }
        return outputs;
    }

    public void mutate(int numberOfWeights) {
        Random random = new Random(System.currentTimeMillis());
        long count = weights.length * (long) weights[0].length;
        if (numberOfWeights < count) {
            count = numberOfWeights;
        }
        while (count-- > 0) {
            int column = random.nextInt(weights.length);
            int row = random.nextInt(weights[0].length);
            weights[column][row] += random.nextFloat()  * (random.nextBoolean() ? 0.1 : -0.1);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Layer layer = (Layer) o;
        return Arrays.deepEquals(weights, layer.weights);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(weights);
    }
}
