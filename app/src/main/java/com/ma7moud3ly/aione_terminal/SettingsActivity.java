package com.ma7moud3ly.aione_terminal;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import androidx.appcompat.app.AppCompatActivity;


public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings); }

    public void clear_history(View v) {
        try {
            openOrCreateDatabase("settings", MODE_PRIVATE, null).execSQL("DROP TABLE history");
            Toast.makeText(this, "Commands History Was Cleared..", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "No History To Clear..", Toast.LENGTH_SHORT).show();
        }
    }

    public void about(View v) {
        startActivity(new Intent(this, AboutActivity.class));
    }

}
