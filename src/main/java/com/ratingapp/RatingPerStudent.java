package com.ratingapp;

public class RatingPerStudent {
    private String courseName;
    private String vorname;
    private String nachname;
    private String name;
    private String datestr;
    private int criterion;
    private char rating;
    private boolean flag;

    public RatingPerStudent(String courseName, String vorname, String nachname, String datestr, int criterion, char rating, boolean flag) {
        this.courseName = courseName;
        this.vorname = vorname;
        this.nachname = nachname;
        this.name = vorname + " " + nachname;
        this.datestr = datestr;
        this.criterion = criterion;
        this.rating = rating;
        this.flag = flag;
    }
    public String getCourseName() {
        return courseName;
    }
    public String getVorname() {
        return vorname;
    }
    public String getNachname() {
        return nachname;
    }
    public String getName() {
        return name;
    }
    public String getDate() {
        return datestr;
    }
    public int getCriterion() {
        return criterion;
    }
    public char getRating() {
        return rating;
    }
    public void setRating(char rating) {
        this.rating = rating;
    }
    public boolean getFlag() {
        return flag;
    }
    public void setFlag(boolean flag) {
        this.flag = flag;
    }
    /*
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;

        RatingPerStudent other = (RatingPerStudent) obj;
        if (criterion == 0) {
            if (other.criterion != 0)
                return false;
        }
        else if (criterion != other.criterion)
            return false;
        return true;
    }
    */
}