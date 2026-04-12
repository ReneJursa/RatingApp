package com.ratingapp;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    public ArrayList<Course> courseArrayList = new ArrayList<Course>();
    ArrayCourseAdapter arrayCourseAdapter;
    public final static String COURSE_OBJECT = "CourseObject";
    Course course;
    private static final String keyUnRatedCourseList = "unRatedCourseList";
    private static final String sharedPreferences_str = "com.ratingapp.MainActivity.SHAREDPREFERENCES";
    private static final String sharedSettingsPreferences_str = "com.ratingapp.SettingsActivity.SHAREDPREFERENCES";
    private static final String keySettings = "SettingsKey";
    private Settings settings;
    private TreeMap<String, Settings> settingsMap = new TreeMap<String, Settings>();
    private boolean deleteCourseFlag = false;
    private boolean displayRatingsFlag = false;
    private boolean displayRatingStatisticsFlag = false;

    int mPosition;
    public static final String COURSENAMEMESSAGE = "com.ratingapp.COURSENAMEMESSAGE";
    private static final String TAG = "MainActivity";
    private final int nrOfCriteria = 3;
    private static final String ratingABCD = "ABCD-Notenskala";
    private final String ratingScale = ratingABCD;
    private static final String arithmeticMean_str = "ArithmeticMean";
    private final String ratingAverage = arithmeticMean_str;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        courseArrayList.clear();
        loadData();
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(COURSE_OBJECT)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                course = intent.getParcelableExtra(COURSE_OBJECT, Course.class);
            }
            else {
                course = intent.getParcelableExtra(COURSE_OBJECT);
            }
            String courseName = "unbekannt";
            if (course != null) {
                courseName = course.getName();
                //Log.i(TAG, "Main got course name " + courseName);
                for (Student st : course.studList) {
                    String vorname = st.getVorname();
                    String nachname = st.getNachname();
                    //Log.v(TAG, "Got Course Object 1: " + course.getName() + " " + vorname + " " + nachname);
                }
                boolean courseExistFlag = false;
                for (Course cr: courseArrayList) {
                    if (cr.getName().equalsIgnoreCase(courseName)) {
                        courseExistFlag = true;
                        break;
                    }
                }
                if(!courseExistFlag) {
                    courseArrayList.add(course);
                    saveData();
                    loadSettings();

                    if(!settingsMap.containsKey(courseName)) {
                        settings = new Settings(ratingScale, ratingAverage, nrOfCriteria);
                        settingsMap.put(courseName, settings);
                        //Log.v(TAG, "keine Settings vorhanden " + courseName);
                        saveSettings();
                    }
                    else {
                        //Log.v(TAG, "Settings vorhanden " + courseName);
                        settings = settingsMap.get(courseName);
                        //Log.v(TAG, "MA: Settings " + settings.getRatingScale() + " " + settings.getRatingAverage() + " " + settings.getNrOfCriteria());
                    }
                }
                else {
                    Toast.makeText(this, "Eingelesener Kurs aus Datei schon vorhanden.", Toast.LENGTH_SHORT).show();
                }
            }
        }
        Comparator<Course> byValue = Comparator.comparing(Course::getName);
        courseArrayList.sort(byValue);
        bindCourseArrayList();
    }
    private void bindCourseArrayList() {
        /*
        OnItemClickListener listener = (view, position) -> {
            Log.d("onclick", "MA onClick position " + " " + position);
            mPosition = position;
            String courseName = courseArrayList.get(mPosition).getName();
            Log.d("onclick", "MA onClick position " + " " + mPosition + " " + courseName);

            Intent intent = new Intent(this, RatingActivity.class);
            intent.putExtra(MESSAGE, courseName);
            Log.d(TAG, "Intent fired");
            startActivity(intent);
        };
        */
        OnItemClickListener listener = new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                //Log.d("onclick", "MA onClick position " + " " + position);
                mPosition = position;
                course = courseArrayList.get(mPosition);
                String courseName = course.getName();
                //Log.d("onclick", "MA onClick position " + " " + mPosition + " " + courseName);
                if(displayRatingsFlag) {
                    callDisplayRatings(courseName);
                }
                else if(displayRatingStatisticsFlag) {
                    callDisplayRatingStatistics(courseName);
                }
                else if(deleteCourseFlag) {
                    /*
                    courseArrayList.remove(mPosition);
                    deleteCourseFlag = false;
                    Toast.makeText(MainActivity.this, "Kurs " + courseName + " wurde gelöschst", Toast.LENGTH_SHORT).show();
                    saveData();
                    bindCourseArrayList();
                    */
                    deleteCourseAlertDialog(courseName);
                }
                else {
                    /*
                    for (Student st : course.studList) {
                        String vorname = st.getVorname();
                        String nachname = st.getNachname();
                        Log.v(TAG, "Course Object: " + course.getName() + " " + vorname + " " + nachname);
                    }
                    */
                    Intent intent = new Intent(MainActivity.this, RatingActivity.class);
                    //intent.putExtra(COURSENAMEMESSAGE, courseName);
                    intent.putExtra(COURSE_OBJECT, course);
                    //Log.v(TAG, "Send Course Object");
                    startActivity(intent);
                }
            }
        };
        arrayCourseAdapter = new ArrayCourseAdapter(R.layout.course_row, courseArrayList, this, listener);
        recyclerView = findViewById(R.id.recycler_id);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(arrayCourseAdapter);
    }
    private void loadData() {

        try {
            SharedPreferences sharedPreferences = getSharedPreferences(sharedPreferences_str, MODE_PRIVATE);
            if (sharedPreferences != null) {
                //Log.v(TAG," sharedPreferences != null ");
                Gson gson = new Gson();
                String json = sharedPreferences.getString(keyUnRatedCourseList, null);
                Type type = new TypeToken<ArrayList<Course>>() {}.getType();
                if(gson.fromJson(json, type) != null) {
                    //Log.v(TAG," gson.fromJson(json, type) != null  ");
                    courseArrayList = gson.fromJson(json, type);
                }
                if (courseArrayList != null && !courseArrayList.isEmpty()) {
                    //Toast.makeText(this, "gespeicherte Kursdaten geladen ", Toast.LENGTH_SHORT).show();
                    for(Course c : courseArrayList) {
                        String courseName = c.getName();
                        //Log.i(TAG, "Kurs " + courseName);
                    }
                }
                /*
                else {
                    //Toast.makeText(this, "Kursdaten konnten nicht geladen werden", Toast.LENGTH_SHORT).show();
                    Log.v(TAG,"Kursdaten konnten nicht geladen werden");
                }
                */
            } else {
                Toast.makeText(this, "Keine gespeicherten Daten vorhanden", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Keine gespeicherten Daten vorhanden ", Toast.LENGTH_SHORT).show();
        }
    }
    private void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences(sharedPreferences_str, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        Gson gson = new Gson();
        String json = gson.toJson(courseArrayList);
        editor.putString(keyUnRatedCourseList, json);
        boolean saveFlag = editor.commit();
        if (saveFlag) {
            Toast.makeText(this, "Kursdaten gespeichert ", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Kursdatenspeicherung fehlgeschlagen", Toast.LENGTH_LONG).show();
        }
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
                    Toast.makeText(this, "Keine gespeicherten Einstellungen vorhanden", Toast.LENGTH_LONG).show();
                }

            } else {
                Toast.makeText(this, "Keine gespeicherten Einstellungen vorhanden", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            //Toast.makeText(RatingActivity.this, "Some error occurred while getting shared preferences " + e.getMessage(), Toast.LENGTH_LONG).show();
            Toast.makeText(MainActivity.this, "Keine gespeicherten Einstellungen vorhanden ", Toast.LENGTH_LONG).show();
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
    public void about(View view) {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }
    public void getFile(View view) {
        Intent intent = new Intent(this, FileActivity.class);
        //Log.v(TAG, "Intent fired ");
        startActivity(intent);
    }
    public void deleteCourse(View view) {
        if(!courseArrayList.isEmpty()) {
            deleteCourseFlag = true;
            Toast.makeText(this, "Bitte auf einen Kursnamen tippen", Toast.LENGTH_SHORT).show();
        }
    }
    public void deleteCourseAlertDialog(String courseName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Löschen von Kurs " + courseName + "?");
        builder.setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                        courseArrayList.remove(mPosition);
                        deleteCourseFlag = false;
                        Toast.makeText(MainActivity.this, "Kurs " + courseName + " wurde gelöschst", Toast.LENGTH_SHORT).show();
                        saveData();
                        bindCourseArrayList();
                    }
                });
        builder.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        Toast.makeText(MainActivity.this, "Löschung abgebrochen", Toast.LENGTH_SHORT).show();
                        deleteCourseFlag = false;
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    public void callDisplayRatings(String courseName) {
        displayRatingsFlag = false;
        Intent intent = new Intent(this, DisplayRatingsActivity.class);
        intent.putExtra(COURSENAMEMESSAGE, courseName);
        startActivity(intent);
    }
    public void displayRatings(View view) {
        if(!courseArrayList.isEmpty()) {
            displayRatingsFlag = true;
            Toast.makeText(this, "Bitte auf einen Kursnamen tippen", Toast.LENGTH_SHORT).show();
        }
    }
    public void callDisplayRatingStatistics(String courseName) {
        displayRatingStatisticsFlag = false;
        Intent intent = new Intent(this, DisplayRatingStatisticsActivity.class);
        intent.putExtra(COURSENAMEMESSAGE, courseName);
        startActivity(intent);
    }
    public void displayRatingStatistics(View view) {
        if(!courseArrayList.isEmpty()) {
            displayRatingStatisticsFlag = true;
            Toast.makeText(this, "Bitte auf einen Kursnamen tippen", Toast.LENGTH_SHORT).show();
        }
    }
}