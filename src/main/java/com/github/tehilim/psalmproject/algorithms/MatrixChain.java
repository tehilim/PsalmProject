package com.github.tehilim.psalmproject.algorithms;

import java.util.*;

public final class MatrixChain {

    private final int[] dimensions;

    public static void main(String... args) {
        int[] w =
                { 1, 2, 3, 4 };
                // { 40, 12, 10, 11, 25 };
                // { 16, 35, 15, 11, 10, 11 };
                // { 17, 20, 13, 10, 12 };
                // { 999, 10, 2, 1000 };
        MatrixChain matrixChain = new MatrixChain(w);
        for (int i = 0; i < w.length; i++) System.out.println(i + ": " + matrixChain.dimensions[i]);
        List<Arc> arcs = matrixChain.oneSweep();
        for (int i = 0; i < arcs.size(); i++) System.out.println(arcs.get(i));
    }

    public MatrixChain(int... dimensions) {
        // O(n): find the index of the lowest dimension
        int minIndex = 0;
        for (int i = 1; i < dimensions.length; i++) {
            if (dimensions[i] <= 0) {
                throw new IllegalArgumentException("Matrices must have strictly positive dimensions");
            }
            if (dimensions[i] < dimensions[minIndex]) {
                minIndex = i;
            }
        }
        // O(n): rotate the array so the minimum is at index 0
        // Creating a new local array prevents accidental changes to the array from outside this class
        this.dimensions = new int[dimensions.length];
        // Move the minimum to the start
        System.arraycopy(dimensions, minIndex, this.dimensions, 0, dimensions.length - minIndex);
        // Put the other dimensions in place
        System.arraycopy(dimensions, 0, this.dimensions, dimensions.length - minIndex, minIndex);
    }

    public List<Arc> oneSweep() {
        // This stack contains indexes for the dimensions array W
        List<Arc> arcs = new ArrayList<>();
        Stack<Integer> stack = new Stack<>();
        // Repeat until the n-th (i.e. last) vertex has been pushed onto the stack
        int c = 0;
        while (c < dimensions.length || stack.size() > 3) {
            if (stack.size() >= 2 && (c >= dimensions.length || dimensions[stack.peek()] > dimensions[c])) {
                // 2 or more vertices on the stack && W(t) > W(c)
                stack.pop();
                // Note that V(t-1) is now the top of the stack since we popped V(t) off the stack

                // Add the arc V(t-1)=>V(c) to the list of potential h-arcs,
                // or V(t-1)=>V(0) once c reaches the end of the list of dimensions.
                arcs.add(new Arc(stack.peek(), c % dimensions.length));
            } else { // stack.size() < 2 || dimensions[stack.peek()] <= dimensions[c]
                stack.push(c++);
            }
        }
        return arcs;
    }

    private static final class Arc {
        int fromIndex, toIndex;
        private Arc(int fromIndex, int toIndex) {
            this.fromIndex = fromIndex;
            this.toIndex = toIndex;
        }
        public String toString() {
            return "V(" + fromIndex + ")=>V(" + toIndex + ")";
        }
    }
}
