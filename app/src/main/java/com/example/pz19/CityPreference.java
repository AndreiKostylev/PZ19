package com.example.pz19;

import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatActivity;

public class CityPreference {
    SharedPreferences prefs;

    public CityPreference(AppCompatActivity activity) {
        prefs = activity.getPreferences(AppCompatActivity.MODE_PRIVATE);
    }

    String getCity() {
        return prefs.getString("city", "Москва");
    }

    void setCity(String city) {
        prefs.edit().putString("city", city).apply();
    }
}