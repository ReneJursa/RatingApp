package com.ratingapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.google.gson.Gson;

import com.google.gson.reflect.TypeToken;
import com.ratingapp.databinding.ActivitySettingsBinding;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class SettingsActivity extends AppCompatActivity {
    public final static String COURSE_OBJECT = "CourseObject";
    private static final String sharedSettingsPreferences_str = "com.ratingapp.SettingsActivity.SHAREDPREFERENCES";
    private static final String keySettings = "SettingsKey";
    private static final String sharedPreferences_str = "com.ratingapp.SHAREDPREFERENCES";
    private static final String keyAllRatedCourses = "allRatedCourses";
    private TreeMap<String, TreeMap<String, ArrayList<RatingPerStudent>>> ratingPerStudentMap = new TreeMap<String, TreeMap<String, ArrayList<RatingPerStudent>>>();
    private static final String TAG = "SettingsActivity";
    private Settings settings;
    private TreeMap<String, Settings> settingsMap = new TreeMap<String, Settings>();
    ActivitySettingsBinding binding;
    private Course course;
    RadioButton radioRatingPCM, radioRatingABCD, radioRating1till6, radioArithmeticMean, radioMedian;
    TextView quantityView;
    Button submit;
    private String ratingScale = "keine";
    private static final String ratingPlusCircleMinus = "Plus-Kreis-Minus-Skala";
    private static final String ratingABCD = "ABCD-Notenskala";
    private static final String rating1till6 = "Notenskala von 1 bis 6";
    private String ratingAverage = "kein";
    private String ratingScale_prev;
    private static final String arithmeticMean_str = "ArithmeticMean";
    private static final String median_str = "Median";
    private int nrOfCriteria = 3;
    private int nrOfCriteria_prev;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_settings);
        ratingScale = ratingABCD;
        ratingScale_prev = ratingScale;
        ratingAverage = arithmeticMean_str;
        Intent intent = getIntent();
        if (intent != null) {
            //Log.v(TAG, "intent in settings not null");
            if (intent.hasExtra(COURSE_OBJECT)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    course = intent.getParcelableExtra(COURSE_OBJECT, Course.class);
                } else {
                    course = intent.getParcelableExtra(COURSE_OBJECT);
                }
                binding.setCourse(course);
                loadSettings();
                settings = settingsMap.get(course.getName());
                loadRatingPerStudentMap();
                //for (int i=0; i < 2; i++) {
                    //Toast.makeText(SettingsActivity.this, "Achtung: Beim Ändern der Bewertungsskala oder der Anzahl der Kriterien werden alle gespeicherten Bewertungen zu " + course.getName() + " gelöscht.", Toast.LENGTH_LONG).show();
                    Toast.makeText(SettingsActivity.this, "Achtung: Beim Ändern der Bewertungsskala werden alle gespeicherten Bewertungen zu " + course.getName() + " gelöscht.", Toast.LENGTH_LONG).show();
                //}
                if(settings != null) {
                    if (settings.getRatingScale().equals(ratingPlusCircleMinus)) {
                        ratingScale = ratingPlusCircleMinus;
                        ratingScale_prev = ratingScale;
                    }
                    else if (settings.getRatingScale().equals(rating1till6)) {
                        ratingScale = rating1till6;
                        ratingScale_prev = ratingScale;
                    }
                    if (settings.getRatingAverage().equals(median_str)) {
                        ratingAverage = median_str;
                    }
                    nrOfCriteria = settings.getNrOfCriteria();
                    nrOfCriteria_prev = nrOfCriteria;
                }
            }
        }
        //Log.v(TAG, "prev ratingScale " + ratingScale_prev + " ratingAverage " + ratingAverage + " nrOfCriteria " + nrOfCriteria);
        radioRatingPCM = (RadioButton) findViewById(R.id.radioRatingPCM);
        radioRatingABCD = (RadioButton) findViewById(R.id.radioRatingABCD);
        radioRating1till6 = (RadioButton) findViewById(R.id.radioRating1till6);
        if(ratingScale.equals(ratingPlusCircleMinus)) {
            radioRatingPCM.setChecked(true);
        }
        if(ratingScale.equals(ratingABCD)) {
            radioRatingABCD.setChecked(true);
        }
        if(ratingScale.equals(rating1till6)) {
            radioRating1till6.setChecked(true);
        }
        radioArithmeticMean = (RadioButton) findViewById(R.id.radioArithmeticMean);
        radioMedian = (RadioButton) findViewById(R.id.radioMedian);
        if(ratingAverage.equals(median_str)) {
            radioMedian.setChecked(true);
        }
        quantityView = (TextView)findViewById(R.id.criterion_number);
        quantityView.setText(String.valueOf(nrOfCriteria));
        submit = (Button) findViewById(R.id.submitButton);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (radioRatingPCM.isChecked()) {
                    //Log.v(TAG, "radioRatingPCM.isChecked()");
                    ratingScale = ratingPlusCircleMinus;
                    radioRatingPCM.setChecked(true);
                    if(!ratingScale_prev.equals(ratingPlusCircleMinus)) {
                        deleteAllRatingsPerStudent();
                    }
                }
                else if (radioRatingABCD.isChecked()) {
                    //Log.v(TAG, "radioRatingABCD.isChecked()");
                    ratingScale = ratingABCD;
                    radioRatingABCD.setChecked(true);
                    if(!ratingScale_prev.equals(ratingABCD)) {
                        deleteAllRatingsPerStudent();
                    }
                }
                else if (radioRating1till6.isChecked()) {
                    //Log.v(TAG, "radioRating1till6.isChecked()");
                    ratingScale = rating1till6;
                    radioRating1till6.setChecked(true);
                    if(!ratingScale_prev.equals(rating1till6)) {
                        deleteAllRatingsPerStudent();
                    }
                }
                else {
                    Toast.makeText(SettingsActivity.this, "Keine Bewertungsskala ausgewählt", Toast.LENGTH_LONG).show();
                }
                if (radioArithmeticMean.isChecked()) {
                    //Log.v(TAG, "radioArithmeticMean.isChecked()");
                    radioArithmeticMean.setChecked(true);
                    ratingAverage = arithmeticMean_str;
                }
                else if (radioMedian.isChecked()) {
                    //Log.v(TAG, "radioMedian.isChecked()");
                    radioMedian.setChecked(true);
                    ratingAverage = median_str;
                }
                else {
                    Toast.makeText(SettingsActivity.this, "Kein Mittelwert ausgewählt", Toast.LENGTH_LONG).show();
                }
                /*
                if(nrOfCriteria != nrOfCriteria_prev) {
                    deleteAllRatingsPerStudent();
                }
                */
                String courseName = course.getName();
                settings = new Settings(ratingScale, ratingAverage, nrOfCriteria);
                if(!settingsMap.containsKey(courseName)) {
                    settingsMap.put(courseName, settings);
                    //Log.v(TAG,"if " + ratingScale + " " +  ratingAverage + " " + nrOfCriteria);
                }
                else {
                    settingsMap.get(courseName).setRatingScale(ratingScale);
                    settingsMap.get(courseName).setRatingAverage(ratingAverage);
                    settingsMap.get(courseName).setNrOfCriteria(nrOfCriteria);
                    //Log.v(TAG,"else " + ratingScale + " " +  ratingAverage + " " + nrOfCriteria);
                }

                saveSettings();
            }
        });
    }
    public void loadSettings() {
        try {
            SharedPreferences sharedPreferences = getSharedPreferences(sharedSettingsPreferences_str, MODE_PRIVATE);
            if (sharedPreferences != null) {
                Gson gson = new Gson();
                String json = sharedPreferences.getString(keySettings, null);
                Type type = new TypeToken<TreeMap<String, Settings>>() {}.getType();
                if (gson.fromJson(json, type) != null) {
                    settingsMap = gson.fromJson(json, type);
                }
                else {
                    //Log.v(TAG, "Einstellungen konnten nicht geladen werden");
                    Toast.makeText(this, "Einstellungen konnten nicht geladen werden", Toast.LENGTH_LONG).show();
                }

            } else {
                Toast.makeText(this, "Keine gespeicherten Einstellungen vorhanden", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            //Toast.makeText(RatingActivity.this, "Some error occurred while getting shared preferences " + e.getMessage(), Toast.LENGTH_LONG).show();
            Toast.makeText(this, "Keine gespeicherten Einstellungen vorhanden ", Toast.LENGTH_LONG).show();
        }
    }
    private void saveSettings() {
        SharedPreferences sharedPreferences = getSharedPreferences(sharedSettingsPreferences_str, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        Gson gson = new Gson();
        String json = gson.toJson(settingsMap);
        editor.putString(keySettings, json);
        boolean saveFlag = editor.commit();
        if (saveFlag) {
            Toast.makeText(this, "Einstellungen für " + course.getName() + " gespeichert", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Speicherung der Einstellungen fehlgeschlagen", Toast.LENGTH_LONG).show();
        }
    }
    public void loadRatingPerStudentMap() {
        try {
            SharedPreferences sharedPreferences = getSharedPreferences(sharedPreferences_str, MODE_PRIVATE);
            if (sharedPreferences != null) {
                Gson gson = new Gson();
                String json = sharedPreferences.getString(keyAllRatedCourses, null);
                Type type = new TypeToken<TreeMap<String, TreeMap<String, ArrayList<RatingPerStudent>>>>() {}.getType();
                if (gson.fromJson(json, type) != null) {
                    ratingPerStudentMap = gson.fromJson(json, type);
                }
            } else {
                Toast.makeText(this, "Keine gespeicherten Daten vorhanden", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            //Toast.makeText(RatingActivity.this, "Some error occurred while getting shared preferences " + e.getMessage(), Toast.LENGTH_LONG).show();
            Toast.makeText(this, "Keine gespeicherten Daten vorhanden ", Toast.LENGTH_LONG).show();
        }
    }
    private void deleteAllRatingsPerStudent() {
        SharedPreferences sharedPreferences = getSharedPreferences(sharedPreferences_str, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        Gson gson = new Gson();
        for (Map.Entry<String, TreeMap<String, ArrayList<RatingPerStudent>>> mapIt : ratingPerStudentMap.entrySet()) {
            String courseName = mapIt.getKey();
            if(courseName.equals(course.getName())) {
                mapIt.getValue().clear();
            }
        }
        String jsonRatingPerStudentMap = gson.toJson(ratingPerStudentMap);
        editor.putString(keyAllRatedCourses, jsonRatingPerStudentMap);
        boolean deleteFlag = editor.commit();
        if (deleteFlag) {
            Toast.makeText(SettingsActivity.this, "Alle Bewertungen zu " + course.getName() + " gelöscht ", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(SettingsActivity.this, "Löschung fehlgeschlagen", Toast.LENGTH_LONG).show();
        }
    }
    public void Decrement(View view) {
        quantityView = (TextView)findViewById(R.id.criterion_number);
        String quantityString = quantityView.getText().toString();
        nrOfCriteria = Integer.parseInt(quantityString);
        nrOfCriteria -= 1;
        if (nrOfCriteria < 1) {
            nrOfCriteria = 1;
            Toast.makeText(this, "Kann nicht kleiner als 1 sein", Toast.LENGTH_SHORT).show();
        }
        quantityView.setText(String.valueOf(nrOfCriteria));
    }
    public void Increment(View view) {
        quantityView = (TextView)findViewById(R.id.criterion_number);
        String quantityString = quantityView.getText().toString();
        nrOfCriteria = Integer.parseInt(quantityString);
        nrOfCriteria += 1;
        if (nrOfCriteria > 100) {
            nrOfCriteria = 100;
            Toast.makeText(this, "Kann nicht größer als 100 sein", Toast.LENGTH_SHORT).show();
        }
        quantityView.setText(String.valueOf(nrOfCriteria));
    }
    public void goHome(View view) {
        Intent intent = new Intent(this, RatingActivity.class);
        intent.putExtra(COURSE_OBJECT, course);
        startActivity(intent);
    }
}