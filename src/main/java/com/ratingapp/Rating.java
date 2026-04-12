package com.ratingapp;
import android.os.Parcel;
import android.os.Parcelable;
public class Rating implements Parcelable{
    String courseName;
    String vorname;
    String nachname;
    int criterion;
    String name;
    char rating;
    boolean flag = false;
    public Rating(String courseName, String vorname, String nachname, int criterion, String name, char rating, boolean flag) {
        this.courseName = courseName;
        this.vorname = vorname;
        this.nachname = nachname;
        this.criterion = criterion;
        this.name = name;
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
    public int getCriterion() {
        return criterion;
    }
    public String getName() {
        return name;
    }
    public char getRating() {
        return rating;
    }
    public boolean getFlag() {
        return flag;
    }

    public static final Parcelable.Creator<Rating> CREATOR = new Parcelable.Creator<Rating>() {
        @Override
        public Rating createFromParcel(Parcel source) {return new Rating(source); }
        @Override
        public Rating[] newArray(int size) {
            return new Rating[size];
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
        destination.writeInt(criterion);
        destination.writeString(name);
        destination.writeString(String.valueOf(rating));
        destination.writeBoolean(flag);
    }

    protected Rating(Parcel source) {
        courseName = source.readString();
        vorname = source.readString();
        nachname = source.readString();
        criterion = source.readInt();
        name = source.readString();
        rating = source.readString().charAt(0);
        flag = source.readBoolean();
    }
}
