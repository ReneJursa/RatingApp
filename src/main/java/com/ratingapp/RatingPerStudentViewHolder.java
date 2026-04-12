package com.ratingapp;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
public class RatingPerStudentViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public TextView courseName;
    public TextView vorname;
    public TextView name;
    public TextView date_str;
    public TextView criterion;
    public TextView rating;
    public TextView flag;
    private OnItemClickListener mListener;
    public RatingPerStudentViewHolder(View ratingPerStudentView, OnItemClickListener listener) {
        super(ratingPerStudentView);
        //courseName = ratingPerStudentView.findViewById(R.id.row_ratingperstudent_coursename);
        //vorname = ratingPerStudentView.findViewById(R.id.row_ratingperstudent_vorname);
        name = ratingPerStudentView.findViewById(R.id.row_ratingperstudent_name);
        date_str = ratingPerStudentView.findViewById(R.id.row_ratingperstudent_datestr);
        criterion = ratingPerStudentView.findViewById(R.id.row_ratingperstudent_criterion);
        rating = ratingPerStudentView.findViewById(R.id.row_ratingperstudent_rating);
        //flag = ratingPerStudentView.findViewById(R.id.row_ratingperstudent_flag);
        mListener = listener;
        ratingPerStudentView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int i = getLayoutPosition();
        mListener.onItemClick(view, getAdapterPosition());
        //Log.d("RPSVH onclick", "onClick "
        //       + getLayoutPosition() + " " + name.getText());
    }
}
