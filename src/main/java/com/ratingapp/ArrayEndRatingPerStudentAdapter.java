package com.ratingapp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ArrayEndRatingPerStudentAdapter extends RecyclerView.Adapter <EndRatingPerStudentViewHolder> {
    private static final String TAG = "ArrayEndRatingPerStudentAdapter";
    private OnItemClickListener mListener;
    private int endrating_row_layout;
    private ArrayList<EndRatingPerStudent> endRatingPerStudentArrayList;
    private Context cxt;

    // Constructor of the class
    public ArrayEndRatingPerStudentAdapter(int endrating_row_layout_as_id,
                                        ArrayList<EndRatingPerStudent> endRatingPerStudentArrayList, Context context, OnItemClickListener listener) {
        endrating_row_layout = endrating_row_layout_as_id;
        this.endRatingPerStudentArrayList = endRatingPerStudentArrayList;
        this.cxt = context;
        mListener = listener;
    }

    // return the size of the list
    @Override
    public int getItemCount() {
        return endRatingPerStudentArrayList == null ? 0 : endRatingPerStudentArrayList.size();
    }

    //   turning the layout for each row in the list to View object
    @Override
    public EndRatingPerStudentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //   cxt = parent.getContext() ;
        View myBookview = LayoutInflater.from(parent.getContext()).
                inflate(endrating_row_layout, parent, false);

        // create GUI object equivalent to the Book object
        EndRatingPerStudentViewHolder myViewHolder = new EndRatingPerStudentViewHolder(myBookview, mListener);
        return myViewHolder;
    }

    // load data to each row in the list
    @Override
    public void onBindViewHolder(final EndRatingPerStudentViewHolder holder, final int listPosition) {
        TextView courseName = holder.courseName;
        TextView vorname = holder.vorname;
        TextView name = holder.name;
        TextView date_str = holder.date_str;
        TextView criterion = holder.criterion;
        TextView rating = holder.rating;
        TextView flag = holder.flag;
        TextView numberOfRatings = holder.numberOfRatings;
        String cn = endRatingPerStudentArrayList.get(listPosition).getCourseName();
        String vn = endRatingPerStudentArrayList.get(listPosition).getVorname();
        String dt = endRatingPerStudentArrayList.get(listPosition).getDate();

        //courseName.setText(endRatingPerStudentArrayList.get(listPosition).getCourseName());
        //vorname.setText(endRatingPerStudentArrayList.get(listPosition).getVorname());
        name.setText(endRatingPerStudentArrayList.get(listPosition).getName());
        //date_str.setText(endRatingPerStudentArrayList.get(listPosition).getDate());
        String criterion_str = String.valueOf(endRatingPerStudentArrayList.get(listPosition).getCriterion());
        criterion.setText("Krit: " + criterion_str);
        String rating_str = String.valueOf(endRatingPerStudentArrayList.get(listPosition).getRating());
        rating.setText(rating_str);
        String flag_str = Boolean.toString(endRatingPerStudentArrayList.get(listPosition).getFlag());
        //flag.setText(flag_str);
        String numberOfRatings_str = String.valueOf(endRatingPerStudentArrayList.get(listPosition).getNumberOfRatings());
        numberOfRatings.setText(numberOfRatings_str);

        //Log.v(TAG, "AER: " + cn + " " + vn + " " + dt + " " + criterion_str + " " + rating_str + " " + flag_str + " " + numberOfRatings_str);
    }
}
