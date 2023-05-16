package com.example.walkingdetection.tools;

public class gaitAnalysis {
    private boolean isWalking;
    private int stepCount;
    private float standardDeviation;

    public gaitAnalysis(boolean isWalking, int stepCount, float standardDeviation){
        this.isWalking = isWalking;
        this.stepCount = stepCount;
        this.standardDeviation = standardDeviation;
    }

    public boolean isWalking(){
        return isWalking;
    }

    public int getStepCount(){
        return stepCount;
    }

    public float getStandardDeviation() {
        return standardDeviation;
    }


}
