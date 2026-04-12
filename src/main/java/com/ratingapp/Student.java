package com.ratingapp;
import android.os.Parcel;
import android.os.Parcelable;

public class Student implements Parcelable {
    String vorname = "unbekannt";
    String nachname = "unbekannt";
    public Student(String vorname, String nachname) {
        this.vorname = vorname;
        this.nachname = nachname;
    }
    public String getVorname() {
        return vorname;
    }
    public String getNachname() {
        return nachname;
    }

    public static final Parcelable.Creator<Student> CREATOR = new Parcelable.Creator<Student>() {
        @Override
        public Student createFromParcel(Parcel source) {
            return new Student(source);
        }
        @Override
        public Student[] newArray(int size) {
            return new Student[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel destination, int flags) {
        destination.writeString(vorname);
        destination.writeString(nachname);
    }

    protected Student(Parcel source) {
        vorname = source.readString();
        nachname = source.readString();
    }
}