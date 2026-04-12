package com.ratingapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import com.ratingapp.databinding.ActivityDisplayRatingStatisticsBinding;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class DisplayRatingStatisticsActivity extends AppCompatActivity {
    private static final String TAG = "DisplayRatingStatisticsActivity";
    private static final String sharedPreferences_str = "com.ratingapp.SHAREDPREFERENCES";
    private static final String keyAllRatedCourses = "allRatedCourses";
    private static final String sharedSettingsPreferences_str = "com.ratingapp.SettingsActivity.SHAREDPREFERENCES";
    private static final String keySettings = "SettingsKey";
    private static final String ratingPlusCircleMinus = "Plus-Kreis-Minus-Skala";
    private static final String COURSENAMEMESSAGE = "com.ratingapp.COURSENAMEMESSAGE";
    public final static String RATINGPERSTUDENTPARCEL_OBJECT = "RatingPerStudentParcelObject";
    ActivityDisplayRatingStatisticsBinding binding;
    private String courseNameChosenInMain = "";
    Course course;
    RecyclerView recyclerView;
    SAFFileManager fileManager = null;
    private TreeMap<String, TreeMap<String, ArrayList<RatingPerStudent>>> ratingPerStudentMap = new TreeMap<String, TreeMap<String, ArrayList<RatingPerStudent>>>();
    private final TreeMap<String, TreeMap<Integer, ArrayList<RatingPerStudent>>> ratingStatisticsMap = new TreeMap<String, TreeMap<Integer, ArrayList<RatingPerStudent>>>();
    private final TreeMap<String, TreeMap<String, ArrayList<EndRatingPerStudent>>> existingRatingPerStudentMap = new TreeMap<String, TreeMap<String, ArrayList<EndRatingPerStudent>>>();
    private final TreeMap<String, TreeMap<String, ArrayList<EndRatingPerStudent>>> endRatingPerStudentMap = new TreeMap<String, TreeMap<String, ArrayList<EndRatingPerStudent>>>();
    private final TreeMap<String, TreeMap<String, ArrayList<EndRatingPerStudent>>> courseNameMap = new TreeMap<String, TreeMap<String, ArrayList<EndRatingPerStudent>>>();
    ArrayList<Integer> criterionArrayList = new ArrayList<Integer>();
    ArrayList<Integer> existingCriterionArrayList = new ArrayList<Integer>();
    public ArrayList<EndRatingPerStudent> endRatingPerStudentArrayList;
    ArrayEndRatingPerStudentAdapter arrayEndRatingPerStudentAdapter;
    String students_str = "";
    private Settings settings;
    private TreeMap<String, Settings> settingsMap = new TreeMap<String, Settings>();
    private static final String arithmeticMean_str = "ArithmeticMean";
    private static final String median_str = "Median";
    String ratingAverage = arithmeticMean_str;
    boolean dirChosen = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_display_rating_statistics);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_display_rating_statistics);
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra(getString(R.string.appMessage))) {
                String courseName = intent.getStringExtra(COURSENAMEMESSAGE);
                //Log.i(TAG, "DRSA Got course name " + courseName);
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
                    //Toast.makeText(this, "Liste aller Kursdaten konnte nicht geladen werden", Toast.LENGTH_SHORT).show();
                    Log.v(TAG, "Liste aller Kursdaten konnte nicht geladen werden");
                }
                */
            } else {
                Toast.makeText(this, "Keine Liste gespeicherter Kursdaten vorhanden", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            //Toast.makeText(MainActivity.this, "Some error occurred while getting shared preferences " + e.getMessage(), Toast.LENGTH_LONG).show();
            Toast.makeText(this, "Keine Liste gespeicherter Daten vorhanden ", Toast.LENGTH_LONG).show();
        }

        if(ratingPerStudentMap != null && !ratingPerStudentMap.isEmpty()) {

            for (Map.Entry<String, TreeMap<String, ArrayList<RatingPerStudent>>> mapIt : ratingPerStudentMap.entrySet()) {
                String crName = mapIt.getKey();
                ratingAverage = arithmeticMean_str;
                loadSettings();
                settings = settingsMap.get(crName);
                if(settings != null) {
                    if (settings.getRatingAverage().equals(median_str)) {
                        ratingAverage = median_str;
                    }
                    else if (settings.getRatingAverage().equals(arithmeticMean_str)) {
                        ratingAverage = arithmeticMean_str;
                    }
                    else {
                        Log.e(TAG, "Fehler in DRSA: für Kurs " + crName + " gibt es keine Einstellung zum Mittelwert");
                        Toast.makeText(DisplayRatingStatisticsActivity.this, "Fehler: Für Kurs " + crName + " gibt es keine Einstellung zum Mittelwert", Toast.LENGTH_LONG).show();
                        ratingAverage = arithmeticMean_str;
                    }
                }
                else {
                    Log.e(TAG, "Fehler in DRSA: für Kurs " + crName + " gibt es keine Einstellungen");
                    Toast.makeText(DisplayRatingStatisticsActivity.this, "Fehler: Für Kurs " + crName + " gibt es keine Einstellungen", Toast.LENGTH_LONG).show();
                    ratingAverage = arithmeticMean_str;
                }

                //Log.v(TAG, "DRSA: Settings " + crName + " ratAvg "  + ratingAverage + " " + settings.getRatingScale() + " " + settings.getRatingAverage() + " " + settings.getNrOfCriteria());

                ratingStatisticsMap.clear();
                for (Map.Entry<String, ArrayList<RatingPerStudent>> mapIt2 : mapIt.getValue().entrySet()) {
                    String name = mapIt2.getKey();

                    for (RatingPerStudent rps : mapIt2.getValue()) {
                        String courseName = rps.getCourseName();
                        String vorname = rps.getVorname();
                        String nachname = rps.getNachname();
                        String name2 = nachname + " " + vorname;
                        String date_str = rps.getDate();
                        int criterion = rps.getCriterion();
                        char rating = rps.getRating();
                        boolean flag = rps.getFlag();
                        if (!criterionArrayList.contains(criterion)) {
                            criterionArrayList.add(criterion);
                        }
                        //Log.v(TAG, "DRSA: " + vorname +" " + nachname + " " + courseName + " " + date_str + " " + criterion + " " + rating + " " + flag);
                        if(flag) {
                            /*
                            if (ratingStatisticsMap.containsKey(name)) {
                                if (ratingStatisticsMap.get(name).containsKey(criterion)) {
                                    ratingStatisticsMap.get(name).get(criterion).add(new RatingPerStudent(courseName, vorname, nachname, date_str, criterion, rating, flag));
                                    //Log.v(TAG, "DRSA 1: " + name + " " + date_str + " " + criterion + " " + rating + " " + flag);
                                } else {
                                    ratingStatisticsMap.get(name).put(criterion, new ArrayList<RatingPerStudent>());
                                    ratingStatisticsMap.get(name).get(criterion).add(new RatingPerStudent(courseName, vorname, nachname, date_str, criterion, rating, flag));
                                    //Log.v(TAG, "DRSA 2: " + name + " " + date_str + " " + criterion + " " + rating + " " + flag);
                                }
                            } else {
                                ratingStatisticsMap.put(name, new TreeMap<Integer, ArrayList<RatingPerStudent>>());
                                ratingStatisticsMap.get(name).put(criterion, new ArrayList<RatingPerStudent>());
                                ratingStatisticsMap.get(name).get(criterion).add(new RatingPerStudent(courseName, vorname, nachname, date_str, criterion, rating, flag));
                                //Log.v(TAG, "DRSA 3: " + name + " " + date_str + " " + criterion + " " + rating + " " + flag);
                            }
                            */
                            if (ratingStatisticsMap.containsKey(name2)) {
                                if (!ratingStatisticsMap.get(name2).containsKey(criterion)) {
                                    ratingStatisticsMap.get(name2).put(criterion, new ArrayList<RatingPerStudent>());
                                }
                            }
                            else {
                                ratingStatisticsMap.put(name2, new TreeMap<Integer, ArrayList<RatingPerStudent>>());
                                ratingStatisticsMap.get(name2).put(criterion, new ArrayList<RatingPerStudent>());
                            }
                            ratingStatisticsMap.get(name).get(criterion).add(new RatingPerStudent(courseName, vorname, nachname, date_str, criterion, rating, flag));
                            //Log.v(TAG, "DRSA 3: " + name + " " + date_str + " " + criterion + " " + rating + " " + flag);
                        }
                        if(courseNameMap.containsKey(courseName)) {
                            if (!courseNameMap.get(courseName).containsKey(name2)) {
                                courseNameMap.get(courseName).put(name2, new ArrayList<EndRatingPerStudent>());
                            }
                        }
                        else {
                            courseNameMap.put(courseName, new TreeMap<String, ArrayList<EndRatingPerStudent>>());
                            courseNameMap.get(courseName).put(name2, new ArrayList<EndRatingPerStudent>());
                        }
                        courseNameMap.get(courseName).get(name2).add(new EndRatingPerStudent(courseName, vorname, nachname, date_str, criterion, rating, flag, 0));
                        //Log.v(TAG, "rM2: " + name + " " + date_str + " " + criterion + " " + rating + " " + flag);
                    }
                }
                SimpleDateFormat sdF = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);
                Date date = new Date();
                String date_str = sdF.format(date);
                for (Map.Entry<String, TreeMap<Integer, ArrayList<RatingPerStudent>>> mapIt2 : ratingStatisticsMap.entrySet()) {
                    //String name = mapIt2.getKey();
                    for (Map.Entry<Integer, ArrayList<RatingPerStudent>> mapIt3 : mapIt2.getValue().entrySet()) {
                        int criterion = mapIt3.getKey();
                        Comparator<RatingPerStudent> byValue = Comparator.comparing(RatingPerStudent::getRating);
                        mapIt3.getValue().sort(byValue);
                        int sizeOfArray = mapIt3.getValue().size();
                        int sizeDivBy2 = sizeOfArray / 2;
                        char medianRating;
                        if(ratingAverage.equals(median_str)) {
                            if (mapIt3.getValue().size() % 2 == 0) {
                                int v1 = mapIt3.getValue().get(sizeDivBy2).getRating();
                                int v2 = mapIt3.getValue().get(sizeDivBy2 - 1).getRating();
                                int avg = (int) ((v1 + v2) / 2.0 + 0.5);
                                medianRating = (char) avg;
                                //Log.v(TAG, "DRSA 3: " + v1  + " " + v2 + " " + criterion + " " + medianRating + " " + sizeOfArray);
                            } else {
                                medianRating = mapIt3.getValue().get(sizeDivBy2).getRating();
                            }
                        }
                        else {
                            double sum = 0.0;
                            for (RatingPerStudent rps : mapIt3.getValue()) {
                                //Log.v(TAG, "DRSA: " + " rating " + rps.getRating());
                                sum += rps.getRating();
                            }
                            //double mean_d = sum / (double)mapIt3.getValue().size();
                            int mean = (int)(sum / (double)mapIt3.getValue().size() + 0.5);
                            //Log.v(TAG, "DRSA: " + " sum " + sum + " size " + mapIt3.getValue().size() + " mean_d " + mean_d + " mean " + mean);
                            medianRating = (char)mean;
                        }
                        String courseName = mapIt3.getValue().get(0).getCourseName();
                        String vorname = mapIt3.getValue().get(0).getVorname();
                        String nachname = mapIt3.getValue().get(0).getNachname();
                        String name = nachname + " " + vorname;
                        //Log.v(TAG, "DRSA 4: " + courseName + " " + criterion + " " + medianRating + " " + sizeOfArray);
                        boolean flag = false;
                        if(sizeOfArray > 0) {
                            flag = true;
                        }

                        if(existingRatingPerStudentMap.containsKey(courseName)) {
                            if (existingRatingPerStudentMap.get(courseName).containsKey(name)) {
                                existingRatingPerStudentMap.get(courseName).get(name).add(new EndRatingPerStudent(courseName, vorname, nachname, date_str, criterion, medianRating, flag, sizeOfArray));
                                //Log.v(TAG, "DRSA 1a:  " + name + " " + date_str + " " + criterion + " " + medianRating + " " + flag + " " + sizeOfArray);
                            }
                            else {
                                existingRatingPerStudentMap.get(courseName).put(name, new ArrayList<EndRatingPerStudent>());
                                existingRatingPerStudentMap.get(courseName).get(name).add(new EndRatingPerStudent(courseName, vorname, nachname, date_str, criterion, medianRating, flag, sizeOfArray));
                                //Log.v(TAG, "DRSA 2a:  " + name + " " + date_str + " " + criterion + " " + medianRating + " " + flag + " " + sizeOfArray);
                            }
                        }
                        else {
                            existingRatingPerStudentMap.put(courseName, new TreeMap<String, ArrayList<EndRatingPerStudent>>());
                            existingRatingPerStudentMap.get(courseName).put(name, new ArrayList<EndRatingPerStudent>());
                            existingRatingPerStudentMap.get(courseName).get(name).add(new EndRatingPerStudent(courseName, vorname, nachname, date_str, criterion, medianRating, flag, sizeOfArray));
                            //Log.v(TAG, "DRSA 3a:  " + name + " " + date_str + " " + criterion + " " + medianRating + " " + flag + " " + sizeOfArray);
                        }
                    }
                }
            }

            for (Map.Entry<String, TreeMap<String, ArrayList<EndRatingPerStudent>>> mapIt : courseNameMap.entrySet()) {
                String courseName = mapIt.getKey();
                for (Map.Entry<String, ArrayList<EndRatingPerStudent>> mapIt2 : mapIt.getValue().entrySet()) {
                    String name = mapIt2.getKey();

                    if (endRatingPerStudentMap.containsKey(courseName)) {
                        if (!endRatingPerStudentMap.get(courseName).containsKey(name)) {
                            endRatingPerStudentMap.get(courseName).put(name, new ArrayList<EndRatingPerStudent>());
                        }
                    } else {
                        endRatingPerStudentMap.put(courseName, new TreeMap<String, ArrayList<EndRatingPerStudent>>());
                        endRatingPerStudentMap.get(courseName).put(name, new ArrayList<EndRatingPerStudent>());
                    }
                    if (existingRatingPerStudentMap.get(courseName).containsKey(name)) {
                        existingCriterionArrayList.clear();

                        for (EndRatingPerStudent rps : existingRatingPerStudentMap.get(courseName).get(name)) {
                            String crName = rps.getCourseName();
                            String vorname = rps.getVorname();
                            String nachname = rps.getNachname();
                            String date_str = rps.getDate();
                            int criterion = rps.getCriterion();
                            if (!existingCriterionArrayList.contains(criterion)) {
                                existingCriterionArrayList.add(criterion);
                            }
                            char medianRating = rps.getRating();
                            boolean flag = rps.getFlag();
                            int nrRat = rps.getNumberOfRatings();
                            EndRatingPerStudent endRatingPerStudent = new EndRatingPerStudent(crName, vorname, nachname, date_str, criterion, medianRating, flag, nrRat);
                            endRatingPerStudentMap.get(courseName).get(name).add(endRatingPerStudent);
                            //Log.v(TAG, "DRSA 5:  " + name + " " + date_str + " " + criterion + " " + medianRating + " " + flag + " " + nrRat);
                        }
                        for (EndRatingPerStudent rps : mapIt2.getValue()) {
                            String crName = rps.getCourseName();
                            String vorname = rps.getVorname();
                            String nachname = rps.getNachname();
                            String date_str = rps.getDate();
                            int criterion = rps.getCriterion();
                            char medianRating = (char) 32;
                            boolean flag = false;
                            int nrRat = 0;
                            if(!existingCriterionArrayList.contains(criterion)) {
                                //Log.v(TAG, "DRSA 5a:  " + name + " " + date_str + " " + criterion + " " + medianRating + " " + flag + " " + nrRat);
                                existingCriterionArrayList.add(criterion);
                                EndRatingPerStudent endRatingPerStudent = new EndRatingPerStudent(crName, vorname, nachname, date_str, criterion, medianRating, flag, nrRat);
                                endRatingPerStudentMap.get(courseName).get(name).add(endRatingPerStudent);
                            }
                        }
                    } else {
                        existingCriterionArrayList.clear();
                        for (EndRatingPerStudent rps : mapIt2.getValue()) {
                            String crName = rps.getCourseName();
                            String vorname = rps.getVorname();
                            String nachname = rps.getNachname();
                            String date_str = rps.getDate();
                            int criterion = rps.getCriterion();
                            char medianRating = (char) 32;
                            boolean flag = false;
                            int nrRat = 0;
                            if (!existingCriterionArrayList.contains(criterion)) {
                                EndRatingPerStudent endRatingPerStudent = new EndRatingPerStudent(crName, vorname, nachname, date_str, criterion, medianRating, flag, nrRat);
                                endRatingPerStudentMap.get(courseName).get(name).add(endRatingPerStudent);
                                //Log.v(TAG, "DRSA 6:  " + name + " " + date_str + " " + criterion + " " + medianRating + " " + flag + " " + nrRat);
                                existingCriterionArrayList.add(criterion);
                            }
                        }
                    }
                }
            }
        }
        else {
            Toast.makeText(this, "Keine gespeicherten Bewertungen vorhanden ", Toast.LENGTH_SHORT).show();
        }
        for (Map.Entry<String, TreeMap<String, ArrayList<EndRatingPerStudent>>> mapIt : endRatingPerStudentMap.entrySet()) {
            for (Map.Entry<String, ArrayList<EndRatingPerStudent>> mapIt2 : mapIt.getValue().entrySet()) {
                Comparator<EndRatingPerStudent> byValue = Comparator.comparing(EndRatingPerStudent::getCriterion);
                mapIt2.getValue().sort(byValue);
            }
        }
        endRatingPerStudentArrayList = new ArrayList <EndRatingPerStudent>();
        boolean chosenCourseRatingsExistFlag = false;
        for (Map.Entry<String, TreeMap<String, ArrayList<EndRatingPerStudent>>> mapIt : endRatingPerStudentMap.entrySet()) {
            String courseName = mapIt.getKey();
            loadSettings();
            settings = settingsMap.get(courseName);
            boolean ratingScalePCMFlag = settings != null && settings.getRatingScale().equals(ratingPlusCircleMinus);
            for (Map.Entry<String, ArrayList<EndRatingPerStudent>> mapIt2 : mapIt.getValue().entrySet()) {
                /*
                if(courseName.equals(courseNameChosenInMain)) {
                    endRatingPerStudentArrayList.addAll(mapIt2.getValue());
                    chosenCourseRatingsExistFlag = true;
                }
                */
                for (EndRatingPerStudent rps : mapIt2.getValue()) {
                    //endRatingPerStudentArrayList.add(rps);
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
                    int nrRat = rps.getNumberOfRatings();
                    //Log.v(TAG, "DR: " + vorname +" " + nachname + " " + crName + " " + date_str + " " + criterion + " " + rating + " " + flag);
                    EndRatingPerStudent endRatingPerStudent = new EndRatingPerStudent(crName, vorname, nachname, date_str, criterion, rating, flag, nrRat);
                    if(crName.equals(courseNameChosenInMain)) {
                        //Log.v(TAG, "DR: " + vorname +" " + nachname + " " + crName + " " + date_str + " " + criterion + " " + rating + " " + flag);
                        endRatingPerStudentArrayList.add(endRatingPerStudent);
                        chosenCourseRatingsExistFlag = true;
                    }
                }

            }
        }
        if(!chosenCourseRatingsExistFlag) {
            Toast.makeText(DisplayRatingStatisticsActivity.this, "Noch keine Bewertungen zu " + courseNameChosenInMain + " vorhanden ", Toast.LENGTH_SHORT).show();
        }
        displayRatingsOneCriterionListener();
        recyclerView = findViewById(R.id.end_recycler_id);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(arrayEndRatingPerStudentAdapter);

        findViewById(R.id.write_into_file).setOnClickListener(l -> {

            SimpleDateFormat sdF = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);
            Date date = new Date();
            String date_str = sdF.format(date);

            storageAccessManager.openDestinationChooser(new StorageAccessManager.DestinationSelectionListener() {
                @Override
                public void onDestinationSelected(int status, Uri destination) {
                    fileManager = new SAFFileManager(DisplayRatingStatisticsActivity.this, destination);
                    //Log.v(TAG, "directory chosen");
                    if (endRatingPerStudentMap != null && !endRatingPerStudentMap.isEmpty()) {
                        for (Map.Entry<String, TreeMap<String, ArrayList<EndRatingPerStudent>>> mapIt : endRatingPerStudentMap.entrySet()) {
                            String courseName = mapIt.getKey();
                            String courseName2 = courseName.replace(",", "_");
                            courseName2 = courseName2.replaceAll(" ", "_");
                            courseName2 = courseName2.replaceAll("/", "_");
                            String filePath = "Gesamtbewertungen_" + courseName2 + "_" + date_str + ".csv";
                            //Log.v(TAG, "file path " + filePath);
                            loadSettings();
                            settings = settingsMap.get(courseName);
                            boolean ratingScalePCMFlag = settings != null && settings.getRatingScale().equals(ratingPlusCircleMinus);
                            students_str = "";
                            for (Map.Entry<String, ArrayList<EndRatingPerStudent>> mapIt2 : mapIt.getValue().entrySet()) {
                                //String name = mapIt2.getKey();
                                for (EndRatingPerStudent rps : mapIt2.getValue()) {
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
                                    int nrRat = rps.getNumberOfRatings();
                                    //String st_str = String.format("%s\u003B%s\u003B%s\u003B%s\u003B%d\u003B%c\u003B%d\u003B%b%n",
                                    //        vorname, nachname, courseName3, date_str, criterion, rating, nrRat, flag);
                                    String st_str = String.format("%s\u003B%s\u003B%s\u003B%s\u003B%d\u003B%c\u003B%d%n",
                                                    vorname, nachname, courseName3, date_str, criterion, rating, nrRat);
                                    students_str += st_str;
                                }
                            }
                            String content = students_str;
                            try {
                                fileManager.writeFile(filePath, content.getBytes(StandardCharsets.UTF_8), false);
                                //Log.v(TAG, "file saved" + filePath);
                                Toast.makeText(DisplayRatingStatisticsActivity.this, "Datei " + filePath + " gespeichert", Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                Log.e(TAG, "Some error occurred while writing content to file ", e);
                                Toast.makeText(DisplayRatingStatisticsActivity.this, "Some error occurred while writing content to file " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    } else {
                        Toast.makeText(DisplayRatingStatisticsActivity.this, "keine Gesamtbewertungen vorhanden", Toast.LENGTH_LONG).show();
                    }
                }
            });
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
                    Log.v(TAG, "Einstellungen konnten nicht geladen werden");
                    Toast.makeText(this, "Einstellungen konnten nicht geladen werden", Toast.LENGTH_LONG).show();
                }

            } else {
                Toast.makeText(this, "Keine gespeicherten Einstellungen vorhanden", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            //Toast.makeText(RatingActivity.this, "Some error occurred while getting shared preferences " + e.getMessage(), Toast.LENGTH_LONG).show();
            Toast.makeText(DisplayRatingStatisticsActivity.this, "Keine gespeicherten Einstellungen vorhanden ", Toast.LENGTH_LONG).show();
        }
    }
    private void displayRatingsOneCriterionListener() {

        OnItemClickListener listener = new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                EndRatingPerStudent ratingPerStudent = endRatingPerStudentArrayList.get(position);
                String courseName = ratingPerStudent.getCourseName();
                String vorname = ratingPerStudent.getVorname();
                String nachname = ratingPerStudent.getNachname();
                String date_str = ratingPerStudent.getDate();
                int criterion = ratingPerStudent.getCriterion();
                char rating = ratingPerStudent.getRating();
                boolean flag = ratingPerStudent.getFlag();
                RatingPerStudentParcel ratingPerStudentParcel
                        = new RatingPerStudentParcel(courseName, vorname, nachname, date_str, criterion, rating, flag);
                //Log.d("onclick", "DRSA onClick position "
                //        + " " + position + " " + courseName + " " + vorname + " " + date_str + " " + criterion + " " + rating);

                Intent intent = new Intent(DisplayRatingStatisticsActivity.this, DisplayRatingsOneCriterionActivity.class);
                intent.putExtra(RATINGPERSTUDENTPARCEL_OBJECT, ratingPerStudentParcel);
                //Log.v(TAG, "Send RatingPerStudentParcel Object");
                startActivity(intent);
            }
        };
        arrayEndRatingPerStudentAdapter =
                new ArrayEndRatingPerStudentAdapter(R.layout.endrating_row, endRatingPerStudentArrayList, this, listener);

    }
    public void goHome(View view) {
        // go back to main page  in response to button
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
