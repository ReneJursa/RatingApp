package com.ratingapp;

import android.view.View;
import android.widget.TextView;

public class EndRatingPerStudentViewHolder extends RatingPerStudentViewHolder {
    public TextView numberOfRatings;
    public EndRatingPerStudentViewHolder(View ratingPerStudentView, OnItemClickListener listener) {
        super(ratingPerStudentView, listener);
        numberOfRatings = ratingPerStudentView.findViewById(R.id.row_ratingperstudent_numberofratings);
    }
    /*
    @Override
    public void onClick(View view) {
        int i = getLayoutPosition();
        //Log.d("RPSVH onclick", "onClick "
        //       + getLayoutPosition() + " " + name.getText());
    }
    */
}
