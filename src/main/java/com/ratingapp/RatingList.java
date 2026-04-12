package com.ratingapp;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;

public class RatingList implements Parcelable {
    public String courseName = "unbekannt";
    ArrayList<Rating> rList = new ArrayList<>();
    public RatingList(String courseName, ArrayList<RatingBoundView> ratingBoundViewList) {
        this.courseName = courseName;
        rList.clear();
        for (RatingBoundView ct: ratingBoundViewList) {
            this.rList.add(new Rating(courseName, ct.getVorname(), ct.getNachname(), ct.getCriterion(), ct.getName(), ct.getRating(), ct.isFlag()));
        }
    }

    public String getCourseName() {
        return courseName;
    }

    public static final Parcelable.Creator<RatingList> CREATOR = new Parcelable.Creator<RatingList>() {
        @Override
        public RatingList createFromParcel(Parcel source) { return new RatingList(source); }
        @Override
        public RatingList[] newArray(int size) {
            return new RatingList[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel destination, int flags) {
        destination.writeString(courseName);
        destination.writeList(rList);
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    protected RatingList(Parcel source) {
        this.courseName = source.readString();
        this.rList = source.readArrayList(Rating.class.getClassLoader());
    }
}
