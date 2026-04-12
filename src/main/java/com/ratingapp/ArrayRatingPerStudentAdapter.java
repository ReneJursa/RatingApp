package com.ratingapp;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ArrayRatingPerStudentAdapter extends RecyclerView.Adapter <RatingPerStudentViewHolder> {
    private static final String TAG = "ArrayRatingPerStudentAdapter";
    private OnItemClickListener mListener;
    private int rating_row_layout;
    private ArrayList <RatingPerStudent> ratingPerStudentArrayList;
    private Context cxt;

    // Constructor of the class
    public ArrayRatingPerStudentAdapter(int rating_row_layout_as_id,
                            ArrayList<RatingPerStudent> ratingPerStudentArrayList, Context context, OnItemClickListener listener) {
        rating_row_layout = rating_row_layout_as_id;
        this.ratingPerStudentArrayList = ratingPerStudentArrayList;
        this.cxt = context;
        mListener = listener;
    }

    // return the size of the list
    @Override
    public int getItemCount() {
        return ratingPerStudentArrayList == null ? 0 : ratingPerStudentArrayList.size();
    }

    //   turning the layout for each row in the list to View object
    @Override
    public RatingPerStudentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //   cxt = parent.getContext() ;
        View myBookview = LayoutInflater.from(parent.getContext()).
                inflate(rating_row_layout, parent, false);

        // create GUI object equivalent to the Book object
        RatingPerStudentViewHolder myViewHolder = new RatingPerStudentViewHolder(myBookview, mListener);
        return myViewHolder;
    }

    // load data to each row in the list
    @Override
    public void onBindViewHolder(final RatingPerStudentViewHolder holder, final int listPosition) {
        TextView courseName = holder.courseName;
        TextView vorname = holder.vorname;
        TextView name = holder.name;
        TextView date_str = holder.date_str;
        TextView criterion = holder.criterion;
        TextView rating = holder.rating;
        TextView flag = holder.flag;
        String cn = ratingPerStudentArrayList.get(listPosition).getCourseName();
        String vn = ratingPerStudentArrayList.get(listPosition).getVorname();
        String dt = ratingPerStudentArrayList.get(listPosition).getDate();

        //courseName.setText(ratingPerStudentArrayList.get(listPosition).getCourseName());
        //vorname.setText(ratingPerStudentArrayList.get(listPosition).getVorname());
        name.setText(ratingPerStudentArrayList.get(listPosition).getName());
        date_str.setText(ratingPerStudentArrayList.get(listPosition).getDate());
        String criterion_str = String.valueOf(ratingPerStudentArrayList.get(listPosition).getCriterion());
        criterion.setText("Krit: " + criterion_str);
        String rating_str = String.valueOf(ratingPerStudentArrayList.get(listPosition).getRating());
        rating.setText(rating_str);
        //String flag_str = Boolean.toString(ratingPerStudentArrayList.get(listPosition).getFlag());
        //flag.setText(flag_str);
        //Log.v(TAG, "AR: " + cn + " " + vn + " " + dt + " " + criterion_str + " " + rating_str + " " + flag_str);
    }
}
