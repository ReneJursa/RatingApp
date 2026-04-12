package com.ratingapp;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ArrayCourseAdapter extends RecyclerView.Adapter <CourseViewHolder> {
    private static final String TAG = "CourseAdapter";
    private OnItemClickListener mListener;
    private int course_row_layout;
    private ArrayList <Course> courseArrayList;
    // Constructor of the class
    public ArrayCourseAdapter(int rating_row_layout_as_id,
                                        ArrayList<Course> courseArrayList, Context context, OnItemClickListener listener) {
        course_row_layout = rating_row_layout_as_id;
        this.courseArrayList = courseArrayList;
        mListener = listener;
    }

    // return the size of the list
    @Override
    public int getItemCount() {
        return courseArrayList == null ? 0 : courseArrayList.size();
    }

    //   turning the layout for each row in the list to View object

    @Override
    public CourseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //   cxt = parent.getContext() ;
        View mCourseview = LayoutInflater.from(parent.getContext()).
                inflate(course_row_layout, parent, false);

        // create GUI object equivalent to the Book object
        CourseViewHolder mViewHolder = new CourseViewHolder(mCourseview, mListener);
        return mViewHolder;
    }

    // load data to each row in the list
    @Override
    public void onBindViewHolder(final CourseViewHolder holder, final int listPosition) {
        TextView courseName = holder.name;
        String cn = courseArrayList.get(listPosition).getName();
        courseName.setText(courseArrayList.get(listPosition).getName());
        //Log.v(TAG, "ACR: " + cn);
    }
}
