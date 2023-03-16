package com.example.walkingdetection.tools;

public class gaitAnalysis {
    private boolean isWalking;
    private int stepCount;

    public gaitAnalysis(boolean isWalking, int stepCount){
        this.isWalking = isWalking;
        this.stepCount = stepCount;
    }

    public boolean isWalking(){
        return isWalking;
    }

    public int getStepCount(){
        return stepCount;
    }
}
