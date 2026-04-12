package com.ratingapp;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

public class RatingBoundView extends BaseObservable {
    private static final String ratingPlusCircleMinus = "Plus-Kreis-Minus-Skala";
    private static final String ratingABCD = "ABCD-Notenskala";
    private static final String rating1till6 = "Notenskala von 1 bis 6";
    private int maxRatingIndex;
    private char[] ratingSymbols;
    private String courseName;
    private String vorname;
    private String nachname;
    @Bindable
    private String mName;
    @Bindable
    private boolean mFlag = false;
    @Bindable
    private int mCriterion;
    @Bindable
    private char mRating;
    @Bindable
    private int ratingIndex;
    public RatingBoundView(String courseName, String vorname, String nachname, int criterion, String name, char rating, String ratingScale) {
        this.courseName = courseName;
        this.vorname = vorname;
        this.nachname = nachname;
        mCriterion = criterion;
        mName = name;
        if(ratingScale.equals(rating1till6)) {
            ratingSymbols = new char[7];
            ratingSymbols[0] = (char)32;
            ratingSymbols[1] = '1';
            ratingSymbols[2] = '2';
            ratingSymbols[3] = '3';
            ratingSymbols[4] = '4';
            ratingSymbols[5] = '5';
            ratingSymbols[6] = '6';
            maxRatingIndex = 6;
        }
        else if(ratingScale.equals(ratingPlusCircleMinus))  {
            ratingSymbols = new char[4];
            ratingSymbols[0] = (char)32;
            ratingSymbols[1] = '+';
            ratingSymbols[2] = 'o';
            ratingSymbols[3] = '-';
            maxRatingIndex = 3;
        }
        else {
            ratingSymbols = new char[5];
            ratingSymbols[0] = (char)32;
            ratingSymbols[1] = 'A';
            ratingSymbols[2] = 'B';
            ratingSymbols[3] = 'C';
            ratingSymbols[4] = 'D';
            maxRatingIndex = 4;
        }
        mRating = rating;
        for (int i=0; i < ratingSymbols.length; i++) {
            if (ratingSymbols[i] == mRating) {
                ratingIndex = i;
            }
        }
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
    public int getCriterion() {
        return mCriterion;
    }
    public void setCriterion(int criterion) {
        mCriterion = criterion;
        notifyPropertyChanged(BR.criterion);
    }
    public String getName() {
        return mName;
    }
    public boolean isFlag() {
        return mFlag;
    }
    public void setName(String name) {
        mName = name;
        notifyPropertyChanged(BR.name);
    }
    public void setFlag(boolean flag, int ratingIndex) {
        if(!mFlag) {
            mFlag = true;
            notifyPropertyChanged(BR.flag);
        }
        if (this.ratingIndex < maxRatingIndex) {
            this.ratingIndex = ratingIndex + 1;
        }
        else {
            this.ratingIndex = 0;
            mFlag = false;
            notifyPropertyChanged(BR.flag);
        }
        char rating = ratingSymbols[this.ratingIndex];
        setRating(rating);
    }
    public void setFlag(boolean flag) {
        mFlag = flag;
    }
    public boolean getFlag() {
        return mFlag;
    }
    public char getRating() {
        return mRating;
    }
    public void setRating(char rating) {
        mRating = rating;
        notifyPropertyChanged(BR.rating);
    }
    public int getRatingIndex() {
        return ratingIndex;
    }
    public void setRatingIndex(int ratingIndex) {
        this.ratingIndex = ratingIndex;
    }
}