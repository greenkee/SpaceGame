/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.greenkee.spaceshooter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.MotionEventCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.greenkee.spaceshooter.sql.EnemySQLHelper;
import com.greenkee.spaceshooter.sql.PowerUpSQLHelper;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This is a simple LunarLander activity that houses a single View. It
 * demonstrates...
 * <ul>
 * <li>animating by calling invalidate() from draw()
 * <li>loading and drawing resources
 * <li>handling onPause() in an animation
 * </ul>
 */
public class GameActivity extends Activity {
    public static EnemySQLHelper enemyHelper;
    ArrayList<HashMap<String, String>> enemyInfoList;

    public static PowerUpSQLHelper powerUpHelper;


    public static final int MAX_MULTI_BALL = 30;
    private static final int MENU_BUTTON_1 = 1;
    private static final int MENU_BUTTON_2 = 2;
    private static final int MENU_BUTTON_3 = 3;
    private static final int MENU_BUTTON_4 = 4;

    public static int numTouches = 0;
    public static final int MAX_TOUCHES = 2;

    float p1X = -1, p1Y = -1, p2X = -1, p2Y = -1;
    int p1ID = -1, p2ID = -1;

    public static SoundPool audio;
    public static int destroySound, gemSound, bombSound;
    public static int playerDeathSound;

    /**
     * A handle to the thread that's actually running the animation.
     */
    private GameView.GameThread mGameThread;

    /**
     * A handle to the View in which the game is running.
     */
    private GameView mGameView;


    /**
     * Invoked during init to give the Activity a chance to set up its Menu.
     *
     * @param menu the Menu to which entries may be added
     * @return true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, MENU_BUTTON_1, 0, getString(R.string.reset_title));
        menu.add(0, MENU_BUTTON_3, 0, getString(R.string.pause_title));
        menu.add(0, MENU_BUTTON_2, 0, getString(R.string.settings_title));
        menu.add(0, MENU_BUTTON_4, 0, "High Scores");

        return true;
    }

    /**
     * Invoked when the user selects an item from the Menu.
     *
     * @param item the Menu entry which was selected
     * @return true if the Menu item was legit (and we consumed it), false
     * otherwise
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_BUTTON_1:
                mGameThread.resetGame();
                return true;
            case MENU_BUTTON_2:
                goToSettings(mGameView);
                return true;
            case MENU_BUTTON_3:
                mGameThread.pause();
                return true;
            case MENU_BUTTON_4:
                goToHighScores();
                return true;

        }

        return false;
    }

    public void goToSettings(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }


    public void goToHighScores() {
        Intent i = new Intent(this, DisplayHighScores.class);
        startActivity(i);
    }

    public void resetGame(View view) {
        mGameThread.resetGame();
    }

    public void pauseGame(View view) {
        mGameThread.pause();
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (!mGameView.piecesCreated) {
            Button pauseButton = (Button) findViewById(R.id.bPause);
            mGameThread.screenSizeObtained(pauseButton.getBottom());
        }
    }


    private void loadAudio() {
        audio = new SoundPool(30, AudioManager.STREAM_MUSIC, 0);
        destroySound = audio.load(this, R.raw.laser4, 1);
        gemSound = audio.load(this, R.raw.power_up_gem, 1);
        bombSound = audio.load(this, R.raw.power_up_bomb, 1);
        playerDeathSound = audio.load(this, R.raw.low_down, 1);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // tell system to use the layout defined in our XML file
        setContentView(R.layout.activity_game_screen);
        loadAudio();
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        enemyHelper = new EnemySQLHelper(this.getApplicationContext());

        HashMap<String, String> infoMap = enemyHelper.getInfo("1");
        if(infoMap.size() <= 0){
            enemyHelper.loadInitialData();
            System.out.println("SIZE: " + infoMap.size());
            System.out.println("NAME: " + infoMap.get("enemyName"));
        }


        mGameView = (GameView) findViewById(R.id.gameScreen);
        mGameThread = mGameView.getThread();

        mGameView.setTextViews((TextView) findViewById(R.id.status_display), (TextView) findViewById(R.id.score_display));


        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        //false to default only if never called before, true to default every time method called


        if (savedInstanceState == null) {
            // we were just launched: set up a new game

            mGameThread.setState(GameView.GameThread.STATE_READY);
        } else {
            super.onRestoreInstanceState(savedInstanceState);
            // we are being restored: resume a previous game
            mGameThread.restoreState(savedInstanceState);

        }
        mGameView.setOnTouchListener(new OnTouchListener() {

                                         @Override
                                         public boolean onTouch(View v, MotionEvent event) {
                                             int action = MotionEventCompat.getActionMasked(event);


                                             final int pointerIndex;
                                             final float x;
                                             final float y;

                                             switch (action) {
                                                 case (MotionEvent.ACTION_DOWN): {
                                                     //  System.out.println("ACTION_DOWN");
                                                     if (mGameThread.getCurrentState() == GameView.GameThread.STATE_RUNNING) {
                                                         int index = MotionEventCompat.getActionIndex(event);
                                                         p1ID = MotionEventCompat.getPointerId(event, index);
                                                         if (p1ID != -1) {
                                                             int p1Index = MotionEventCompat.findPointerIndex(event, p1ID);
                                                             mGameView.mShip.move((int) MotionEventCompat.getX(event, p1Index),
                                                                     (int) MotionEventCompat.getY(event, p1Index));
                                                         }
                                                     } else if (mGameThread.getCurrentState() == GameView.GameThread.STATE_PAUSE) {
                                                         mGameThread.unpause();
                                                     } else if (!(mGameView.dialogOpen) && (!(mGameThread.gameOver)) && (mGameThread.getCurrentState() == GameView.GameThread.STATE_READY) || (mGameThread.getCurrentState() == GameView.GameThread.STATE_LOSE)) {
                                                         mGameThread.doStart();

                                                        // System.out.println("START GAME");

                                                     }

                                                     return true;
                                                 }
                    /*
                    case (MotionEvent.ACTION_POINTER_DOWN): {
                            int index = MotionEventCompat.getActionIndex(event);
                            p1ID = MotionEventCompat.getPointerId(event, index);
                            mGameView.mShip.move( (int) MotionEventCompat.getX(event, index) ,
                                (int)MotionEventCompat.getY(event, index));
                        return true;
                    }*/
                                                 case (MotionEvent.ACTION_MOVE): {
                                                     //  System.out.println("ACTION_MOVE");
                                                     if (mGameThread.getCurrentState() == GameView.GameThread.STATE_RUNNING) {
                                                         if (p1ID != -1) {

                                                             int p1Index = MotionEventCompat.findPointerIndex(event, p1ID);
                                                             mGameView.mShip.move((int) MotionEventCompat.getX(event, p1Index),
                                                                     (int) MotionEventCompat.getY(event, p1Index));
                                                         }
                                                     }


                                                     return true;
                                                 }
                    /*
                    case (MotionEvent.ACTION_POINTER_UP): {
                        int index = MotionEventCompat.getActionIndex(event);
                        return true;
                    }*/
                                                 case (MotionEvent.ACTION_UP): {
                                                     // System.out.println("ACTION_UP");

                                                     reset();
                                                     return true;
                                                 }
                                                 case (MotionEvent.ACTION_CANCEL): {
                                                     // System.out.println("ACTION_CANCEL");
                                                     reset();
                                                     return true;
                                                 }
                                                 case (MotionEvent.ACTION_OUTSIDE): {
                                                     return true;

                                                 }
                                             }

                                             return false;
                                         }


                                         private void reset() {
                                             numTouches = 0;
                                             mGameView.mShip.stop();
                                             p1ID = -1;
                                             p2ID = -1;
                                         }
                                     }
        );
    }

    protected void displayToast(String message) {
        Context context = getApplicationContext();
        CharSequence text = message;
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mGameThread.checkSettings(sharedPrefs);
        mGameThread.resumeScreen();
    }

    /**
     * Invoked when the Activity loses user focus.
     */
    @Override
    protected void onPause() {
        mGameView.getThread().pause(); // pause game when Activity pauses
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        audio.release();
        super.onDestroy();
    }

    /**
     * Notification that something is about to happen, to give the Activity a
     * chance to save state.
     *
     * @param outState a Bundle into which this Activity should save its state
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // just have the View's thread save its state into our Bundle
        super.onSaveInstanceState(outState);
        mGameThread.saveState(outState);
    }


}
