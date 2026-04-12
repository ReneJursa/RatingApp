package com.ratingapp;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.RequiresApi;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import java.util.Objects;

public class RatingPerStudentParcel extends BaseObservable implements Parcelable {
    private String courseName;
    private String vorname;
    private String nachname;
    private String name;
    private String datestr;
    private int criterion;
    @Bindable
    private char rating;
    private boolean flag;

    public RatingPerStudentParcel(String courseName, String vorname, String nachname, String datestr, int criterion, char rating, boolean flag) {
        this.courseName = courseName;
        this.vorname = vorname;
        this.nachname = nachname;
        this.name = vorname + " " + nachname;
        this.datestr = datestr;
        this.criterion = criterion;
        this.rating = rating;
        notifyPropertyChanged(BR.rating);
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
        notifyPropertyChanged(BR.rating);
    }
    public boolean getFlag() {
        return flag;
    }
    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public static final Parcelable.Creator<RatingPerStudentParcel> CREATOR = new Parcelable.Creator<RatingPerStudentParcel>() {
        @Override
        public RatingPerStudentParcel createFromParcel(Parcel source) {
            return new RatingPerStudentParcel(source);
        }
        @Override
        public RatingPerStudentParcel[] newArray(int size) {
            return new RatingPerStudentParcel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel destination, int flags) {
        destination.writeString(courseName);
        destination.writeString(vorname);
        destination.writeString(nachname);
        destination.writeString(name);
        destination.writeString(datestr);
        destination.writeInt(criterion);
        destination.writeString(String.valueOf(rating));
        destination.writeBoolean(flag);
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    protected RatingPerStudentParcel(Parcel source) {
        this.courseName = source.readString();
        this.vorname = source.readString();
        this.nachname = source.readString();
        this.name = source.readString();
        this.datestr = source.readString();
        this.criterion = source.readInt();
        this.rating = Objects.requireNonNull(source.readString()).charAt(0);
        this.flag = source.readBoolean();
    }
}
