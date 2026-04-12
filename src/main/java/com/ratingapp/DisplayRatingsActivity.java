package com.ratingapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import com.ratingapp.databinding.ActivityDisplayRatingsBinding;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.Date;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;

public class DisplayRatingsActivity extends AppCompatActivity {
    private static final String TAG = "DisplayRatingsActivity";
    private static final String sharedPreferences_str = "com.ratingapp.SHAREDPREFERENCES";
    private static final String keyAllRatedCourses = "allRatedCourses";
    private static final String sharedSettingsPreferences_str = "com.ratingapp.SettingsActivity.SHAREDPREFERENCES";
    private static final String keySettings = "SettingsKey";
    private static final String ratingPlusCircleMinus = "Plus-Kreis-Minus-Skala";
    private static final String COURSENAMEMESSAGE = "com.ratingapp.COURSENAMEMESSAGE";
    public final static String RATINGPERSTUDENTPARCEL_OBJECT = "RatingPerStudentParcelObject";
    ActivityDisplayRatingsBinding binding;
    private String courseNameChosenInMain = "";
    Course course;
    private Settings settings;
    private TreeMap<String, Settings> settingsMap = new TreeMap<String, Settings>();
    RecyclerView recyclerView;
    SAFFileManager fileManager = null;
    private TreeMap<String, TreeMap<String, ArrayList<RatingPerStudent>>> ratingPerStudentMap = new TreeMap<String, TreeMap<String, ArrayList<RatingPerStudent>>>();
    private TreeMap<String, TreeMap<String, TreeMap<Date, ArrayList<RatingPerStudent>>>> ratingPerStudentSortedMap = new TreeMap<String, TreeMap<String, TreeMap<Date, ArrayList<RatingPerStudent>>>>();
    public ArrayList<RatingPerStudent> ratingPerStudentArrayList;
    ArrayRatingPerStudentAdapter arrayRatingPerStudentAdapter;
    String students_str = "";
    boolean dirChosen = false;
    Date date;
    SimpleDateFormat sdF = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_ratings);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_display_ratings);
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra(getString(R.string.appMessage))) {
                String courseName = intent.getStringExtra(COURSENAMEMESSAGE);
                //Log.i(TAG, "DRA Got course name " + courseName);
                courseNameChosenInMain = courseName;
                course = new Course(courseNameChosenInMain);
                binding.setCourse(course);
            }
        }

        final StorageAccessManager storageAccessManager = new StorageAccessManager(this);

        try {
            SharedPreferences sharedPreferences = getSharedPreferences(sharedPreferences_str, MODE_PRIVATE);
            if (sharedPreferences != null) {
                Gson gson = new Gson();
                String json = sharedPreferences.getString(keyAllRatedCourses, null);
                Type type = new TypeToken<TreeMap<String, TreeMap<String, ArrayList<RatingPerStudent>>>>() {}.getType();
                ratingPerStudentMap = gson.fromJson(json, type);
                /*
                if (ratingPerStudentMap != null && !ratingPerStudentMap.isEmpty()) {
                    Toast.makeText(this, "Liste aller Kursdaten geladen ", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Liste aller Kursdaten konnte nicht geladen werden", Toast.LENGTH_SHORT).show();
                    Log.v(TAG, "Liste aller Kursdaten konnte nicht geladen werden");
                }
                */
            } else {
                Toast.makeText(this, "Keine Liste gespeicherter Kursdaten vorhanden", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            //Toast.makeText(MainActivity.this, "Some error occurred while getting shared preferences " + e.getMessage(), Toast.LENGTH_LONG).show();
            Toast.makeText(DisplayRatingsActivity.this, "Keine Liste gespeicherter Daten vorhanden ", Toast.LENGTH_LONG).show();
        }

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
                        //Log.v(TAG, "DR: " + vorname +" " + nachname + " " + crName + " " + date_str + " " + criterion + " " + rating + " " + flag);
                        RatingPerStudent ratingPerStudent = new RatingPerStudent(courseName, vorname, nachname, date_str, criterion, rating, flag);
                        /*
                        if(crName.equals(courseNameChosenInMain)) {
                            //Log.v(TAG, "DR: " + vorname +" " + nachname + " " + crName + " " + date_str + " " + criterion + " " + rating + " " + flag);
                            ratingPerStudentArrayList.add(ratingPerStudent);
                            chosenCourseRatingsExistFlag = true;
                        }
                        */
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
                        //Log.v(TAG, "DR2: " + courseName + " " + name + " " + date_str);
                        Comparator<RatingPerStudent> byValue = Comparator.comparing(RatingPerStudent::getCriterion);
                        mapIt3.getValue().sort(byValue);
                        if(courseName.equals(courseNameChosenInMain)) {
                            ratingPerStudentArrayList.addAll(mapIt3.getValue());
                            chosenCourseRatingsExistFlag = true;
                        }
                    }
                }
            }
            if(!chosenCourseRatingsExistFlag) {
                Toast.makeText(DisplayRatingsActivity.this, "Noch keine Bewertung vorhanden ", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(DisplayRatingsActivity.this, "Keine gespeicherten Bewertungen vorhanden ", Toast.LENGTH_SHORT).show();
        }
        editRatingPerStudent();
        recyclerView = findViewById(R.id.recycler_id);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(arrayRatingPerStudentAdapter);

        findViewById(R.id.write_into_file).setOnClickListener(l -> {

            storageAccessManager.openDestinationChooser(new StorageAccessManager.DestinationSelectionListener() {
                @Override
                public void onDestinationSelected(int status, Uri destination) {
                    fileManager = new SAFFileManager(DisplayRatingsActivity.this, destination);
                    //Log.v(TAG, "directory chosen");
                    if (ratingPerStudentMap != null && !ratingPerStudentMap.isEmpty()) {
                        for (Map.Entry<String, TreeMap<String, ArrayList<RatingPerStudent>>> mapIt : ratingPerStudentMap.entrySet()) {
                            String courseName = mapIt.getKey();
                            String courseName2 = courseName.replaceAll(",", "_");
                            courseName2 = courseName2.replaceAll(" ", "_");
                            courseName2 = courseName2.replaceAll("/", "_");
                            String filePath = "Bewertungen_" + courseName2 + ".csv";
                            //Log.v(TAG, "file path " + filePath);
                            loadSettings();
                            settings = settingsMap.get(courseName);
                            boolean ratingScalePCMFlag = settings != null && settings.getRatingScale().equals(ratingPlusCircleMinus);
                            students_str = "";
                            for (Map.Entry<String, ArrayList<RatingPerStudent>> mapIt2 : mapIt.getValue().entrySet()) {
                                String name = mapIt2.getKey();
                                for (RatingPerStudent rps : mapIt2.getValue()) {
                                    String courseName3 = rps.getCourseName();
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
                                    String st_str = String.format("%s\u003B%s\u003B%s\u003B%s\u003B%d\u003B%c\u003B%b%n",
                                            vorname, nachname, courseName3, date_str, criterion, rating, flag);
                                    //String st_str = String.format("%s\u003B%s\u003B%s\u003B%s\u003B%d\u003B%c%n",
                                    //        vorname, nachname, courseName3, date_str, criterion, rating);
                                    students_str += st_str;
                                }
                            }
                            String content = students_str;
                            try {
                                fileManager.writeFile(filePath, content.getBytes(StandardCharsets.UTF_8), false);
                                //Log.v(TAG, "file saved" + filePath);
                                Toast.makeText(DisplayRatingsActivity.this, "Datei " + filePath + " gespeichert", Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                Log.e(TAG, "Some error occurred while writing content to file ", e);
                                Toast.makeText(DisplayRatingsActivity.this, "Some error occurred while writing content to file " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    } else {
                        Toast.makeText(DisplayRatingsActivity.this, "keine Bewertungen vorhanden", Toast.LENGTH_LONG).show();
                    }
                }
            });
        });
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

                Intent intent = new Intent(DisplayRatingsActivity.this, EditRatingActivity.class);
                intent.putExtra(RATINGPERSTUDENTPARCEL_OBJECT, ratingPerStudentParcel);
                //Log.v(TAG, "Send RatingPerStudentParcel Object");
                startActivity(intent);
            }
        };
        arrayRatingPerStudentAdapter =
                new ArrayRatingPerStudentAdapter(R.layout.rating_row, ratingPerStudentArrayList, this, listener);
    }
    public void deleteAllRatingsPerStudent(View view) {
        /*
        SharedPreferences sharedPreferences = getSharedPreferences(sharedPreferences_str, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        Gson gson = new Gson();
        ratingPerStudentMap.clear();
        String jsonRatingPerStudentMap = gson.toJson(ratingPerStudentMap);
        editor.putString(keyAllRatedCourses, jsonRatingPerStudentMap);
        boolean deleteFlag = editor.commit();
        if (deleteFlag) {
            Toast.makeText(this, "Alle Bewertungen gelöscht ", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Löschung fehlgeschlagen", Toast.LENGTH_LONG).show();
        }
        */
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Löschen aller Bewertungen zu " + courseNameChosenInMain + "?");
        builder.setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                        SharedPreferences sharedPreferences = getSharedPreferences(sharedPreferences_str, MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.clear();
                        Gson gson = new Gson();
                        //ratingPerStudentMap.clear();
                        for (Map.Entry<String, TreeMap<String, ArrayList<RatingPerStudent>>> mapIt : ratingPerStudentMap.entrySet()) {
                            String courseName = mapIt.getKey();
                            if(courseName.equals(courseNameChosenInMain)) {
                                mapIt.getValue().clear();
                            }
                        }
                        String jsonRatingPerStudentMap = gson.toJson(ratingPerStudentMap);
                        editor.putString(keyAllRatedCourses, jsonRatingPerStudentMap);
                        boolean deleteFlag = editor.commit();
                        if (deleteFlag) {
                            Toast.makeText(DisplayRatingsActivity.this, "Alle Bewertungen zu " + courseNameChosenInMain + " gelöscht ", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(DisplayRatingsActivity.this, DisplayRatingsActivity.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(DisplayRatingsActivity.this, "Löschung fehlgeschlagen", Toast.LENGTH_LONG).show();
                        }
                    }
                });
        builder.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        Toast.makeText(DisplayRatingsActivity.this, "Löschung abgebrochen", Toast.LENGTH_SHORT).show();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    /*
    public void displayRatingStatistics(View view) {
        // Do something in response to button
        Intent intent = new Intent(this, DisplayRatingStatisticsActivity.class);
        Log.v(TAG, "Intent for DRSA fired ");
        startActivity(intent);
    }
    */
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
                    Log.v(TAG, "Einstellungen konnten nicht geladen werden");
                    Toast.makeText(this, "Einstellungen konnten nicht geladen werden", Toast.LENGTH_LONG).show();
                }

            } else {
                Toast.makeText(this, "Keine gespeicherten Einstellungen vorhanden", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            //Toast.makeText(RatingActivity.this, "Some error occurred while getting shared preferences " + e.getMessage(), Toast.LENGTH_LONG).show();
            Toast.makeText(DisplayRatingsActivity.this, "Keine gespeicherten Einstellungen vorhanden ", Toast.LENGTH_LONG).show();
        }
    }
    public void goHome(View view) {
        // go back to main page  in response to button
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
