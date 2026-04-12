package com.ratingapp;
import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
public class CourseViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public TextView name;
    private OnItemClickListener mListener;
    //public static final String MESSAGE = "CourseName.MESSAGE";
    //private static final String TAG = "CourseViewHolder";

    public CourseViewHolder(View courseView, OnItemClickListener listener) {
        super(courseView);
        name = courseView.findViewById(R.id.row_course_name);
        mListener = listener;
        courseView.setOnClickListener(this);
    }
    /*
    @Override
    public void onClick(View view) {
        String courseName = name.getText().toString();
        Log.d("onclick", "onClick " + getLayoutPosition() + " " + name.getText());

        //Intent intent = new Intent(this, RatingActivity.class);
        //intent.putExtra(MESSAGE, courseName);
        //Log.d(TAG, "Intent fired");
        //startActivity(intent);
    }
    */
    /*
    @Override
    public void onClick(View v) {
        //onItemClickListener.onItemClick(v, getLayoutPosition());
        String courseName = name.getText().toString();
        Log.d("onclick", "onClick " + getLayoutPosition() + " " + name.getText());
    }

     */
    @Override
    public void onClick(View view) {
        mListener.onItemClick(view, getAdapterPosition());
        //String courseName = name.getText().toString();
        //Log.d("onclick", "onClick " + getLayoutPosition() + " " + name.getText());
    }
}