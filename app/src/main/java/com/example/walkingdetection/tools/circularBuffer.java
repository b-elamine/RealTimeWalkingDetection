package com.example.walkingdetection.tools;

import android.util.Pair;

import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;

public class circularBuffer {
    private final float[] buffer;
    private int head;
    private int tail;
    private int size;
    private final int windowSize;
    private final int overlapSize;
    private gaitAnalysis processedData;
    private boolean isWalking;
    private int steps=0;
    private boolean processingInProgress;



    public circularBuffer(int windowSize, int overlapSize) {
        buffer = new float[windowSize+overlapSize];
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
        if (size == buffer.length && !processingInProgress) {
            processingInProgress = true;
            System.out.println(size);
            new Thread(this::processWindow).start();
        }
    }

    private void processWindow() {

        System.out.println("console debug thread 2nd started ");
        // Standard Deviation
        float sd = mathFuncs.calculateStandardDeviation(toArray());

        // Calculating AFC if walking

        //if (sd>0.8) {
            List<Float> window = new ArrayList<>(toArray().length);
            for (float f : toArray()){
                window.add(f);
            }
            List<Float> afc_window = mathFuncs.afc(window,window.size());
            //Creating an arraylist for index to be able to detect peaks
            List<Float> index = new ArrayList<>();
            for (int i=0; i< afc_window.size(); i++){
                index.add((float) i);
            }

            // Peaks Detection
            System.out.println("console debug afc size : "+" "+afc_window.size());
            System.out.println("console debug std : "+" "+ sd);
            Pair<List<Float>, List<Float>> peaks = mathFuncs.peaksDetect(afc_window, 0.1f, index);

                isWalking = true;
                steps=peaks.first.size();

                //calculate parameters


      //  } else {
        //     isWalking = false;
       // }



        // Update tail to create overlap with next window
        tail = (tail + windowSize - overlapSize) % buffer.length;
        size -= (windowSize - overlapSize);

        // save the output of the processing
        processedData = new gaitAnalysis(isWalking, steps, sd); // true and one replaced by the results
        //System.out.println(tail + " " + size + " " +head);

        //Processing accomplished set the flag to false
        processingInProgress = false;
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
