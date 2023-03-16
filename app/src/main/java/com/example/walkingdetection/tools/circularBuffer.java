package com.example.walkingdetection.tools;

public class circularBuffer {
    private final float[] buffer;
    private int head;
    private int tail;
    private int size;
    private final int windowSize;
    private final int overlapSize;

    private circularBuffer(int windowSize, int overlapSize) {
        buffer = new float[windowSize];
        head = 0;
        tail = 0;
        size = 0;
        this.windowSize = windowSize;
        this.overlapSize = overlapSize;
    }

    public void add(float value) {
        buffer[head] = value;
        head = (head + 1) % buffer.length;
        if (size < buffer.length) {
            size++;
        }
        if (size == buffer.length) {
            new Thread(this::processWindow).start();
        }
    }

    private gaitAnalysis processWindow() {
        float[] window = toArray();
        // Do processing on window here
        // ...

        // Update tail to create overlap with next window
        tail = (tail + windowSize - overlapSize) % buffer.length;
        size -= (windowSize - overlapSize);
        return new gaitAnalysis(true, 1); //here we replace "true" and "1" by the results of the processing
    }

    private float[] toArray() {
        float[] window = new float[windowSize];
        for (int i = 0; i < windowSize; i++) {
            window[i] = buffer[(tail + i) % buffer.length];
        }
        return window;
    }



}
