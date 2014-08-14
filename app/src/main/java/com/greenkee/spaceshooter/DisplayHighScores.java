package com.greenkee.spaceshooter;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

import java.util.ArrayList;


public class DisplayHighScores extends SherlockActivity {
    TextView highScoreDisplay;
    String highScores;
    SharedPreferences sharedPrefs;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                // NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        initialize();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        highScoreDisplay.setText(displayHighScoreValues());

    }

    public String displayHighScoreValues(){
        ArrayList<String> highScoreArray = getHighScoreStrings();
        String message = "";
        for(int i = 0; i < highScoreArray.size(); i++){
            message += highScoreArray.get(i);
            message += "\n";

        }
        return message;
    }

    public ArrayList<String> getHighScoreStrings(){
        ArrayList<String> highScoreArray = new ArrayList<String>();
        for(int i = 0; i < SettingsActivity.maxHighScore; i++){
            String highScoreKey = "DATA_HIGH_SCORE_"+i;
            String highScore = "";
            try{
                highScore = sharedPrefs.getString(highScoreKey, "no value");
                int scoreIndex = highScore.indexOf(": ");

                if(scoreIndex > 0){
                    highScoreArray.add(highScore);
                }
            }catch (NullPointerException e){
                System.out.println("NO HIGH SCORE VALUE STORED");

            }
        }
        return highScoreArray;
    }

    private void initialize(){
        highScoreDisplay = (TextView)findViewById(R.id.high_score_text);
    }

}
