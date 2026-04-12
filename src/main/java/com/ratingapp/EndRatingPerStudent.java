package com.ratingapp;

public class EndRatingPerStudent extends RatingPerStudent {
    private final int numberOfRatings;
    public EndRatingPerStudent(String courseName, String vorname, String nachname, String datestr, int criterion, char rating, boolean flag, int numberOfRatings) {
        super(courseName, vorname, nachname, datestr, criterion, rating, flag);
        this.numberOfRatings = numberOfRatings;
    }
    public int getNumberOfRatings() {
        return numberOfRatings;
    }
}
