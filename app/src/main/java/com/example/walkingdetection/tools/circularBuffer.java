package com.example.walkingdetection.tools;

import static java.lang.Float.NaN;
import android.util.Pair;
import java.util.ArrayList;
import java.util.List;
import com.example.walkingdetection.tools.mathFuncs;

public class circularBuffer {
    private final float[] buffer;
    private int head;
    private int tail;
    private int size;
    private final int windowSize;
    private final int overlapSize;
    private gaitAnalysis processedData;

    public circularBuffer(int windowSize, int overlapSize) {
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

    private void processWindow() {

        // Standard Deviation
        float sd = mathFuncs.calculateStandardDeviation(toArray());

        // Calculating AFC
        List<Float> window = new ArrayList<Float>(toArray().length);
        for (float f : toArray()){
            window.add(f);
        }
        List<Float> afc_window = mathFuncs.afc(window,window.size());

        // Peaks Detection
        float i =0;
        List<Float> index = new ArrayList<>();
        for (float f : afc_window
             ) {
            index.add(i++);
        }
        Pair<List<Float>, List<Float>> peaks = mathFuncs.peaksDetect(afc_window, 1.2f, index);


        // Update tail to create overlap with next window
        tail = (tail + windowSize - overlapSize) % buffer.length;
        size -= (windowSize - overlapSize);
        // save the output of the processing
        processedData = new gaitAnalysis(true, tail, sd); // true and one replaced by the results
    }

    public gaitAnalysis getProcessedData() {
        if (processedData!=null){
            return processedData;
        }
        return null;
    }


    private float[] toArray() {
        float[] window = new float[windowSize];
        for (int i = 0; i < windowSize; i++) {
            window[i] = buffer[(tail + i) % buffer.length];
        }
        return window;
    }


}
