package com.example.travelblog.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.example.travelblog.R;
import com.example.travelblog.SharedPref;

public class SettingActivity extends AppCompatActivity {

    private Toolbar settingToolbar;

    private Switch aSwitch;
    SharedPref pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        pref = new SharedPref(this);
        if (pref.loadNightMode() == true) {
            setTheme(R.style.DarkTheme);
        }else{
            setTheme(R.style.AppTheme);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        settingToolbar = findViewById(R.id.setting_toolbar);
        setSupportActionBar(settingToolbar);
        getSupportActionBar().setTitle("Setting");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        aSwitch = findViewById(R.id.switchMode);
        if (pref.loadNightMode() == true) {
            aSwitch.setChecked(true);
        }
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked){
                    pref.setNightModeState(true);
                    restartApp();
                } else {
                    pref.setNightModeState(false);
                    restartApp();
                }
            }
        });
    }

    private void restartApp() {
        startActivity(new Intent(getApplicationContext(),MainActivity.class));
        finish();
    }
}
