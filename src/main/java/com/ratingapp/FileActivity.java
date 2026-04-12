package com.ratingapp;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.TreeMap;

public class FileActivity extends AppCompatActivity {
    public final static String EXTRA_OBJECT = "CourseObject";
    public final static String EXTRA_RATINGLIST = "RatingList";
    private int fileNumber = 0;
    SAFFileManager fileManager = null;
    private static final String TAG = "FileActivity";
    private static final String keyAllRatedCourses = "allRatedCourses";
    private static final String sharedPreferences_str = "com.ratingapp.SHAREDPREFERENCES";
    private static final String sharedSettingsPreferences_str = "com.ratingapp.SettingsActivity.SHAREDPREFERENCES";
    private static final String keySettings = "SettingsKey";
    private static final String ratingPlusCircleMinus = "Plus-Kreis-Minus-Skala";
    private static final String ratingABCD = "ABCD-Notenskala";
    private static final String rating1till6 = "Notenskala von 1 bis 6";
    private static final String arithmeticMean_str = "ArithmeticMean";
    private TreeMap<String, TreeMap<String, ArrayList<RatingPerStudent>>> ratingPerStudentMap = new TreeMap<String, TreeMap<String, ArrayList<RatingPerStudent>>>();
    private Settings settings;
    private TreeMap<String, Settings> settingsMap = new TreeMap<String, Settings>();
    private Intent intent;
    private boolean saveFlag = false;
	String courseName = "";
    Course course;
    RatingList ratingList;
    String students_str = "";
    String ratings_str = "";
    boolean RatingFileReadFlag = false;
    boolean RatingFlag = false;
    boolean dirChosen = false;
    ArrayList<String> rows = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_file);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        final StorageAccessManager storageAccessManager = new StorageAccessManager(this);
        String message = "no data from intent";

        Intent intent = getIntent();
        if (intent != null) {
            //Log.v(TAG, "DM intent not null");

            if (intent.hasExtra(EXTRA_RATINGLIST)) {

                //Log.v(TAG, "Got Intent");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ratingList = intent.getParcelableExtra(EXTRA_RATINGLIST, RatingList.class);
                }
                else {
                    ratingList = intent.getParcelableExtra(EXTRA_RATINGLIST);
                }

                if (ratingList != null) {
                    //Log.v(TAG, "DM ratingList not null");
                    RatingFlag = true;
                    students_str = "";
                    for (Rating rt : ratingList.rList) {
                        //String name = rt.getName();
                        //char rating = rt.getRating();
                        //Log.v(TAG, "Got Rating Object, Name: " + name + " " + rating);
                        String vorname = rt.getVorname();
                        String nachname = rt.getNachname();
                        int criterion = rt.getCriterion();
                        char rating = rt.getRating();
                        boolean flag = rt.getFlag();
                        String st_str = String.format("%s\u003B%s\u003B%d\u003B%c\u003B%b%n",
                                vorname, nachname, criterion, rating, flag);
                        students_str += st_str;
                        //Log.v(TAG, "st_str: " + st_str);
                    }
                }
            }
        }

        findViewById(R.id.choose_base_directory).setOnClickListener(l -> {
            try {
                storageAccessManager.openDestinationChooser(new StorageAccessManager.DestinationSelectionListener() {
                    @Override
                    public void onDestinationSelected(int status, Uri destination) {
                        fileManager = new SAFFileManager(FileActivity.this, destination);
                        //Log.v(TAG, "directory chosen");
                        dirChosen = true;
                    }
                });
            } catch (Exception e) {
                Toast.makeText(FileActivity.this, "Fehler beim Auswählen des Verzeichnisses " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        findViewById(R.id.read_file).setOnClickListener(l -> {
            String filePath = ((TextView) findViewById(R.id.file_path)).getText().toString();
            if (!filePath.isEmpty()) {
                if (dirChosen) {
                    try {
                        students_str = fileManager.readFileAsString(filePath);
                        if (students_str != null) {
                            //Log.v(TAG, "file read");
                            //Log.v(TAG, students_str);
                            RatingFlag = false;
                            Toast.makeText(FileActivity.this, "Datei " + filePath + " gelesen", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(FileActivity.this, "Datei " + filePath + " nicht lesbar", Toast.LENGTH_LONG).show();
                        }
                    } catch (IOException e) {
                        Toast.makeText(FileActivity.this,"Fehler beim Lesen der Datei " + filePath + " " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
                else {
                    storageAccessManager.openDestinationChooser(new StorageAccessManager.DestinationSelectionListener() {
                        @Override
                        public void onDestinationSelected(int status, Uri destination) {
                            fileManager = new SAFFileManager(FileActivity.this, destination);
                            try {
                                students_str = fileManager.readFileAsString(filePath);
                                if (students_str != null) {
                                    //Log.v(TAG, students_str);
                                    RatingFlag = false;
                                    Toast.makeText(FileActivity.this, "Datei " + filePath + " gelesen", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    Toast.makeText(FileActivity.this, "Datei " + filePath + " nicht lesbar", Toast.LENGTH_LONG).show();
                                }
                            } catch (IOException e) {
                                Log.e(TAG, "Some error occurred while reading file ", e);
                                Toast.makeText(FileActivity.this, "Fehler beim Lesen der Datei "  + filePath + " " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
            else {
                Toast.makeText(FileActivity.this, "kein Dateiname angegeben", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.read_rating_file).setOnClickListener(l -> {
            String filePath = ((TextView) findViewById(R.id.file_path)).getText().toString();
            if (!filePath.isEmpty()) {
                if (dirChosen) {
                    try {
                        ratings_str = fileManager.readFileAsString(filePath);
                        if (ratings_str != null) {
                            //Log.v(TAG, "rating file read");
                            //Log.v(TAG, ratings_str);
                            RatingFileReadFlag = true;
                            Toast.makeText(FileActivity.this, "Datei " + filePath + " gelesen", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(FileActivity.this, "Datei " + filePath + " nicht lesbar", Toast.LENGTH_LONG).show();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Some error occurred while reading rating file ", e);
                        RatingFileReadFlag = false;
                        Toast.makeText(FileActivity.this,"Fehler beim Lesen der Datei " + filePath + " " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
                else {
                    storageAccessManager.openDestinationChooser(new StorageAccessManager.DestinationSelectionListener() {
                        @Override
                        public void onDestinationSelected(int status, Uri destination) {
                            fileManager = new SAFFileManager(FileActivity.this, destination);
                            try {
                                ratings_str = fileManager.readFileAsString(filePath);
                                if (ratings_str != null) {
                                    //Log.v(TAG, ratings_str);
                                    RatingFileReadFlag = true;
                                    Toast.makeText(FileActivity.this, "Datei " + filePath + " gelesen", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    Toast.makeText(FileActivity.this, "Datei " + filePath + " nicht lesbar", Toast.LENGTH_LONG).show();
                                }
                            } catch (IOException e) {
                                Log.e(TAG, "Some error occurred while reading rating file ", e);
                                RatingFileReadFlag = false;
                                Toast.makeText(FileActivity.this, "Fehler beim Lesen der Datei "  + filePath + " " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
            else {
                Toast.makeText(FileActivity.this, "kein Dateiname angegeben", Toast.LENGTH_SHORT).show();
            }
        });
        /*
        findViewById(R.id.write_into_file).setOnClickListener(l -> {
            String filePath = ((TextView) findViewById(R.id.file_path)).getText().toString();
            if (!filePath.isEmpty()) {
                String content = students_str;
                if (dirChosen) {
                    try {
                        fileManager.writeFile(filePath, content.getBytes(StandardCharsets.UTF_8), false);
                        //Log.v(TAG, "file saved");
                        Toast.makeText(FileActivity.this, "Datei " + filePath + " gespeichert", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Log.e(TAG, "Some error occurred while writing content to file ", e);
                        Toast.makeText(FileActivity.this, "Fehler beim Speichern der Datei " + filePath + " " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
                else {
                    storageAccessManager.openDestinationChooser(new StorageAccessManager.DestinationSelectionListener() {
                        @Override
                        public void onDestinationSelected(int status, Uri destination) {
                            fileManager = new SAFFileManager(FileActivity.this, destination);
                            try {
                                fileManager.writeFile(filePath, content.getBytes(StandardCharsets.UTF_8), false);
                                //Log.v(TAG, "file saved");
                                Toast.makeText(FileActivity.this, "Datei " + filePath + " gespeichert", Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                Log.e(TAG, "Some error occurred while writing content to file ", e);
                                Toast.makeText(FileActivity.this, "Fehler beim Speichern der Datei " + filePath + " " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
            else {
                Toast.makeText(FileActivity.this, "kein Dateiname angegeben", Toast.LENGTH_SHORT).show();
            }
        });
        */
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
            Toast.makeText(FileActivity.this, "Keine gespeicherten Daten vorhanden ", Toast.LENGTH_SHORT).show();
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
                    Log.v(TAG, "Einstellungen konnten nicht geladen werden");
                    Toast.makeText(this, "Einstellungen konnten nicht geladen werden", Toast.LENGTH_LONG).show();
                }

            } else {
                Toast.makeText(this, "Keine gespeicherten Einstellungen vorhanden", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            //Toast.makeText(RatingActivity.this, "Some error occurred while getting shared preferences " + e.getMessage(), Toast.LENGTH_LONG).show();
            Toast.makeText(FileActivity.this, "Keine gespeicherten Einstellungen vorhanden ", Toast.LENGTH_LONG).show();
        }
    }
    public void save(View view) {
        //Intent intent;
        saveFlag = true;
        if (!RatingFlag && (students_str != null && !students_str.isEmpty())) {

            Scanner inp_stud = new Scanner(students_str);
            String name = "";
            String row = inp_stud.nextLine();
            boolean all_rows_valid = true;
            int nr_rows = 0;
            boolean courseObjCreated = false;
            while(inp_stud.hasNext()) {
                row = inp_stud.nextLine();
                int idx1 = row.indexOf(";");
                int idx2 = row.indexOf(";", idx1 + 1);
                int idx3 = row.indexOf(";", idx2 + 1);
                //System.out.println(row);
                //Log.v(TAG, "Zeile " + row);
                String[] s = row.split(";");
                if(idx1 != -1 && idx2 != -1 && idx3 != -1 && s.length == 4) {

                    int number = -1;
                    if(s[0] != null && !s[0].isEmpty()) {
                        if(Character.isDigit(s[0].trim().charAt(0))) {
                            try {
                                number = Integer.parseInt(s[0].trim());
                            } catch (NumberFormatException e) {
                                Log.e(TAG, "Die Person\n" + row + "\nwurde nicht importiert." + e.getMessage());
                                Toast.makeText(FileActivity.this, "Die Person\n" + row + "\nwurde nicht importiert.", Toast.LENGTH_SHORT).show();
                            }
                        }
                        else {
                            Toast.makeText(FileActivity.this, "Die Person\n" + row + "\nwurde nicht importiert.", Toast.LENGTH_SHORT).show();
                            continue;
                        }
                    }
                    String vorname = "";
                    if(s[1] != null && !s[1].isEmpty()) {
                        vorname = s[1].trim();
                    }
                    String nachname = "";
                    if(s[2] != null && !s[2].isEmpty()) {
                        nachname = s[2].trim();
                    }
                    courseName = "";
                    if(s[3] != null && !s[3].isEmpty() && !s[3].isBlank()) {
                        courseName = s[3].trim();
                        if (!courseObjCreated) {
                            course = new Course(courseName);
                            courseObjCreated = true;
                        }
                    }
                    if(courseObjCreated) {
                        course.studList.add(new Student(vorname, nachname));
                    }
                    else {
                        //Log.e(TAG,"Der/Die Kollegiat*in\n" + row + "\nwurde nicht importiert.");
                        Toast.makeText(FileActivity.this, "Die Person\n" + row + "\nwurde nicht importiert.", Toast.LENGTH_SHORT).show();
                        all_rows_valid = false;
                    }
                }
                else {
                    //Log.e(TAG,"Der/Die Kollegiat*in\n" + row + "\nwurde nicht importiert.");
                    Toast.makeText(FileActivity.this, "Die Person\n" + row + "\nwurde nicht importiert.", Toast.LENGTH_SHORT).show();
                    all_rows_valid = false;
                }
                nr_rows++;
            }
            if (all_rows_valid) {
                //Log.v(TAG, "Alle Kollegiat:innen wurden importiert.");
                Toast.makeText(FileActivity.this, "Alle Personen wurden importiert.", Toast.LENGTH_SHORT).show();
            }
            intent = new Intent(this, MainActivity.class);
            intent.putExtra(EXTRA_OBJECT, course);
            //Log.v(TAG, "Send Course Object");
        }
        else if (RatingFlag) {
            intent = new Intent(this, MainActivity.class);
            intent.putExtra(EXTRA_RATINGLIST, ratingList);
            //Log.v(TAG, "Send Rating List back");
        }
        else if(RatingFileReadFlag) {
            ratingPerStudentMap.clear();
            loadRatingPerStudentMap();
            boolean settingsExistFlag = false;
            if (ratings_str != null && !ratings_str.isEmpty()) {
                Scanner inp_stud = new Scanner(ratings_str);
                String row;
                boolean all_rows_valid = true;
                int nr_rows = 0;
                while(inp_stud.hasNext()) {
                    row = inp_stud.nextLine();
                    int idx1 = row.indexOf(";");
                    int idx2 = row.indexOf(";", idx1 + 1);
                    int idx3 = row.indexOf(";", idx2 + 1);
                    int idx4 = row.indexOf(";", idx3 + 1);
                    int idx5 = row.indexOf(";", idx4 + 1);
                    int idx6 = row.indexOf(";", idx5 + 1);
                    //Log.v(TAG, "Zeile " + row);
                    String[] s = row.split(";");
                    if(idx1 != -1 && idx2 != -1 && idx3 != -1 && idx4 != -1 && idx5 != -1 && idx6 != -1 && s.length == 7) {
                        String vorname = "";
                        if(s[0] != null && !s[0].isEmpty()) {
                            vorname = s[0].trim();
                        }
                        String nachname = "";
                        if(s[1] != null && !s[1].isEmpty()) {
                            nachname = s[1].trim();
                        }
                        String name = nachname + " " + vorname;
                        String courseName = "";
                        String ratingScale_saved = ratingABCD;
                        if(s[2] != null && !s[2].isEmpty()) {
                            courseName = s[2].trim();
                            loadSettings();
                            settings = settingsMap.get(courseName);
                            if(settings != null) {
                                ratingScale_saved = settings.getRatingScale();
                                settingsExistFlag = true;
                            }
                            else {
                                settingsExistFlag = false;
                            }
                        }
                        String date_str = "";
                        if(s[3] != null && !s[3].isEmpty()) {
                            date_str = s[3].trim();
                        }
                        int criterion = -1;
                        if(s[4] != null && !s[4].isEmpty()) {
                            criterion = Integer.parseInt(s[4].trim());
                        }
                        char rating = (char)32;
                        String ratingScale_imported = "";
                        if(s[5] != null && !s[5].isEmpty() && !s[5].isBlank()) {
                            rating = s[5].trim().charAt(0);
                            if(rating == '+' || rating == 'o' || rating == '-') {
                                ratingScale_imported = ratingPlusCircleMinus;
                                if(rating == '+') {
                                    rating = 'A';
                                }
                                else if(rating == 'o') {
                                    rating = 'B';
                                }
                                else {
                                    rating = 'C';
                                }
                            }
                            else if (rating == 'A' || rating == 'B' || rating == 'C' || rating == 'D') {
                                ratingScale_imported = ratingABCD;
                            }
                            else if (Character.isDigit(rating)) {
                                int i = Character.getNumericValue(rating);
                                if(0 < i && i < 7) {
                                    ratingScale_imported = rating1till6;
                                }
                            }
                        }
                        boolean flag = false;
                        if(s[6] != null && !s[6].isEmpty() && !s[6].isBlank()) {
                            if(s[6].trim().equals("true") || s[6].trim().equals("false")) {
                                flag = Boolean.parseBoolean(s[6]);
                            }
                        }
                        if(settingsExistFlag && (ratingScale_imported.equals(ratingScale_saved) || rating == (char)32)) {
                            if (ratingPerStudentMap.containsKey(courseName)) {
                                if (!ratingPerStudentMap.get(courseName).containsKey(name)) {
                                    ratingPerStudentMap.get(courseName).put(name, new ArrayList<RatingPerStudent>());
                                }
                            } else {
                                ratingPerStudentMap.put(courseName, new TreeMap<String, ArrayList<RatingPerStudent>>());
                                ratingPerStudentMap.get(courseName).put(name, new ArrayList<RatingPerStudent>());
                            }
                            //String key = courseName  + " " + name;
                            //Log.v(TAG, "rM3: " + key + " " + date_str + " " + criterion + " " + rating + " " + flag);
                            ratingPerStudentMap.get(courseName).get(name).add(new RatingPerStudent(courseName, vorname, nachname, date_str, criterion, rating, flag));
                        }
                        else {
                            //Log.v(TAG,"Die Bewertung\n" + row + "\nwurde nicht importiert.");
                            Toast.makeText(FileActivity.this, "Die Bewertung\n" + row + "\nwurde nicht gespeichert.", Toast.LENGTH_SHORT).show();
                            all_rows_valid = false;
                        }
                    }
                    else {
                        //Log.e(TAG,"Die Bewertung\n" + row + "\nwurde nicht importiert.");
                        Toast.makeText(FileActivity.this, "Die Bewertung\n" + row + "\nwurde nicht gespeichert.", Toast.LENGTH_SHORT).show();
                        all_rows_valid = false;
                    }
                    nr_rows++;
                }
                if (all_rows_valid) {
                    //Log.v(TAG, "Alle Bewertungen wurden importiert.");
                    Toast.makeText(FileActivity.this, "Alle Bewertungen wurden gespeichert.", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(this, "Kursdaten gespeichert ", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Kursdatenspeicherung fehlgeschlagen", Toast.LENGTH_SHORT).show();
            }
            intent = new Intent(this, MainActivity.class);
            //Log.v(TAG, "go back to display ratings");
        }
        else {
            intent = new Intent(this, MainActivity.class);
            //Log.v(TAG, "go back to main");
        }
        //startActivity(intent);
    }
    public void goHome(View view){
        if(saveFlag) {
            startActivity(intent);
        }
        else {
            intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }
}