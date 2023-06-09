package com.example.walkingdetection.tools;

public class gaitAnalysis {
    private final boolean isWalking;
    private final int stepCount;
    private final float standardDeviation;

    public gaitAnalysis(Boolean isWalking, int stepCount, float standardDeviation){
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
