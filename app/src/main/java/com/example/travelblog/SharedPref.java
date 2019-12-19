package com.example.travelblog;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPref {
    SharedPreferences preferences;

    public SharedPref(Context context) {
        preferences = context.getSharedPreferences("filename", Context.MODE_PRIVATE);
    }

    public void setNightModeState(Boolean state) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("Nightmode", state);
        editor.commit();
    }

    public Boolean loadNightMode(){
        Boolean state = preferences.getBoolean("Nightmode", false);
        return state;
    }
}
