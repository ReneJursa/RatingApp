package com.ratingapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ratingapp.databinding.ActivityEditRatingBinding;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Objects;
import java.util.TreeMap;

public class EditRatingActivity extends AppCompatActivity {
    private static final String TAG = "EditRatingActivity";
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
    RatingPerStudentParcel ratingPerStudentParcel;
    private Settings settings;
    boolean settingsExistFlag = false;
    ActivityEditRatingBinding binding;
    private TreeMap<String, Settings> settingsMap = new TreeMap<String, Settings>();
    String courseName;
    private String sender = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_rating);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_rating);
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(RATINGPERSTUDENTPARCEL_OBJECT)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ratingPerStudentParcel = intent.getParcelableExtra(RATINGPERSTUDENTPARCEL_OBJECT, RatingPerStudentParcel.class);
            }
            else {
                ratingPerStudentParcel = intent.getParcelableExtra(RATINGPERSTUDENTPARCEL_OBJECT);
            }
            binding.setData(ratingPerStudentParcel);
            courseName = "unbekannt";
            if (ratingPerStudentParcel != null) {
                courseName = ratingPerStudentParcel.getCourseName();
                //Log.i(TAG, "Edit Rating got course name " + courseName);

                loadSettings();

                if(settingsMap.containsKey(courseName)) {
                    settings = settingsMap.get(courseName);
                    settingsExistFlag = true;
                    //Log.v(TAG, "ERA: Settings " + settings.getRatingScale() + " " + settings.getRatingAverage() + " " + settings.getNrOfCriteria());
                }
                else {
                    settingsExistFlag = false;
                    //Log.v(TAG, "Keine Einstellungen vorhanden " + courseName);
                    Toast.makeText(this, "Keine Einstellungen zum Kurs " + courseName + " vorhanden.", Toast.LENGTH_SHORT).show();
                }
            }
        }
        if (intent != null && intent.hasExtra(getString(R.string.senderMessage))) {
            sender = intent.getStringExtra(SENDERMESSAGE);
        }
        findViewById(R.id.save_rating).setOnClickListener(l -> {
            if(settingsExistFlag) {
                ratingPerStudentMap.clear();
                loadRatingPerStudentMap();
                char rating_imported = (char) 32;
                String ratingScale_imported = "";
                String ratingScale_saved = ratingABCD;
                ratingScale_saved = settings.getRatingScale();
                String rating_str = ((TextView) findViewById(R.id.RatingValue)).getText().toString();
                boolean validRating = false;
                if (!rating_str.isBlank()) {
                    rating_imported = rating_str.trim().charAt(0);
                    //Log.v(TAG, "Eingelesene Bewertung " + rating_imported);

                    if (rating_imported == '+' || rating_imported == 'o' || rating_imported == '-') {
                        validRating = true;
                        ratingScale_imported = ratingPlusCircleMinus;
                        if (rating_imported == '+') {
                            rating_imported = 'A';
                        } else if (rating_imported == 'o') {
                            rating_imported = 'B';
                        } else {
                            rating_imported = 'C';
                        }
                    } else if (rating_imported == 'A' || rating_imported == 'B' || rating_imported == 'C' || rating_imported == 'D') {
                        validRating = true;
                        ratingScale_imported = ratingABCD;
                    } else if (Character.isDigit(rating_imported)) {
                        int i = Character.getNumericValue(rating_imported);
                        if (0 < i && i < 7) {
                            validRating = true;
                            ratingScale_imported = rating1till6;
                        }
                    }

                    String courseName_p = ratingPerStudentParcel.getCourseName();
                    String vorname_p = ratingPerStudentParcel.getVorname();
                    String nachname_p = ratingPerStudentParcel.getNachname();
                    String name_p = nachname_p + " " + vorname_p;
                    String date_str_p = ratingPerStudentParcel.getDate();
                    int criterion_p = ratingPerStudentParcel.getCriterion();
                    char rating_p = ratingPerStudentParcel.getRating();

                    if (rating_p == '+' || rating_p == 'o' || rating_p == '-') {
                        if (rating_p == '+') {
                            rating_p = 'A';
                        } else if (rating_p == 'o') {
                            rating_p = 'B';
                        } else {
                            rating_p = 'C';
                        }
                    }
                    //Log.v(TAG, "parcel " + courseName_p + " " + name_p + " " + date_str_p + " " + criterion_p + " " + rating_p);
                    if (ratingScale_imported.equals(ratingScale_saved) && validRating) {
                        //Log.v(TAG, "ratingScale_saved: " + ratingScale_saved);
                        if (ratingPerStudentMap.containsKey(courseName_p) && Objects.requireNonNull(ratingPerStudentMap.get(courseName_p)).containsKey(name_p)) {
                            for (RatingPerStudent rps : Objects.requireNonNull(Objects.requireNonNull(ratingPerStudentMap.get(courseName_p)).get(name_p))) {
                                String courseName = rps.getCourseName();
                                String vorname = rps.getVorname();
                                String nachname = rps.getNachname();
                                String name = nachname + " " + vorname;
                                String date_str = rps.getDate();
                                int criterion = rps.getCriterion();
                                char rating2 = rps.getRating();
                                boolean flag = rps.getFlag();
                                //Log.v(TAG, "gefunden 1 " + courseName + " " + vorname + " " + nachname + " " + date_str + " " + criterion + " " + rating2);
                                if (date_str.equals(date_str_p) && criterion == criterion_p && rating2 == rating_p) {
                                    rps.setRating(rating_imported);
                                    rps.setFlag(true);
                                    //Log.v(TAG, "gefunden " + courseName + " " + vorname + " " + nachname + " " + date_str + " " + criterion + " " + rating2);
                                    //Log.v(TAG, "Bewertung geändert auf " + rating_imported);
                                    ratingPerStudentParcel.setRating(rating_imported);
                                    binding.setData(ratingPerStudentParcel);
                                    break;
                                }
                            }
                            SharedPreferences sharedPreferences = getSharedPreferences(sharedPreferences_str, MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.clear();
                            Gson gson = new Gson();
                            String jsonRatingPerStudentMap = gson.toJson(ratingPerStudentMap);
                            editor.putString(keyAllRatedCourses, jsonRatingPerStudentMap);
                            boolean saveFlag = editor.commit();
                            if (saveFlag) {
                                Toast.makeText(this, "Geänderte Bewertung gespeichert ", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "Speicherung der Änderung fehlgeschlagen", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        //Log.v(TAG,"Eingelesene Bewertung nicht gültig.");
                        Toast.makeText(EditRatingActivity.this, "Die eingegebene Bewertung passt nicht zur eingestellten Bewertungsskala", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(EditRatingActivity.this, "Bewertung nicht geändert", Toast.LENGTH_SHORT).show();
                }
            }
            else {
                //Log.v(TAG, "Keine Einstellungen vorhanden " + courseName);
                Toast.makeText(this, "Keine Einstellungen zum Kurs " + courseName + " vorhanden.", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.delete_rating).setOnClickListener(l -> {
            ratingPerStudentMap.clear();
            loadRatingPerStudentMap();
            String courseName_p = ratingPerStudentParcel.getCourseName();
            String vorname_p = ratingPerStudentParcel.getVorname();
            String nachname_p = ratingPerStudentParcel.getNachname();
            String name_p = nachname_p + " " + vorname_p;
            String date_str_p = ratingPerStudentParcel.getDate();
            int criterion_p = ratingPerStudentParcel.getCriterion();
            char rating_p = ratingPerStudentParcel.getRating();
            if(rating_p == '+' || rating_p == 'o' || rating_p == '-') {
                if (rating_p == '+') {
                    rating_p = 'A';
                } else if (rating_p == 'o') {
                    rating_p = 'B';
                } else {
                    rating_p = 'C';
                }
            }
            //Log.v(TAG, "parcel " + courseName_p + " " + name_p + " " + date_str_p + " " + criterion_p + " " + rating_p);

            if (ratingPerStudentMap.containsKey(courseName_p) && Objects.requireNonNull(ratingPerStudentMap.get(courseName_p)).containsKey(name_p)) {

                for (RatingPerStudent rps: Objects.requireNonNull(Objects.requireNonNull(ratingPerStudentMap.get(courseName_p)).get(name_p))) {
                    String courseName = rps.getCourseName();
                    String vorname = rps.getVorname();
                    String nachname = rps.getNachname();
                    String name = nachname + " " + vorname;
                    String date_str = rps.getDate();
                    int criterion = rps.getCriterion();
                    char rating2 = rps.getRating();
                    boolean flag = rps.getFlag();
                    //Log.v(TAG, "gefunden 1 " + courseName + " " + vorname + " " + nachname + " " + date_str + " " + criterion + " " + rating2);
                    if (date_str.equals(date_str_p) && criterion == criterion_p && rating2 == rating_p) {
                        rps.setRating((char)32);
                        rps.setFlag(false);
                        //Log.v(TAG, "gefunden " + courseName + " " + vorname + " " + nachname + " " + date_str + " " + criterion + " " + rating2);
                        //Log.v(TAG, "Bewertung gelöscht");
                        ratingPerStudentParcel.setRating((char)32);
                        binding.setData(ratingPerStudentParcel);
                        break;
                    }
                }
                SharedPreferences sharedPreferences = getSharedPreferences(sharedPreferences_str, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                Gson gson = new Gson();
                String jsonRatingPerStudentMap = gson.toJson(ratingPerStudentMap);
                editor.putString(keyAllRatedCourses, jsonRatingPerStudentMap);
                boolean saveFlag = editor.commit();
                if (saveFlag) {
                    Toast.makeText(this, "Löschung der Bewertung gespeichert", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Speicherung der Löschung fehlgeschlagen", Toast.LENGTH_SHORT).show();
                }
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
                    Toast.makeText(this, "Keine gespeicherten Einstellungen vorhanden", Toast.LENGTH_LONG).show();
                }

            } else {
                Toast.makeText(this, "Keine gespeicherten Einstellungen vorhanden", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(EditRatingActivity.this, "Keine gespeicherten Einstellungen vorhanden ", Toast.LENGTH_LONG).show();
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
            Toast.makeText(EditRatingActivity.this, "Keine gespeicherten Daten vorhanden ", Toast.LENGTH_SHORT).show();
        }
    }
    public void goHome(View view){
        Intent intent;
        if(!sender.isBlank() && sender.equals("DisplayRatingsOneCriterionActivity")) {
            intent = new Intent(this, DisplayRatingStatisticsActivity.class);
        }
        else {
            intent = new Intent(this, DisplayRatingsActivity.class);
        }
        intent.putExtra(COURSENAMEMESSAGE, courseName);
        startActivity(intent);
    }
}
