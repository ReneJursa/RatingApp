package com.ratingapp;

import android.widget.EditText;

import androidx.databinding.BindingAdapter;
import androidx.databinding.InverseBindingAdapter;
public class CharToStringBindingAdapter {
    @BindingAdapter("android:text")
    public static void setText(EditText view, char value) {
        String stringValue = value == 0 ? "" : String.valueOf(value);
        if (stringValue.equals(view.getText().toString())) {
            return;
        }
        view.setText(stringValue);
    }
    @InverseBindingAdapter(attribute = "android:text")
    public static int getText(EditText view) {
        if (view.getText() == null) {
            return 0;
        }

        String stringValue = view.getText().toString();
        if (stringValue.isEmpty()) {
            return 0;
        }
        return stringValue.charAt(0);
    }
}
