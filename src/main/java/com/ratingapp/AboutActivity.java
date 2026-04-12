package com.ratingapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class AboutActivity extends AppCompatActivity {
    private Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        TextView textView = findViewById(R.id.readLicense);
        String text = readFile("License.txt");
        textView.setText(text);
    }
    private String readFile(String file_str) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            InputStream is = getAssets().open(file_str);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = br.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
            return "Fehler beim Lesen von License.txt";
        }
        return stringBuilder.toString();
    }
    public void openDoc(View view){
        String url = "https://github.com/ReneJursa/RatingApp/";
        intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }
    public void goHome(View view) {
        intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
