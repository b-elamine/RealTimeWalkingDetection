package com.example.walkingdetection.tools;

public class circularBuffer {
    private double[] buffer;
    private int head;
    private int tail;
    private int size;

    private circularBuffer(int capacity) {
        buffer = new double[capacity];
        head = 0;
        tail = 0;
        size = 0;
    }

    public void add(double value){
        buffer[head] = value;
        head = (head+1) % buffer.length;
        if (size < buffer.length) {
            size++;
        } else {
            tail = (tail + 1) % buffer.length;
        }
    }
    public double get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index out of bounds");
        }
        return buffer[(tail + index) % buffer.length];
    }

    public int size() {
        return size;
    }

}
