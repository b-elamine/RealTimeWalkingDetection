package com.example.walkingdetection.tools;

import static java.lang.Float.NaN;

import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class mathFuncs {
    // Standard Deviation
    public static float calculateStandardDeviation(float[] data) {
        float sum = 0.0F;
        float mean = 0.0F;
        float variance = 0.0F;
        float standardDeviation = 0.0F;

        // Calculate sum of all elements in array
        for(float num : data) {
            sum += num;
        }

        // Calculate mean of array elements
        mean = sum/data.length;

        // Calculate variance of array elements
        for(float num : data) {
            variance += Math.pow(num - mean, 2);
        }

        variance = variance/data.length;

        // Calculate standard deviation of array elements
        standardDeviation = (float) Math.sqrt(variance);

        return standardDeviation;
    }

    // AFC function "Normalized unbiased"
    public static List<Float> afc(List<Float> sequence, float lags) {
        float sum = 0, sumEcarts = 0;
        float var;
        List<Float> ecarts = new ArrayList<>(); //Deviation (Every data instance minus the mean)
        List<Float> afc = new ArrayList<>(); //Autocorrelation output

        //Variance calculation
        //First we calculate the sum of our data set
        for (int i = 0; i < sequence.size(); i++) {
            sum += sequence.get(i);
        }

        //Deviation
        for (int i = 0; i < sequence.size(); i++) {
            ecarts.add((sequence.get(i) - (sum / sequence.size())));
        }

        //Sum ecarts contains now the standard deviation
        for (int i = 0; i < ecarts.size(); i++) {
            sumEcarts += Math.pow(ecarts.get(i), 2);
        }

        //We have all to calculate the variance..
        var = (float) sumEcarts / ecarts.size();

        //Autocorrelation function
        for (int i = 0; i < lags; i++) {
            sum = 0;
            if (i == 0) {
                afc.add((float) 1);
            } else {
                for (int j = 0; j < ecarts.size() - i; j++) {
                    sum += ecarts.get(j + i) * ecarts.get(j);
                }
                afc.add(sum / (sequence.size() - i) / var);
            }

        }

        return afc;
    }

    // Peaks Detection algorithm
    public static Pair<List<Float>, List<Float>> peaksDetect(List<Float> data, float delta, List<Float> index) {
        // Initializing needed variables
        List<Float> maxPeaks = new ArrayList<>(); // Maximum peaks values
        List<Float> minPeaks = new ArrayList<>(); // Minimum peaks values
        List<Float> positionMax = new ArrayList<>(); // Index of every max peak
        List<Float> positionMin = new ArrayList<>(); // Index of every min peak
        // Helpers for manipulation
        float min, max, minPos, maxPos, val;
        boolean lookingForMax = true;

        if (data.size() != index.size()) {

            //throwing an exception just by printing "MUST BE HANDLED LATER !"

            System.out.println("Data and index size must be the equal!" +
                    data.size() +
                    " =/= " +
                    index.size());

            System.exit(0);
        }
        if (delta <= 0) {

            //throwing an exception just by printing "MUST BE HANDLED LATER !"

            System.out.println("Delta have to be positive not null scalar number");
            System.exit(0);
        }

        min = Float.POSITIVE_INFINITY;
        max = Float.NEGATIVE_INFINITY;
        minPos = NaN;
        maxPos = NaN;

        for (int i = 0; i < data.size(); i++) {
            val = data.get(i);
            if (val > max) {
                max = val;
                maxPos = index.get(i);
            }

            if (val < min) {
                min = val;
                minPos = index.get(i);
            }
            if (lookingForMax) {
                if (val < max - delta) {
                    if (maxPos!=0){
                        maxPeaks.add(max);
                        positionMax.add(maxPos);
                    }
                    min = val;
                    minPos = index.get(i);
                    lookingForMax = false;
                }
            } else {
                if (val > min + delta) {
                    if (minPos!=0){
                        minPeaks.add(min);
                        positionMin.add(minPos);
                    }
                    max = val;
                    maxPos = index.get(i);
                    lookingForMax = true;
                }
            }
        }
        return new Pair<>(positionMax, maxPeaks);
    }

}
