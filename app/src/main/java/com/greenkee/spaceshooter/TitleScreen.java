package com.greenkee.spaceshooter;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;

public class TitleScreen extends Activity implements View.OnClickListener{
    Button p1Button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_title_screen);

        if (savedInstanceState == null) {
        } else {
            super.onRestoreInstanceState(savedInstanceState);

        }

        initialize();

        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    private void initialize() {
        p1Button = (Button)findViewById(R.id.b1Player);
        p1Button.setOnClickListener(this);
    }

    public void startGame1(View view){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor prefEditor = prefs.edit();
        prefEditor.putString(SettingsActivity.PreferenceKeys.PREF_NUM_PLAYERS, "1");
        prefEditor.commit();

        Intent intent = new Intent(this, GameActivity.class);
        startActivity(intent);
    }

    public void startGame2(View view){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor prefEditor = prefs.edit();
        prefEditor.putString(SettingsActivity.PreferenceKeys.PREF_NUM_PLAYERS, "2");
        prefEditor.commit();

        Intent intent = new Intent(this, GameActivity.class);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case (R.id.b1Player):
                startGame1(v);
                break;
        }
    }
}

