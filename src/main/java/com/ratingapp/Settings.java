package com.ratingapp;

public class Settings {
    private String ratingScale;
    private String ratingAverage;
    private int nrOfCriteria;
    public Settings(String ratingScale, String ratingAverage, int nrOfCriteria) {
        this.ratingScale = ratingScale;
        this.ratingAverage = ratingAverage;
        this.nrOfCriteria = nrOfCriteria;
    }
    public String getRatingScale() {
        return ratingScale;
    }
    public void setRatingScale(String ratingScale) {
        this.ratingScale = ratingScale;
    }
    public String getRatingAverage() {
        return ratingAverage;
    }
    public void setRatingAverage(String ratingAverage) {
        this.ratingAverage = ratingAverage;
    }
    public int getNrOfCriteria() {
        return nrOfCriteria;
    }
    public void setNrOfCriteria(int nrOfCriteria) {
        this.nrOfCriteria = nrOfCriteria;
    }
}