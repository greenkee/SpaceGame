package com.greenkee.spaceshooter;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;

public class SettingsActivity extends SherlockPreferenceActivity {
    public static int maxHighScore = 5;
    class PreferenceKeys {
        public static final String PREF_PADDLE_SPEED = "paddle_speed";
        public static final String PREF_BALL_INCREMENT = "ball_increment";
        public static final String PREF_NUM_BALLS = "num_ball";
        public static final String PREF_MAX_SCORE = "max_score";
        public static final String PREF_SOUND_ENABLED = "sound_enabled";
        public static final String PREF_NUM_PLAYERS = "num_players";
        public static final String PREF_AI_DIFFICULTY = "ai_difficulty";
        public static final String PREF_ENEMY_SPAWN_RATE = "enemy_spawn_rate";
        public static final String PREF_POWERUP_SPAWN_RATE = "powerup_spawn_rate";
        public static final String PREF_FIRE_RATE = "player_fire_rate";

        public static final String DATA_HIGH_SCORE_0 = "high_score_0";
        public static final String DATA_HIGH_SCORE_1 = "high_score_1";
        public static final String DATA_HIGH_SCORE_2 = "high_score_2";
        public static final String DATA_HIGH_SCORE_3 = "high_score_3";
        public static final String DATA_HIGH_SCORE_4 = "high_score_4";
    }
	
	SharedPreferences prefs;
	SharedPreferences.OnSharedPreferenceChangeListener changeListener;



	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	addPreferencesFromResource(R.xml.preferences);
	prefs = getPreferenceScreen().getSharedPreferences();

	
	ActionBar actionBar = getSupportActionBar();
    actionBar.setDisplayHomeAsUpEnabled(true);
    
    setVolumeControlStream(AudioManager.STREAM_MUSIC);
	}
	
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
	protected void onResume() {
	    super.onResume();
	    
	     changeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
	    	  public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
                  if(key.equals(PreferenceKeys.PREF_ENEMY_SPAWN_RATE)){
                      SharedPreferences.Editor prefEditor = prefs.edit();
                      if (prefs.getString(key, "1").length() < 1  ){
                          throwInvalidNumber(prefEditor, key, 1);
                      }else {
                          try{
                              double rate = Double.parseDouble(prefs.getString(key, "1"));
                              if (rate  <= 0 ){

                                  String message;
                                  prefEditor.putString(key, Double.toString(EnemyShip.minCooldown));
                                  message = Double.toString(EnemyShip.minCooldown);

                                  displayToast("Please enter a number greater than 0");

                                  prefEditor.commit();

                                  EditTextPreference prefText = (EditTextPreference) findPreference(key);
                                  prefText.setText(message);

                              }else if (rate <= EnemyShip.minCooldown){
                                  String message;
                                  prefEditor.putString(key, Double.toString(EnemyShip.minCooldown));
                                  message = Double.toString(EnemyShip.minCooldown);

                                  displayToast("Number too small! Do you want to die?");

                                  prefEditor.commit();

                                  EditTextPreference prefText = (EditTextPreference) findPreference(key);
                                  prefText.setText(message);
                              }
                          } catch(NumberFormatException e){
                              throwInvalidNumber(prefEditor, key, 1);
                          }
                      }

                  }
                  if(key.equals(PreferenceKeys.PREF_POWERUP_SPAWN_RATE)){
                      SharedPreferences.Editor prefEditor = prefs.edit();
                      if (prefs.getString(key, "10").length() < 1  ){
                          throwInvalidNumber(prefEditor, key, 1);
                      }else {
                          try{
                              double rate = Double.parseDouble(prefs.getString(key, "10"));
                              if (rate  <= 0 ){

                                  String message;
                                  prefEditor.putString(key, Double.toString(PowerUp.minPowerUpCooldown));
                                  message = Double.toString(PowerUp.minPowerUpCooldown);

                                  displayToast("Please enter a number greater than 0");

                                  prefEditor.commit();

                                  EditTextPreference prefText = (EditTextPreference) findPreference(key);
                                  prefText.setText(message);

                              }else if (rate <= PowerUp.minPowerUpCooldown){
                                  String message;
                                  prefEditor.putString(key, Double.toString(PowerUp.minPowerUpCooldown));
                                  message = Double.toString(PowerUp.minPowerUpCooldown);

                                  displayToast("Power OVERWHELMING! Too much power, please enter a larger number!");

                                  prefEditor.commit();

                                  EditTextPreference prefText = (EditTextPreference) findPreference(key);
                                  prefText.setText(message);
                              }
                          } catch(NumberFormatException e){
                              throwInvalidNumber(prefEditor, key, 1);
                          }
                      }

                  }
                  if(key.equals(PreferenceKeys.PREF_FIRE_RATE)){
                      SharedPreferences.Editor prefEditor = prefs.edit();
                      if (prefs.getString(key, ".25").length() < 1  ){
                          throwInvalidNumber(prefEditor, key, 1);
                      }else {
                          try{
                              double rate = Double.parseDouble(prefs.getString(key, ".25"));
                              if (rate  <= 0){

                                  String message;
                                  prefEditor.putString(key, Double.toString(PlayerShip.minWeaponCooldown));
                                  message = Double.toString(PlayerShip.minWeaponCooldown);

                                  displayToast("Please enter a number greater than 0");

                                  prefEditor.commit();

                                  EditTextPreference prefText = (EditTextPreference) findPreference(key);
                                  prefText.setText(message);

                              }else if (rate <= PlayerShip.minWeaponCooldown){
                                  String message;
                                  prefEditor.putString(key, Double.toString(PlayerShip.minWeaponCooldown));
                                  message = Double.toString(PlayerShip.minWeaponCooldown);

                                  displayToast("Sorry, there is not enough light for all these lasers, please enter a smaller number");

                                  prefEditor.commit();

                                  EditTextPreference prefText = (EditTextPreference) findPreference(key);
                                  prefText.setText(message);
                              }
                          } catch(NumberFormatException e){
                              throwInvalidNumber(prefEditor, key, .25);
                          }
                      }

                  }
                  /*
	    			if (key.equals(PreferenceKeys.PREF_PADDLE_SPEED)){
	    				//Preference connectionPref = findPreference(key);
	    				//connectionPref.setSummary(preferences.getString(key, "Controls how fast the paddles move"));
	    			}
	    			if(key.equals(PreferenceKeys.PREF_BALL_INCREMENT)){

	    			}
	    			if(key.equals(PreferenceKeys.PREF_NUM_BALLS)){
	    				
	    				SharedPreferences.Editor prefEditor = prefs.edit();
	    				if (prefs.getString(key, "1").length() < 1  ){
	    					throwInvalidNumber(prefEditor, key, 1);
	    				}else {
	    					try{
	    						int balls = Integer.parseInt(prefs.getString(key, "1"));
		    					if (balls > GameActivity.MAX_MULTI_BALL || balls < 1){
		    	    				
			    					String message;
			    					if (balls > GameActivity.MAX_MULTI_BALL){
			    						prefEditor.putString(key, Integer.toString(GameActivity.MAX_MULTI_BALL));
			    						message = Integer.toString(GameActivity.MAX_MULTI_BALL);
			    					}else{
			    						prefEditor.putString(key, Integer.toString(1));
			    						message = Integer.toString(1);
			    					}
			    					
			    					displayToast("Please enter a number from 1 to " + Integer.toString(GameActivity.MAX_MULTI_BALL));
			    					
			    					prefEditor.commit();
			    					
			    					EditTextPreference prefText = (EditTextPreference) findPreference(key);
			    					prefText.setText(message);
			    					
			    				}
	    					} catch(NumberFormatException e){
	    						throwInvalidNumber(prefEditor, key, 1);
	    					}
	    				}
	    			}
	    			if(key.equals(PreferenceKeys.PREF_MAX_SCORE)){
	    				SharedPreferences.Editor prefEditor = prefs.edit();
	    				if (prefs.getString(key, "7").length() < 1  ){
	    					throwInvalidNumber(prefEditor, key, 7);
	    				} else {
	    					try{
	    						int score = Integer.parseInt(prefs.getString(key, "7"));
		    					if ( score < 1){
		    	    				
			    					prefEditor.putString(key, "1");
			    					
			    					
			    					displayToast("Maximum score must be greater than 0");
			    					prefEditor.commit();
			    					
			    					EditTextPreference prefText = (EditTextPreference) findPreference(key);
			    					prefText.setText("1");
			    					
			    				}
	    					} catch (NumberFormatException e){
	    						throwInvalidNumber(prefEditor, key, 7);
	    					}

	    			}}*/
	    	  }
	    	};
	    	prefs.registerOnSharedPreferenceChangeListener(changeListener);
	}

	@Override
	protected void onPause() {
	    super.onPause();
	    prefs.unregisterOnSharedPreferenceChangeListener(changeListener);
	}
	    
	protected void displayToast(String message){
		Context context = getApplicationContext();
		CharSequence text = message;
		int duration = Toast.LENGTH_SHORT;
		
		Toast toast = Toast.makeText(context, text, duration);
		toast.show();
	}
	
	protected void throwInvalidNumber(SharedPreferences.Editor editor, String key, double n){
		editor.putString(key, Double.toString(n));
		
		displayToast("Please enter a valid number");

		editor.commit();
		
		EditTextPreference prefText = (EditTextPreference) findPreference(key);
		prefText.setText(Double.toString(n));
	}
}
