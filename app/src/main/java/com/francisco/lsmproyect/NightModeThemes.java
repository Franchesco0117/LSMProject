package com.francisco.lsmproyect;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.SharedPreferences;

public class NightModeThemes extends AppCompatActivity {
    SharedPreferences sharedPreferences;
    boolean isNightMode;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        isNightMode = sharedPreferences.getBoolean("nightMode", false);

        if (isNightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
    }
}
