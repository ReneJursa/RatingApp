package com.ratingapp;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import androidx.databinding.DataBindingUtil;

import com.ratingapp.databinding.ActivityRatingBinding;
import com.ratingapp.databinding.RatingboundviewItemBinding;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Map;
import java.util.TreeMap;
import java.util.Date;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;

public class RatingActivity extends AppCompatActivity {
    public final static String COURSE_OBJECT = "CourseObject";
    public final static String RATINGLIST_OBJECT = "RatingList";
    private static final String TAG = "RatingActivity";
    private static final String keyCurrentlyUnRatedCourse = "currentlyUnRatedCourse";
    private static final String keyCurrentlyRatedCourse = "currentlyRatedCourse";
    private static final String sharedPreferences_str = "com.ratingapp.SHAREDPREFERENCES";
    private static final String keyAllRatedCourses = "allRatedCourses";
    private static final String sharedSettingsPreferences_str = "com.ratingapp.SettingsActivity.SHAREDPREFERENCES";
    private static final String keySettings = "SettingsKey";
    private static final String ratingPlusCircleMinus = "Plus-Kreis-Minus-Skala";
    private static final String ratingABCD = "ABCD-Notenskala";
    private static final String rating1till6 = "Notenskala von 1 bis 6";
    private static final String arithmeticMean_str = "ArithmeticMean";
    private static final String median_str = "Median";
    public static final String COURSENAMEMESSAGE = "com.ratingapp.COURSENAMEMESSAGE";
    ActivityRatingBinding binding;
    Course course;
    boolean courseFlag = false;
    private String courseNameChosenInMain = "";
    private ArrayList<RatingBoundView> ratingBoundViewList = new ArrayList<RatingBoundView>();
    RatingBoundView[] ratingBoundViews;
    private RatingList ratingList;
    boolean ratingListFlag = false;
    private TreeMap<String, TreeMap<String, ArrayList<RatingPerStudent>>> ratingPerStudentMap = new TreeMap<String, TreeMap<String, ArrayList<RatingPerStudent>>>();
    private Settings settings;
    private TreeMap<String, Settings> settingsMap = new TreeMap<String, Settings>();
    private int nrOfCriteria = 3;
    private static final char startRating = (char)32;
	private Date date;
    SimpleDateFormat sdF = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);
    TextView dateDisplay;
    Calendar currentDate = Calendar.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Log.i("oncreate", "oncreate");
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_rating);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_rating);
        dateDisplay = findViewById(R.id.dateID);
        Intent intent = getIntent();
        if (intent != null) {
            //Log.v(TAG, "intent not null");
            /*
            if (intent.hasExtra(getString(R.string.appMessage))) {
                String courseName = intent.getStringExtra(COURSENAMEMESSAGE);
                Log.i(TAG, "Got course name " + courseName);
                courseNameChosenInMain = courseName;
                String keyString = keyCurrentlyUnRatedCourse + courseName;
                loadData2(keyString);
            }
            else
            */
			//date = new Date();
            setDate();
            if (intent.hasExtra(COURSE_OBJECT)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    course = intent.getParcelableExtra(COURSE_OBJECT, Course.class);
                }
                else {
                    course = intent.getParcelableExtra(COURSE_OBJECT);
                }
                String courseName = "unbekannt";
                String vorname = "unbekannt";
                String nachname = "unbekannt";
                if (course != null) {
                    courseFlag = true;
                    courseName = course.getName();
                    //Log.i(TAG, "Rating got course name " + courseName);
                    loadSettings();
                    settings = settingsMap.get(courseName);
                    String ratingScale = ratingABCD;
                    if(settings != null) {
                        if (settings.getRatingScale().equals(ratingPlusCircleMinus)) {
                            ratingScale = ratingPlusCircleMinus;
                        } else if (settings.getRatingScale().equals(rating1till6)) {
                            ratingScale = rating1till6;
                        }
                        nrOfCriteria = settings.getNrOfCriteria();
                        //Log.v(TAG, "RA: Settings " + courseName + " " + settings.getRatingScale() + " " + settings.getRatingAverage() + " " + settings.getNrOfCriteria());
                    }
                    else {
                        String ratingAverage = arithmeticMean_str;
                        settings = new Settings(ratingScale, ratingAverage, nrOfCriteria);
                        if(!settingsMap.containsKey(courseName)) {
                            settingsMap.put(courseName, settings);
                            //Log.v(TAG,"RA if " + ratingScale + " " +  ratingAverage + " " + nrOfCriteria);
                        }
                        else {
                            settingsMap.get(courseName).setRatingScale(ratingScale);
                            settingsMap.get(courseName).setRatingAverage(ratingAverage);
                            settingsMap.get(courseName).setNrOfCriteria(nrOfCriteria);
                            //Log.v(TAG,"RA else " + ratingScale + " " +  ratingAverage + " " + nrOfCriteria);
                        }
                        saveSettings();
                    }
                    binding.setCourse(course);

                    for (Student st : course.studList) {
                        vorname = st.getVorname();
                        nachname = st.getNachname();

                        //Log.v(TAG, "Got Course Object 1: " + course.getName() + " " + vorname + " " + nachname);
                        for (int i = 1; i <= nrOfCriteria; i++) {
                            String name = vorname + " " + nachname;
                            ratingBoundViewList.add(new RatingBoundView(courseName, vorname, nachname, i, name, startRating, ratingScale));
                        }
                    }
                    //String keyString = keyCurrentlyUnRatedCourse + courseName;
                    //saveData2(keyString);
                }
                bindRatingViewList();
            }
            else if (intent.hasExtra(RATINGLIST_OBJECT)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ratingList = intent.getParcelableExtra(RATINGLIST_OBJECT, RatingList.class);
                }
                else {
                    ratingList = intent.getParcelableExtra(RATINGLIST_OBJECT);
                }
                if (ratingList != null) {
                    ratingListFlag = true;
                    String courseName = ratingList.getCourseName();
                    course = new Course(courseName);
                    loadSettings();
                    settings = settingsMap.get(courseName);
                    String ratingScale = ratingABCD;
                    if(settings != null && settings.getRatingScale().equals(ratingPlusCircleMinus)) {
                        ratingScale = ratingPlusCircleMinus;
                    }
                    else if(settings != null && settings.getRatingScale().equals(rating1till6)) {
                        ratingScale = rating1till6;
                    }
                    binding.setCourse(course);
                    courseFlag = true;
                    //Log.v(TAG, "MA ratingList not null");
                    for (Rating rt : ratingList.rList) {
                        String vorname = rt.getVorname();
                        String nachname = rt.getNachname();
                        int criterion = rt.getCriterion();
                        String name = rt.getName();
                        char rating = rt.getRating();
                        boolean flag = rt.getFlag();
                        //Log.v(TAG, "Got Rating Object 2: " + course.getName() + " " + name + " " + rating);
                        RatingBoundView ratingBoundView = new RatingBoundView(courseName, vorname, nachname, criterion, name, rating, ratingScale);
                        ratingBoundView.setFlag(flag);
                        ratingBoundViewList.add(ratingBoundView);
                    }
                }
                bindRatingViewList();
            }
        }
    }
    private void bindRatingViewList() {
        ratingBoundViews = ratingBoundViewList.toArray(new RatingBoundView[0]);
        ActionCallback actionCallback = new ActionCallback() {
            @Override
            public void onClick(RatingBoundView ratingBoundView) {
                ratingBoundView.setFlag(!ratingBoundView.isFlag(), ratingBoundView.getRatingIndex());
            }
        };
        RatingBoundViewAdapter adapter = new RatingBoundViewAdapter(actionCallback, ratingBoundViews);
        binding.ratingBoundViewList.setAdapter(adapter);
    }
    private static class RatingBoundViewAdapter extends DataBoundAdapter<RatingboundviewItemBinding> {
        List<RatingBoundView> mRatingBoundViewList = new ArrayList<>();
        private final ActionCallback mActionCallback;
        public RatingBoundViewAdapter(ActionCallback actionCallback, RatingBoundView... ratingBoundViews) {
            super(R.layout.ratingboundview_item);
            mActionCallback = actionCallback;
            Collections.addAll(mRatingBoundViewList, ratingBoundViews);
        }
        @Override
        protected void bindItem(DataBoundViewHolder<RatingboundviewItemBinding> holder, int position,
                                List<Object> payloads) {
            holder.binding.setData(mRatingBoundViewList.get(position));
            holder.binding.setCallback(mActionCallback);
        }
        @Override
        public int getItemCount() {
            return mRatingBoundViewList.size();
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        //Log.i("pause", "RA onpause");
    }
    @Override
    protected void onStop() {
        super.onStop();
        //Log.i("onstop", "RA onstop");
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Log.i("ondestroy", "RA ondestroy");
    }
    @Override
    protected void onResume() {
        super.onResume();
        //Log.i("onresume", "RA onresume");
    }
    @Override
    protected void onRestart() {
        super.onRestart();
        //Log.i("onrestart", "RA onrestart");
    }
    @Override
    protected void onStart() {
        super.onStart();
        //Log.i("onstart", "RA onstart");
    }
    /*
    public void loadData(View view) {
        String courseName = "";
        if(ratingBoundViewList != null && !ratingBoundViewList.isEmpty()) {
            courseName = ratingBoundViewList.get(0).getCourseName();
        }
        else if (!courseNameChosenInMain.isEmpty()) {
            courseName = courseNameChosenInMain;
        }
        if (!courseName.isEmpty()) {
            String keyString = keyCurrentlyRatedCourse + courseName;
            //Log.v(TAG, " vor loadData2 courseName: " + courseName);
            loadData2(keyString);
        }
        else {
            Toast.makeText(this, "Keine gespeicherten Kursdaten vorhanden", Toast.LENGTH_SHORT).show();
        }
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
            Toast.makeText(RatingActivity.this, "Keine gespeicherten Einstellungen vorhanden ", Toast.LENGTH_LONG).show();
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
    private void loadData2(String keyString) {
        // method to load arraylist from shared prefs
        // initializing our shared prefs with name as
        // shared preferences.
        try {
            SharedPreferences sharedPreferences = getSharedPreferences(sharedPreferences_str, MODE_PRIVATE);
            if (sharedPreferences != null) {
                Gson gson = new Gson();
                String json = sharedPreferences.getString(keyString, null);
                Type type = new TypeToken<ArrayList<RatingBoundView>>() {}.getType();
                if(gson.fromJson(json, type) != null) {
                    ratingBoundViewList = gson.fromJson(json, type);
                    Toast.makeText(this, "gespeicherte Kursdaten geladen ", Toast.LENGTH_SHORT).show();
                    String courseName = ratingBoundViewList.get(0).getCourseName();
                    course = new Course(courseName);
                    binding.setCourse(course);
                    courseFlag = true;
                    bindRatingViewList();
                } else {
                    Toast.makeText(this, "Kursdaten konnten nicht geladen werden", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Keine gespeicherten Kursdaten vorhanden", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            //Toast.makeText(RatingActivity.this, "Some error occurred while getting shared preferences " + e.getMessage(), Toast.LENGTH_LONG).show();
            Toast.makeText(RatingActivity.this, "Keine gespeicherten Daten vorhanden ", Toast.LENGTH_LONG).show();
            //Log.e(TAG,"Some error occurred while getting shared preferences " + e.getMessage());
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
                //}
                //if (ratingPerStudentMap != null && !ratingPerStudentMap.isEmpty()) {
                    //Toast.makeText(this, "Liste aller Kursdaten geladen ", Toast.LENGTH_SHORT).show();
                }
                /*
                else {
                    Log.v(TAG, "Liste aller Kursdaten konnte nicht geladen werden");
                    //Toast.makeText(this, "Liste aller Kursdaten konnte nicht geladen werden", Toast.LENGTH_SHORT).show();
                }
                */
            } else {
                Toast.makeText(this, "Keine gespeicherten Daten vorhanden", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            //Toast.makeText(RatingActivity.this, "Some error occurred while getting shared preferences " + e.getMessage(), Toast.LENGTH_LONG).show();
            Toast.makeText(RatingActivity.this, "Keine gespeicherten Daten vorhanden ", Toast.LENGTH_LONG).show();
        }
    }
    public void saveData(View view) {
        String courseName = "";
        if(ratingBoundViewList != null && !ratingBoundViewList.isEmpty()) {
            courseName = ratingBoundViewList.get(0).getCourseName();
        }
        if (!courseName.isEmpty()) {
            String keyString = keyCurrentlyRatedCourse + courseName;
            //saveData2(keyString);
            saveDataAlertDialog(keyString, courseName);
        }
        else {
            Toast.makeText(this, "Keine Bewertungen vorhanden", Toast.LENGTH_SHORT).show();
        }
    }
    public void saveDataAlertDialog(String keyString, String courseName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Speichern der Bewertungen von Kurs " + courseName + "?");
        builder.setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                        Toast.makeText(RatingActivity.this, "Bewertungen zu " + courseName + " wurden gespeichert\nAlle aktuellen Bewertungen auf Anfangszustand gesetzt", Toast.LENGTH_LONG).show();
                        saveData2(keyString);
                        for(RatingBoundView rbv : ratingBoundViewList) {
                            String crName = rbv.getCourseName();
                            if(crName.equals(courseName)) {
                                rbv.setRating(startRating);
                                rbv.setFlag(false);
                            }
                        }
                        bindRatingViewList();
                    }
                });
        builder.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        Toast.makeText(RatingActivity.this, "Speicherung abgebrochen", Toast.LENGTH_SHORT).show();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void saveData2(String keyString) {
        if (ratingBoundViewList != null && !ratingBoundViewList.isEmpty()) {

            loadRatingPerStudentMap();
            /*
            if(ratingPerStudentMap != null && !ratingPerStudentMap.isEmpty()) {
                for (Map.Entry<String, TreeMap<String, ArrayList<RatingPerStudent>>> mapIt : ratingPerStudentMap.entrySet()) {
                    String courseName = mapIt.getKey();
                    for (Map.Entry<String, ArrayList<RatingPerStudent>> mapIt2 : mapIt.getValue().entrySet()) {
                        String name = mapIt2.getKey();
                        for (RatingPerStudent rps : mapIt2.getValue()) {
                            String date_str = rps.getDate();
                            int criterion = rps.getCriterion();
                            char rating = rps.getRating();
                            boolean flag = rps.getFlag();
                            Log.v(TAG, "rM: " + courseName + " " + name + " " + date_str + " " + criterion + " " + rating + " " + flag);
                        }
                    }
                }
            }
            */
            //SimpleDateFormat sdF = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);
            //Date date = new Date();
            setDate();
            String date_str = sdF.format(date);

            loadSettings();
            settings = settingsMap.get(course.getName());
            boolean ratingScalePCMFlag = settings != null && settings.getRatingScale().equals(ratingPlusCircleMinus);

            for(RatingBoundView rbv : ratingBoundViewList) {
                String courseName = rbv.getCourseName();
                //String courseName2 = courseName.replace(",","");
                String vorname = rbv.getVorname();
                String nachname = rbv.getNachname();
                String name = nachname + " " + vorname;

                String key = vorname + " " +  nachname + " " + courseName;
                int criterion = rbv.getCriterion();
                char rating = rbv.getRating();
                if(ratingScalePCMFlag) {
                    switch (rating) {
                        case '+' -> rating = 'A';
                        case 'o' -> rating = 'B';
                        case '-' -> rating = 'C';
                        default -> rating = ' ';
                    }
                }
                boolean flag = rbv.getFlag();

                if(ratingPerStudentMap.containsKey(courseName)) {
                    if (ratingPerStudentMap.get(courseName).containsKey(name)) {
                        ratingPerStudentMap.get(courseName).get(name).add(new RatingPerStudent(courseName, vorname, nachname, date_str, criterion, rating, flag));
                        //Log.v(TAG, "rM1: " + key + " " + date_str + " " + criterion + " " + rating + " " + flag);
                    }
                    else {
                        ratingPerStudentMap.get(courseName).put(name, new ArrayList<RatingPerStudent>());
                        ratingPerStudentMap.get(courseName).get(name).add(new RatingPerStudent(courseName, vorname, nachname, date_str, criterion, rating, flag));
                        //Log.v(TAG, "rM2: " + key + " " + date_str + " " + criterion + " " + rating + " " + flag);
                    }
                }
                else {
                    ratingPerStudentMap.put(courseName, new TreeMap<String, ArrayList<RatingPerStudent>>());
                    ratingPerStudentMap.get(courseName).put(name, new ArrayList<RatingPerStudent>());
                    ratingPerStudentMap.get(courseName).get(name).add(new RatingPerStudent(courseName, vorname, nachname, date_str, criterion, rating, flag));
                    //Log.v(TAG, "rM3: " + key + " " + date_str + " " + criterion + " " + rating + " " + flag);
                }
            }
            /*
            for (Map.Entry<String, TreeMap<String, ArrayList<RatingPerStudent>>> mapIt : ratingPerStudentMap.entrySet()) {
                String courseName = mapIt.getKey();
                for (Map.Entry<String, ArrayList<RatingPerStudent>> mapIt2 : mapIt.getValue().entrySet()) {
                    String name = mapIt2.getKey();
                    for (RatingPerStudent rps : mapIt2.getValue()) {
                        String date_str2 = rps.getDate();
                        int criterion = rps.getCriterion();
                        char rating = rps.getRating();
                        boolean flag = rps.getFlag();
                        Log.v(TAG, "rM4: " + courseName + " " + name + " " + date_str2 + " " + criterion + " " + rating + " " + flag);
                    }
                }
            }
            */
            SharedPreferences sharedPreferences = getSharedPreferences(sharedPreferences_str, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            Gson gson = new Gson();
            //String json = gson.toJson(ratingBoundViewList);
            String jsonRatingPerStudentMap = gson.toJson(ratingPerStudentMap);
            //editor.putString(keyString, json);
            editor.putString(keyAllRatedCourses, jsonRatingPerStudentMap);
            //editor.apply();
            boolean saveFlag = editor.commit();
            /*
            if (saveFlag) {
                Toast.makeText(this, "Kursdaten gespeichert ", Toast.LENGTH_SHORT).show();
            } else {
            */
            if (!saveFlag) {
                Toast.makeText(this, "Kursdatenspeicherung fehlgeschlagen", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(this, "Keine zu speichernden Daten vorhanden", Toast.LENGTH_SHORT).show();
        }
    }
    /*
    public void sendMessage(View view) {
        Intent intent = new Intent(this, FileActivity.class);
        //EditText editText = (EditText) findViewById(R.id.edit_message);
        //String message = editText.getText().toString();
        //intent.putExtra(RATING_MESSAGE, message);
        if (courseFlag == true) {

            for (RatingBoundView ct: ratingBoundViewList) {
                Log.v(TAG, "C1: " + ct.getCourseName() + " " + ct.getName() + " " + ct.getRating());
            }

            ratingList = new RatingList(ratingBoundViewList.get(0).getCourseName(), ratingBoundViewList);

            for (Rating rt: ratingList.rList) {
                Log.v(TAG, "RT1: " + rt.getName() + " " + rt.getRating());
            }

            intent.putExtra(RATINGLIST_OBJECT, ratingList);
        }

        Log.v(TAG, "Intent fired ");
        startActivity(intent);
    }
    */
    public void displayRatings(View view) {
        Intent intent = new Intent(this, DisplayRatingsActivity.class);
        //Log.v(TAG, "Intent for DRA fired ");
        startActivity(intent);
    }
    public void goMain(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
    public void settings(View view) {
        Log.v(TAG, "in settings");
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.putExtra(COURSE_OBJECT, course);
        startActivity(intent);
    }
    public void onDButtonClicked(View v) {

        DatePickerDialog.OnDateSetListener dListener = new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int yr, int mth, int dy) {
                currentDate.set(Calendar.YEAR, yr);
                currentDate.set(Calendar.MONTH, mth);
                currentDate.set(Calendar.DAY_OF_MONTH, dy);
                setDate();
            }
        };

        new DatePickerDialog(this, dListener,
                currentDate.get(Calendar.YEAR),
                currentDate.get(Calendar.MONTH),
                currentDate.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void setDate() {
        date = new Date(currentDate.getTimeInMillis());
        String date_str = sdF.format(date);
        dateDisplay.setText(date_str);
    }
}
