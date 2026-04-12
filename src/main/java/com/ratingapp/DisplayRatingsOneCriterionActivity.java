package com.ratingapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ratingapp.databinding.ActivityDisplayRatingsOneCriterionBinding;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class DisplayRatingsOneCriterionActivity extends AppCompatActivity {
    private static final String TAG = "DisplayRatingsOneCriterionActivity";
    public static final String COURSENAMEMESSAGE = "com.ratingapp.COURSENAMEMESSAGE";
    public static final String SENDERMESSAGE = "com.ratingapp.SENDERMESSAGE";
    public final static String RATINGPERSTUDENTPARCEL_OBJECT = "RatingPerStudentParcelObject";
    private static final String keyAllRatedCourses = "allRatedCourses";
    private static final String sharedPreferences_str = "com.ratingapp.SHAREDPREFERENCES";
    private static final String sharedSettingsPreferences_str = "com.ratingapp.SettingsActivity.SHAREDPREFERENCES";
    private static final String keySettings = "SettingsKey";
    private static final String ratingPlusCircleMinus = "Plus-Kreis-Minus-Skala";
    private static final String ratingABCD = "ABCD-Notenskala";
    private static final String rating1till6 = "Notenskala von 1 bis 6";
    private TreeMap<String, TreeMap<String, ArrayList<RatingPerStudent>>> ratingPerStudentMap = new TreeMap<String, TreeMap<String, ArrayList<RatingPerStudent>>>();
    private TreeMap<String, TreeMap<String, TreeMap<Date, ArrayList<RatingPerStudent>>>> ratingPerStudentSortedMap = new TreeMap<String, TreeMap<String, TreeMap<Date, ArrayList<RatingPerStudent>>>>();
    public ArrayList<RatingPerStudent> ratingPerStudentArrayList;
    ArrayRatingPerStudentAdapter arrayRatingPerStudentAdapter;
    RatingPerStudentParcel ratingPerStudentParcel;
    ActivityDisplayRatingsOneCriterionBinding binding;
    private Settings settings;
    private TreeMap<String, Settings> settingsMap = new TreeMap<String, Settings>();
    boolean settingsExistFlag = false;
    RecyclerView recyclerView;
    private String courseNameParcel;
    private String nameParcel;
    private int criterionParcel;
    Date date;
    SimpleDateFormat sdF = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_ratings_one_criterion);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_display_ratings_one_criterion);
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(RATINGPERSTUDENTPARCEL_OBJECT)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ratingPerStudentParcel = intent.getParcelableExtra(RATINGPERSTUDENTPARCEL_OBJECT, RatingPerStudentParcel.class);
            }
            else {
                ratingPerStudentParcel = intent.getParcelableExtra(RATINGPERSTUDENTPARCEL_OBJECT);
            }
            binding.setData(ratingPerStudentParcel);
            courseNameParcel = "unbekannt";
            if (ratingPerStudentParcel != null) {
                courseNameParcel = ratingPerStudentParcel.getCourseName();
                String vorname = ratingPerStudentParcel.getVorname();
                String nachname = ratingPerStudentParcel.getNachname();
                nameParcel = nachname + " " + vorname;
                criterionParcel = ratingPerStudentParcel.getCriterion();
                //Log.i(TAG, "DROCA got course name " + courseNameParcel);

                loadSettings();

                if(settingsMap.containsKey(courseNameParcel)) {
                    settings = settingsMap.get(courseNameParcel);
                    settingsExistFlag = true;
                    //Log.v(TAG, "DROCA: Settings " + settings.getRatingScale() + " " + settings.getRatingAverage() + " " + settings.getNrOfCriteria());
                }
                else {
                    settingsExistFlag = false;
                    //Log.v(TAG, "Keine Einstellungen vorhanden " + courseNameParcel);
                    Toast.makeText(this, "Keine Einstellungen zum Kurs " + courseNameParcel + " vorhanden.", Toast.LENGTH_SHORT).show();
                }
            }
        }
        ratingPerStudentMap.clear();
        loadRatingPerStudentMap();
        ratingPerStudentArrayList = new ArrayList <RatingPerStudent>();
        if(ratingPerStudentMap != null && !ratingPerStudentMap.isEmpty()) {
            boolean chosenCourseRatingsExistFlag = false;
            for (Map.Entry<String, TreeMap<String, ArrayList<RatingPerStudent>>> mapIt : ratingPerStudentMap.entrySet()) {
                String courseName = mapIt.getKey();
                loadSettings();
                settings = settingsMap.get(courseName);
                boolean ratingScalePCMFlag = settings != null && settings.getRatingScale().equals(ratingPlusCircleMinus);
                for (Map.Entry<String, ArrayList<RatingPerStudent>> mapIt2 : mapIt.getValue().entrySet()) {
                    String name = mapIt2.getKey();
                    for (RatingPerStudent rps : mapIt2.getValue()) {
                        String crName = rps.getCourseName();
                        String vorname = rps.getVorname();
                        String nachname = rps.getNachname();
                        String date_str = rps.getDate();
                        int criterion = rps.getCriterion();
                        char rating = rps.getRating();
                        if(ratingScalePCMFlag) {
                            switch (rating) {
                                case 'A' -> rating = '+';
                                case 'B' -> rating = 'o';
                                case 'C' -> rating = '-';
                                default -> rating = ' ';
                            }
                        }
                        boolean flag = rps.getFlag();
                        if(!flag) {
                            rating = (char)32;
                        }
                        //Log.v(TAG, "DROCA: " + vorname +" " + nachname + " " + crName + " " + date_str + " " + criterion + " " + rating + " " + flag);
                        RatingPerStudent ratingPerStudent = new RatingPerStudent(courseName, vorname, nachname, date_str, criterion, rating, flag);

                        try {
                            date = sdF.parse(date_str);
                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }
                        if (ratingPerStudentSortedMap.containsKey(courseName)) {
                            if (!ratingPerStudentSortedMap.get(courseName).containsKey(name)) {
                                ratingPerStudentSortedMap.get(courseName).put(name, new TreeMap<Date, ArrayList<RatingPerStudent>>());
                                ratingPerStudentSortedMap.get(courseName).get(name).put(date, new ArrayList<RatingPerStudent>());
                            }
                            else {
                                if(!ratingPerStudentSortedMap.get(courseName).get(name).containsKey(date)) {
                                    ratingPerStudentSortedMap.get(courseName).get(name).put(date, new ArrayList<RatingPerStudent>());
                                }
                            }
                        } else {
                            ratingPerStudentSortedMap.put(courseName, new TreeMap<String, TreeMap<Date, ArrayList<RatingPerStudent>>>());
                            ratingPerStudentSortedMap.get(courseName).put(name, new TreeMap<Date, ArrayList<RatingPerStudent>>());
                            ratingPerStudentSortedMap.get(courseName).get(name).put(date, new ArrayList<RatingPerStudent>());
                        }
                        ratingPerStudentSortedMap.get(courseName).get(name).get(date).add(ratingPerStudent);
                    }
                }
            }
            for (Map.Entry<String, TreeMap<String, TreeMap<Date, ArrayList<RatingPerStudent>>>> mapIt : ratingPerStudentSortedMap.entrySet()) {
                String courseName = mapIt.getKey();
                for (Map.Entry<String, TreeMap<Date, ArrayList<RatingPerStudent>>> mapIt2 : mapIt.getValue().entrySet()) {
                    String name = mapIt2.getKey();
                    for (Map.Entry<Date, ArrayList<RatingPerStudent>> mapIt3 : mapIt2.getValue().entrySet()) {
                        Date date = mapIt3.getKey();
                        String date_str = sdF.format(date);
                        //Log.v(TAG, "DROCA2: " + courseName + " " + name + " " + date_str);
                        Comparator<RatingPerStudent> byValue = Comparator.comparing(RatingPerStudent::getCriterion);
                        mapIt3.getValue().sort(byValue);
                        if(courseName.equals(courseNameParcel)) {
                            chosenCourseRatingsExistFlag = true;
                            for (RatingPerStudent rps : mapIt3.getValue()) {
                                String crName = rps.getCourseName();
                                String vorname = rps.getVorname();
                                String nachname = rps.getNachname();
                                String name2 = nachname + " " + vorname;
                                String date_str2 = rps.getDate();
                                int criterion = rps.getCriterion();
                                char rating = rps.getRating();
                                boolean flag = rps.getFlag();
                                if(name2.equals(nameParcel) && criterion == criterionParcel && flag) {
                                    RatingPerStudent ratingPerStudent = new RatingPerStudent(courseName, vorname, nachname, date_str, criterion, rating, flag);
                                    ratingPerStudentArrayList.add(ratingPerStudent);
                                }
                            }
                        }
                    }
                }
            }
            if(!chosenCourseRatingsExistFlag) {
                Toast.makeText(DisplayRatingsOneCriterionActivity.this, "Noch keine Bewertung vorhanden ", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(DisplayRatingsOneCriterionActivity.this, "Keine gespeicherten Bewertungen vorhanden ", Toast.LENGTH_SHORT).show();
        }
        editRatingPerStudent();
        recyclerView = findViewById(R.id.recycler_id);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(arrayRatingPerStudentAdapter);
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
            Toast.makeText(DisplayRatingsOneCriterionActivity.this, "Keine gespeicherten Einstellungen vorhanden ", Toast.LENGTH_LONG).show();
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
            Toast.makeText(DisplayRatingsOneCriterionActivity.this, "Keine gespeicherten Daten vorhanden ", Toast.LENGTH_SHORT).show();
        }
    }
    private void editRatingPerStudent() {

        OnItemClickListener listener = new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                RatingPerStudent ratingPerStudent = ratingPerStudentArrayList.get(position);
                String courseName = ratingPerStudent.getCourseName();
                String vorname = ratingPerStudent.getVorname();
                String nachname = ratingPerStudent.getNachname();
                String date_str = ratingPerStudent.getDate();
                int criterion = ratingPerStudent.getCriterion();
                char rating = ratingPerStudent.getRating();
                boolean flag = ratingPerStudent.getFlag();
                RatingPerStudentParcel ratingPerStudentParcel
                        = new RatingPerStudentParcel(courseName, vorname, nachname, date_str, criterion, rating, flag);
                //Log.d("onclick", "DRA onClick position "
                //        + " " + position + " " + courseName + " " + vorname + " " + date_str + " " + criterion + " " + rating);

                Intent intent = new Intent(DisplayRatingsOneCriterionActivity.this, EditRatingActivity.class);
                intent.putExtra(RATINGPERSTUDENTPARCEL_OBJECT, ratingPerStudentParcel);
                String sender = "DisplayRatingsOneCriterionActivity";
                intent.putExtra(SENDERMESSAGE, sender);
                //Log.v(TAG, "Send RatingPerStudentParcel Object");
                startActivity(intent);
            }
        };
        arrayRatingPerStudentAdapter =
                new ArrayRatingPerStudentAdapter(R.layout.rating_row, ratingPerStudentArrayList, this, listener);
    }
    public void goHome(View view){
        Intent intent = new Intent(this, DisplayRatingStatisticsActivity.class);
        intent.putExtra(COURSENAMEMESSAGE, courseNameParcel);
        startActivity(intent);
    }
}
