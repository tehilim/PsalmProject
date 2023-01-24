package com.github.tehilim.psalmproject.algorithms;

import java.util.Random;

public class MergeSort {

    public static void main(String[] args) throws Exception {
        for (int k = 0; k < 100000; k++) {
            Random random = new Random();
            int size = 1 + Math.abs(random.nextInt(10));
            int[] data = new int[size];
            for (int i = 0; i < size; i++) {
                data[i] = random.nextInt(100);
            }
            for (int i = 0; i + 1 < data.length; i++) {
                System.out.print(data[i] + ",");
            }
            System.out.println(data[data.length - 1]);
            mergeSort(data);
            Exception e = null;
            for (int i = 0; i + 1 < data.length; i++) {
                if (i > 0 && data[i - 1] > data[i]) {
                    e = new IllegalStateException("Bug");
                }
                System.out.print(data[i] + ",");
            }
            System.out.println(data[data.length - 1]);
            if (e != null) {
                throw e;
            }
        }
    }

    /**
     * Mergesort variant which makes use of existing non-decreasing ranges in the data.
     * @param data the data to be sorted
     */
    public static void mergeSort(int[] data) {
        int start = 0, mid = 0, end = 0;
        while (start > 0 || end < data.length) {
            start = end < data.length ? end : 0;
            mid = findRange(data, start);
            end = findRange(data, mid);
            if (start < mid && mid < end && end <= data.length) {
                merge(data, start, mid, end);
            }
        }
    }

    private static int findRange(int[] data, int index) {
        while (index + 1 < data.length && data[index] <= data[index + 1]) {
            index++;
        }
        return index + 1;
    }

    private static void merge(int[] data, int start, int mid, int end) {
        System.out.println("merge(" + start + "," + mid + "," + end + ")");
        int leftPointer = start, rightPointer = mid;
        int[] merged = new int[end - start];
        int writePointer = 0;
        while (leftPointer < mid && rightPointer < end) {
            if (data[leftPointer] <= data[rightPointer]) {
                merged[writePointer++] = data[leftPointer++];
            } else {
                merged[writePointer++] = data[rightPointer++];
            }
        }
        if (leftPointer < mid) {
            int count = mid - leftPointer;
            System.arraycopy(data, leftPointer, data, end - count,count);
        }
        System.arraycopy(merged, 0, data, start, writePointer);
    }
}
