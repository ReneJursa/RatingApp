package com.ratingapp;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.RequiresApi;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import java.util.ArrayList;

public class Course extends BaseObservable implements Parcelable {

    @Bindable
    private String name = "unbekannt";
    ArrayList<Student> studList = new ArrayList<Student>();

    public Course(String name) {
        this.name = name;
        notifyPropertyChanged(BR.name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        notifyPropertyChanged(BR.name);
    }

    public static final Parcelable.Creator<Course> CREATOR = new Parcelable.Creator<Course>() {
        @Override
        public Course createFromParcel(Parcel source) {
            return new Course(source);
        }
        @Override
        public Course[] newArray(int size) {
            return new Course[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel destination, int flags) {
        destination.writeString(name);
        destination.writeList(studList);
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    protected Course(Parcel source) {
        this.name = source.readString();
        this.studList = source.readArrayList(Student.class.getClassLoader());
    }
}