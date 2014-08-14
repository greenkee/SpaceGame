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


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


/**
 * View that draws, takes keystrokes, etc. for a simple LunarLander game.
 * <p/>
 * Has a mode which RUNNING, PAUSED, etc. Has a x, y, dx, dy, ... capturing the
 * current ship physics. All x/y etc. are measured with (0,0) at the lower left.
 * updatePhysics() advances the physics based on realtime. draw() renders the
 * ship, and does an invalidate() to prompt another draw() as soon as possible
 * by the system.
 */
class GameView extends SurfaceView implements SurfaceHolder.Callback {
    int myCanvas_w, myCanvas_h;
    Bitmap myCanvasBitmap = null;
    Canvas myCanvas = null;
    Matrix identityMatrix;

    public boolean dialogOpen = false;

    public static boolean soundEnabled;

    public static float screenWidth = 0, screenHeight = 0;

    public boolean piecesCreated = false;

    public int numPlayers;

    public PlayerShip mShip;

    public static ArrayList<Projectile> playerProjectiles;

    public static ArrayList<EnemyShip> enemyShipList;

    public static ArrayList<PowerUp> powerUpList;



    class GameThread extends Thread implements Runnable {


        private Object mPauseLock;
        private boolean mPaused;
        private boolean mFinished;

        public static final int STATE_LOSE = 1;
        public static final int STATE_PAUSE = 2;
        public static final int STATE_READY = 3;
        public static final int STATE_RUNNING = 4;
        public static final int STATE_WIN = 5;
        public static final int STATE_SCORE = 6;


        private float sideBuffer;
        public float bottomBuffer;
        public float screenBottom;

        private int playerScore;



        private int mCanvasHeight = 1;

        private int mCanvasWidth = 1;

        public boolean gameOver = false;

        /**
         * Message handler used by thread to interact with TextView
         */
        private Handler mHandler;

        /**
         * Used to figure out elapsed time between frames
         */
        private long mLastTime;

        //Paints
        private Paint scorePaint;

        // The state of the game. One of READY, RUNNING, PAUSE, LOSE, or WIN
        private int mMode;

        /**
         * Indicate whether the surface has been created & is ready to draw
         */
        private boolean mRun = false;

        private final Object mRunLock = new Object();

        /**
         * Handle to the surface manager object we interact with
         */
        private SurfaceHolder mSurfaceHolder;

        private Bundle mBundle;

        //private boolean drawEnabled = false;

        private Canvas c;

        private String score;

        private float p1StartX, p1StartY;

        Drawable mShipImage;


        private SharedPreferences sharedPrefs;

        public GameThread(SurfaceHolder surfaceHolder, Context context,
                          Handler handler) {
            // get handles to some important objects
            mSurfaceHolder = surfaceHolder;
            mHandler = handler;
            mContext = context;

            Resources res = context.getResources();
            /* how to load sprites, drawables
            mLanderImage = context.getResources().getDrawable(
                    R.drawable.lander_plain);
            */

            // load background image as a Bitmap instead of a Drawable b/c
            // we don't need to transform it and it's faster to draw this way
            // mBackgroundImage = BitmapFactory.decodeResource(res,
            //         R.drawable.earthrise);

            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics metrics = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(metrics);

            screenWidth = metrics.widthPixels;
            screenHeight = metrics.heightPixels;


            sideBuffer = screenWidth / 15;


            bottomBuffer = 0;// screenHeight / 10;


            scorePaint = new Paint();
            scorePaint.setARGB(255, 255, 255, 255);

            mBundle = new Bundle();


            score = "" + 0;

            Message msg = mHandler.obtainMessage();
            mBundle.putString("score_text", score);
            msg.setData(mBundle);
            mHandler.sendMessage(msg);

            mPauseLock = new Object();
            mPaused = false;
            mFinished = false;

            sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

            playerProjectiles = new ArrayList<Projectile>();
            enemyShipList = new ArrayList<EnemyShip>();
            powerUpList = new ArrayList<PowerUp>();


            Drawable sampleProjectile = getResources().getDrawable(R.drawable.laser_green);
            Projectile.setLaserWidth(sampleProjectile.getIntrinsicWidth());

        }

        public void screenSizeObtained(float buffer) {
            bottomBuffer = buffer;
            screenBottom = screenHeight - bottomBuffer;

            System.out.println("Bottom:" + screenBottom);


            mShip = new PlayerShip(getContext());

            p1StartX = (screenWidth / 2) - (mShip.getWidth() / 2);
            p1StartY = ((float) ((screenBottom) * .8)) - (mShip.getHeight() / 2);
            mShip.setStartLoc(p1StartX, p1StartY);

            System.out.println("PLAYER START: " + p1StartX + ", " + p1StartY);

            piecesCreated = true;
            checkSettings(sharedPrefs);
        }

        public void reset() {
            mShip.reset();
            EnemyShip.currentEnemyCounter = 0;
            PowerUp.currentPowerUpCounter = 0;
            powerUpList.clear();
            enemyShipList.clear();
            playerProjectiles.clear();
        }

        /**
         * Starts the game, setting parameters for the current difficulty.
         */
        public void doStart() {
            reset();
            dialogOpen = false;


            playerScore = 0;
            score = "" + playerScore;
            Message msg = mHandler.obtainMessage();
            mBundle.putString("score_text", score);
            msg.setData(mBundle);
            mHandler.sendMessage(msg);

            c = mSurfaceHolder.lockCanvas(null);



            synchronized (mSurfaceHolder) {

                clear(c);
                mSurfaceHolder.unlockCanvasAndPost(c);
                mLastTime = System.currentTimeMillis() + 100;
                setState(STATE_RUNNING);
            }
        }

        public void pause() {
            synchronized (mSurfaceHolder) {
                if (mMode == STATE_RUNNING) setState(STATE_PAUSE);

            }
        }

        public void unpause() {
            // Move the real time clock up to now
            synchronized (mSurfaceHolder) {
                mLastTime = System.currentTimeMillis() + 100;
            }
            setState(STATE_RUNNING);
        }

        public void onPause() {
            synchronized (mPauseLock) {
                mPaused = true;
            }
        }

        public void onResume() {
            synchronized (mPauseLock) {
                mPaused = false;
                mPauseLock.notifyAll();
            }
        }

        @Override
        public void run() {
            c = null;
            try {
                Thread.sleep(500);
            } catch (Exception ignored) {
            }

            while (!mFinished) {
                try {
                    c = mSurfaceHolder.lockCanvas();
                    synchronized (mSurfaceHolder) {

                        if (mMode == STATE_RUNNING) updateGame();
                        synchronized (mRunLock) {
                            if (mRun) {
                                clear(c);
                                if (piecesCreated) {
                                    doDraw(c);
                                }
                            }
                        }
                    }
                } finally {
                    if (c != null) {
                        mSurfaceHolder.unlockCanvasAndPost(c);
                        if (gameOver) {
                            resetGame();
                        }
                    }
                }
                synchronized (mPauseLock) {
                    while (mPaused) {
                        try {
                            mPauseLock.wait();
                        } catch (InterruptedException e) {

                        }
                    }
                }

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        /**
         * Dump game state to the provided Bundle. Typically called when the
         * Activity is being suspended.
         *
         * @return Bundle with this view's state
         */
        public Bundle saveState(Bundle map) {
            synchronized (mSurfaceHolder) {
                if (map != null) {
                  /*  map.putFloat(KEY_X, ball1.getX());
                    map.putFloat(KEY_Y, ball1.getY());
                    map.putFloat(KEY_DX, ball1.getSpeedX());
                    map.putFloat(KEY_DY, ball1.getSpeedY());*/
                }
            }
            return map;

        }

        /**
         * Restores game state from the indicated Bundle. Typically called when
         * the Activity is being restored after having been previously
         * destroyed.
         *
         * @param savedState Bundle containing the game state
         */
        public synchronized void restoreState(Bundle savedState) {
            synchronized (mSurfaceHolder) {
                setState(STATE_PAUSE);
                /*ball1.setPosition(savedState.getFloat(KEY_X), savedState.getFloat(KEY_Y));
                ball1.setSpeedX(savedState.getFloat(KEY_DX));
                ball1.setSpeedY(savedState.getFloat(KEY_DY));*/
            }
        }

        public void setRunning(boolean b) {
            // Do not allow mRun to be modified while any canvas operations
            // are potentially in-flight. See doDraw().
            synchronized (mRunLock) {
                mRun = b;
            }
        }

        public void setState(int mode) {
            synchronized (mSurfaceHolder) {
                setState(mode, null);
            }
        }

        public int getCurrentState() {
            return mMode;
        }

        public void setState(int mode, CharSequence message) {
            synchronized (mSurfaceHolder) {
                mMode = mode;

                if (mMode == STATE_RUNNING) {
                    //drawEnabled = true;
                    onResume();
                    Message msg = mHandler.obtainMessage();
                    mBundle.putString("status_text", "");
                    mBundle.putInt("status_viz", View.INVISIBLE);
                    msg.setData(mBundle);
                    mHandler.sendMessage(msg);
                } else {
                    Resources res = mContext.getResources();
                    CharSequence strStatus = "";

                    if (mMode == STATE_READY) {
                        //drawEnabled = false;
                        strStatus = res.getText((R.string.mode_ready_text));
                    } else if (mMode == STATE_PAUSE) {
                        //drawEnabled = false;
                        strStatus = res.getText(R.string.mode_paused_text);
                        onPause();
                    }
                    if (message != null) {
                        strStatus = message + "\n" + strStatus;
                    }

                    Message msg = mHandler.obtainMessage();
                    mBundle.putString("status_text", strStatus.toString());
                    mBundle.putInt("status_viz", View.VISIBLE);
                    msg.setData(mBundle);
                    mHandler.sendMessage(msg);
                }
            }
        }


        /* Callback invoked when the surface dimensions change. */
        public void setSurfaceSize(int width, int height) {
            // synchronized to make sure these all change atomically
            synchronized (mSurfaceHolder) {
                mCanvasWidth = width;
                mCanvasHeight = height;

                // don't forget to resize the background image
                // mBackgroundImage = Bitmap.createScaledBitmap(
                //         mBackgroundImage, width, height, true);
            }
        }

        public void resumeScreen() {
            System.out.println("RESUME SCREEN");
            try {
                c = mSurfaceHolder.lockCanvas();
                synchronized (mSurfaceHolder) {
                    synchronized (mRunLock) {
                        if (mRun) {
                            clear(c);
                            doDraw(c);
                        }
                    }
                }
            } finally {
                if (c != null) {
                    mSurfaceHolder.unlockCanvasAndPost(c);
                }
            }
        }


        private void clear(Canvas canvas) {
            myCanvas.drawARGB(255, 0, 0, 0);
            canvas.drawBitmap(myCanvasBitmap, identityMatrix, null);

        }


        private void doDraw(Canvas canvas) {
            checkObjectBoundaries();

            mShip.draw(myCanvas);
            drawProjectiles(myCanvas);
            drawEnemyShips(myCanvas);
            drawPowerUps(myCanvas);

            canvas.drawBitmap(myCanvasBitmap, identityMatrix, null);

        }

        private void updateGame() {
            long now = System.currentTimeMillis();

            // Do nothing if mLastTime is in the future.
            // This allows the game-start to delay the start of the physics
            // by 100ms or whatever.
            if (mLastTime > now) return;


            double elapsed = (now - mLastTime) / 1000.0;


            updateProjectiles(elapsed);
            updatePlayer(elapsed);
            updateEnemyShips(elapsed);
            updatePowerUps(elapsed);


            mLastTime = now;
        }

        private void drawPowerUps(Canvas c){
            for(PowerUp pUp: powerUpList){
                pUp.draw(c);
            }
        }

        private void drawEnemyShips(Canvas c) {
            for (EnemyShip es : enemyShipList) {
                es.draw(c);
            }
        }

        private void updatePlayer(double elapsed) {
            mShip.update(elapsed);
            int i = 0;
            boolean finished = false;
            while ((i < enemyShipList.size()) && !(finished)) {
                EnemyShip es = enemyShipList.get(i);
                if (mShip.getVulnerable() && (mShip.inContact(es.getX(), es.getY()) || mShip.inContact(es.getX() + es.getWidth(), es.getY()) ||
                        mShip.inContact(es.getX(), es.getY() + es.getHeight()) || mShip.inContact(es.getX() + es.getWidth(), es.getY() + es.getHeight()))) {
                    if (!(es.getComponentName().equals("asteroid"))) {
                        if (es.getVulnerable()) {
                            destroyPlayerShip();
                            finished = true;
                        }
                    } else {
                        destroyPlayerShip();
                        finished = true;
                    }

                }
                i++;
            }
            int j = 0;
            while(j< powerUpList.size()){
                PowerUp pUp = powerUpList.get(j);
                if(pUp.getCollectable() && mShip.inContact(pUp.getX(), pUp.getY()) || mShip.inContact(pUp.getX() + pUp.getWidth(), pUp.getY()) ||
                        mShip.inContact(pUp.getX(), pUp.getY() + pUp.getHeight()) || mShip.inContact(pUp.getX() + pUp.getWidth(), pUp.getY() + pUp.getHeight())){
                    if(pUp instanceof GemPowerUp){
                        updateScore(1000);
                    }else if(pUp instanceof BombPowerUp){
                        bombTriggered();

                    }
                    powerUpList.remove(j);
                    pUp.destroy();
                }
                j++;
            }

        }

        private void bombTriggered(){
            while(enemyShipList.size() > 0){
                EnemyShip es = enemyShipList.get(0);
                es.destroy();
                enemyShipList.remove(0);
                updateScore(es.getPointValue());
            }

        }

        private void createPowerUp() {
            PowerUp pUp;
            double rand = Math.random();
            if (rand > .5) {
                pUp = new BombPowerUp(getContext());
            } else{
                pUp = new GemPowerUp(getContext());
            }
            System.out.println("POWER UP CREATED");
            powerUpList.add(pUp);
        }


        private void createEnemyShip() {
            EnemyShip es;
            double rand = Math.random();
            if (rand > .85) {
                es = new EnemyShip(getContext(), GameActivity.enemyHelper.getInfo("3"));
            } else if (rand > .7) {
                es = new EnemyShip(getContext(), GameActivity.enemyHelper.getInfo("2"));
            } else {
                es = new EnemyShip(getContext(), GameActivity.enemyHelper.getInfo("1"));
            }
            enemyShipList.add(es);
        }

        private void updatePowerUps(double e){
            PowerUp.currentPowerUpCounter += e;
            if (PowerUp.currentPowerUpCounter >PowerUp.powerUpCooldown) {
                System.out.println("PUP CT:" + PowerUp.currentPowerUpCounter);
                createPowerUp();
                PowerUp.currentPowerUpCounter = 0;
            } else if (PowerUp.currentPowerUpProgress >= PowerUp.pointsForPowerUp){
                System.out.println("PUP PROG:" + PowerUp.currentPowerUpProgress);
                PowerUp.currentPowerUpProgress = 0;
                createPowerUp();
            }

            int i = 0;
            while (i < powerUpList.size()) {
                PowerUp pUp = powerUpList.get(i);
                pUp.update(e);
                i++;

            }
        }

        private void updateEnemyShips(double e) {
            EnemyShip.currentEnemyCounter += e;
            if(EnemyShip.enemyCooldown > EnemyShip.minCooldown){
                EnemyShip.enemyCooldown -= (e/100);
            }
            if (EnemyShip.currentEnemyCounter > EnemyShip.enemyCooldown) {
                createEnemyShip();
                EnemyShip.currentEnemyCounter = 0;
            }

            int i = 0;
            while (i < enemyShipList.size()) {
                EnemyShip es = enemyShipList.get(i);

                for (int j = 0; j < playerProjectiles.size(); j++) {
                    Projectile p = playerProjectiles.get(j);
                    if ((es.inContact(p.getX(), p.getY()) || es.inContact(p.getX() + p.getWidth(), p.getY()) ||
                            es.inContact(p.getX(), p.getY() + p.getHeight()) || es.inContact(p.getX() + p.getWidth(), p.getY() + p.getHeight()))) {
                        if ((es.getComponentName().equals("asteroid")) ) {
                            playerProjectiles.remove(j);}
                        else if (es.getVulnerable()) {
                            es.destroy();
                            enemyShipList.remove(i);
                            updateScore(es.getPointValue());
                            playerProjectiles.remove(j);
                        }
                        break;
                    }
                }
                i++;
                es.update(e);
            }
        }


        private void checkEnemyShipBoundaries() {
            int i = 0;
            while (i < enemyShipList.size()) {
                EnemyShip es = enemyShipList.get(i);
                if ((es.getY() < 0) || (es.getY() + es.getHeight() > screenBottom)) {
                    if (es.getY() + es.getHeight() > screenBottom) {
                        enemyShipList.remove(es);
                        i--;
                    } else {
                        es.setPosition(es.getX(), 0);
                    }
                }

                if ((es.getX() < 0) || (es.getX() + es.getWidth() > screenWidth)) {
                    if (es.getX() + es.getWidth() > screenWidth) {
                        es.setPosition(screenWidth - es.getWidth(), es.getY());
                    } else {
                        es.setPosition(0, es.getY());
                    }
                }
                i++;
            }
        }

        private void drawProjectiles(Canvas c) {
            for (Projectile p : playerProjectiles) {
                p.draw(c);
            }
        }

        private void updateProjectiles(double e) {
            for (Projectile p : playerProjectiles) {
                p.update(e);
            }
        }

        private void destroyPlayerShip() {
            mShip.destroy();
            gameOver = true;
            synchronized (mSurfaceHolder) {
                mMode = STATE_LOSE;
                String strStatus = "You died! You scored\n" + playerScore + " points. \nTap to play again!";
                Message msg = mHandler.obtainMessage();
                mBundle.putString("status_text", strStatus);
                mBundle.putInt("status_viz", View.VISIBLE);
                msg.setData(mBundle);
                mHandler.sendMessage(msg);
            }
        }


        private void checkProjectileBoundaries() {
            int i = 0;
            while (i < playerProjectiles.size()) {
                Projectile p = playerProjectiles.get(i);
                if (outOfBounds(p.getX(), p.getY(), p.getWidth(), p.getHeight())) {
                    playerProjectiles.remove(i);
                } else {
                    i++;
                }
            }
        }

        private boolean outOfBounds(float x, float y, float width, float height) {
            if (y < 0 || y + height > screenBottom || x < 0 || x + width > screenWidth) {
                return true;
            } else {
                return false;
            }
        }

        private void checkObjectBoundaries() {
            checkShipBoundary(mShip);
            checkProjectileBoundaries();
            checkEnemyShipBoundaries();
            checkPowerUpBoundaries();
        }

        private void checkPowerUpBoundaries(){
            int i = 0;
            while (i < powerUpList.size()) {
                PowerUp pUp = powerUpList.get(i);
                if (outOfBounds(pUp.getX(), pUp.getY(), pUp.getWidth(), pUp.getHeight())) {
                    powerUpList.remove(i);
                } else {
                    i++;
                }
            }
        }

        private void checkShipBoundary(Ship ship) {
            if ((ship.getY() < 0) || (ship.getY() + ship.getHeight() > screenBottom)) {
                if (ship.getY() + ship.getHeight() > screenBottom) {
                    ship.setPosition(ship.getX(), (screenBottom) - ship.getHeight());
                } else {
                    ship.setPosition(ship.getX(), 0);
                }
            }

            if ((ship.getX() < 0) || (ship.getX() + ship.getWidth() > screenWidth)) {
                if (ship.getX() + ship.getWidth() > screenWidth) {
                    ship.setPosition(screenWidth - ship.getWidth(), ship.getY());
                } else {
                    ship.setPosition(0, ship.getY());
                }
            }
        }

        private void updateScore(double d) {
            synchronized (mSurfaceHolder) {
                PowerUp.currentPowerUpProgress += d;
                playerScore += (d);
                score = "" + playerScore;
                Message msg = mHandler.obtainMessage();
                mBundle.putString("score_text", score);
                msg.setData(mBundle);
                mHandler.sendMessage(msg);


            }
        }


        public void resetGame() {
            System.out.println("RESET GAME");
            updateScore(0);
            checkHighScore(playerScore);
            gameOver = false;

            reset();
            EnemyShip.enemyCooldown = Double.parseDouble(sharedPrefs.getString(SettingsActivity.PreferenceKeys.PREF_ENEMY_SPAWN_RATE, "1"));
            PowerUp.powerUpCooldown = Double.parseDouble(sharedPrefs.getString(SettingsActivity.PreferenceKeys.PREF_POWERUP_SPAWN_RATE, "10"));
            if (mMode != STATE_LOSE) {
                setState(STATE_READY);
            }

            try {
                c = mSurfaceHolder.lockCanvas();
                synchronized (mSurfaceHolder) {
                    synchronized (mRunLock) {
                        if (mRun) {
                            clear(c);
                            doDraw(c);
                        }
                    }
                }
            } finally {
                if (c != null) {
                    mSurfaceHolder.unlockCanvasAndPost(c);
                }
            }
        }





        public ArrayList<Integer> getHighScoreValues(){ //greatest to least
            ArrayList<Integer> highScoreArray = new ArrayList<Integer>();
            for(int i = 0; i < SettingsActivity.maxHighScore; i++){
                String highScoreKey = "DATA_HIGH_SCORE_"+i;
                String highScore = getString(sharedPrefs, highScoreKey);
                int scoreIndex = highScore.indexOf(": ");

                if(scoreIndex > 0){
                    String scoreString = highScore.substring(scoreIndex+2);
                    int scoreValue = Integer.parseInt(scoreString);
                    highScoreArray.add(scoreValue);
                }

            }
            return highScoreArray;
        }


        public void checkHighScore( int score){
            if (score > 0){
                int i = 0; boolean scored = false;
                ArrayList<Integer> highScoreArray = getHighScoreValues();
                while(i < SettingsActivity.maxHighScore && !scored){
                    if(i < highScoreArray.size()){
                        if(scored){
                            String previousHighScore = getHighScore(i - 1);
                            changeHighScore(previousHighScore, i);
                        }
                        if(score > highScoreArray.get(i)){
                            startHighScoreThread(score, i);
                            scored = true;
                        }
                    }else{
                        startHighScoreThread(score, i);
                        scored = true;
                    }

                    i++;
                }
            }

        }

        public void changeHighScore(String value, int index){
            String highScoreKey = "DATA_HIGH_SCORE_"+index;
            System.out.println("HSK" + highScoreKey);
            storeString(sharedPrefs, highScoreKey, value);
        }

        public String getHighScore(int index){
            String highScoreKey = "DATA_HIGH_SCORE_"+index;
            return getString(sharedPrefs, highScoreKey);
        }

        public void storeString(SharedPreferences prefs, String key, String data){
            SharedPreferences.Editor prefEditor = prefs.edit();
            prefEditor.putString(key, data);
            prefEditor.commit();
        }


        public  String getString(SharedPreferences prefs, String key){

            return prefs.getString(key, "no value");
        }

        public  void storeInt(SharedPreferences prefs, String key, int data){
            SharedPreferences.Editor prefEditor = prefs.edit();
            prefEditor.putInt(key, data);
            prefEditor.commit();
        }

        public  int getInt(SharedPreferences prefs,  String key){
            return prefs.getInt(key, -1);
        }





        public void checkSettings(SharedPreferences prefs) {
            if (piecesCreated) {
                soundEnabled = prefs.getBoolean(SettingsActivity.PreferenceKeys.PREF_SOUND_ENABLED, true);

                numPlayers = Integer.parseInt(prefs.getString(SettingsActivity.PreferenceKeys.PREF_NUM_PLAYERS, "1"));

                EnemyShip.enemyCooldown = Double.parseDouble(prefs.getString(SettingsActivity.PreferenceKeys.PREF_ENEMY_SPAWN_RATE, "1"));
                PowerUp.powerUpCooldown = Double.parseDouble(sharedPrefs.getString(SettingsActivity.PreferenceKeys.PREF_POWERUP_SPAWN_RATE, "10"));
                mShip.setWeaponCooldown(Double.parseDouble(prefs.getString(SettingsActivity.PreferenceKeys.PREF_FIRE_RATE, ".25")));
            }
        }

    }


    /**
     * Handle to the application context, used to e.g. fetch Drawables.
     */
    private Context mContext;

    /**
     * Pointer to the text view to display "Paused.." etc.
     */
    private TextView mStatusText;
    private TextView mScoreText;
    private TextView mHighScoreText;
    /**
     * The thread that actually draws the animation
     */
    private GameThread thread;
    private Context c;
    private Handler uiHandler;


    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        uiHandler = new Handler();

        c = context;
        // register our interest in hearing about changes to our surface
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);

        // create thread only; it's started in surfaceCreated()
        thread = new GameThread(holder, context, new Handler() {
            @Override
            public void handleMessage(Message m) {
                mStatusText.setVisibility(m.getData().getInt("status_viz"));
                mStatusText.setText(m.getData().getString("status_text"));

                mScoreText.setVisibility(VISIBLE);
                //System.out.println("ST:" + m.getData().getString("score_text"));
                mScoreText.setText(m.getData().getString("score_text"));
            }
        });

        /*
        uiThread = new UIThread(holder, context, new Handler(){
            @Override
            public void handleMessage(Message m){

            }
        });


        Handler UIHandler;
        UIHandler.post(new Runnable() {
            @Override
            public void run() {

            }
        });*/

        setFocusable(true); // make sure we get key events
    }


    public void startHighScoreThread( final int score, final int index){
        System.out.println("DIALOG OPEN:" + dialogOpen);
        // Do something that takes a while
        if(!dialogOpen){
            dialogOpen = true;
            System.out.println("START HIGH SCORE THREAD");
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    System.out.println("Highscore running");
                    uiHandler.post(new Runnable() { // This thread runs in the UI
                        @Override
                        public void run() {

                            AlertDialog.Builder alert = new AlertDialog.Builder(c);

                            alert.setTitle("High Score!");
                            alert.setMessage("Enter your name.");

                            final EditText input = new EditText(c);
                            input.setHint("Name");
                            alert.setView(input);

                            alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String name = input.getText().toString();
                                    if (!name.equals("")) {
                                        String message = name + ": " + score;
                                        getThread().changeHighScore(message, index);
                                        System.out.println("HS:"+message);
                                        dialogOpen = false;
                                    } else {
                                        displayToast("Please enter a name");
                                        dialogOpen = false;
                                        startHighScoreThread(score, index);
                                    }
                                }
                            });
                            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialogOpen = false;
                                }
                            });
                            alert.show();
                        }
                    });

                }
            };
            new Thread(runnable).start();
        }

    }

    protected void displayToast(String message){
        CharSequence text = message;
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(c, text, duration);
        toast.show();
    }


    /**
     * Fetches the animation thread corresponding to this LunarView.
     *
     * @return the animation thread
     */
    public GameThread getThread() {
        return thread;
    }

    /**
     * Standard window-focus override. Notice focus lost so we can pause on
     * focus lost. e.g. user switches to take a call.
     */
    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        if (!hasWindowFocus) thread.pause();

    }

    /**
     * Installs a pointer to the text view used for messages.
     */
    public void setTextViews(TextView statusView, TextView scoreView) {
        mStatusText = statusView;
        mScoreText = scoreView;
    }

    /* Callback invoked when the surface dimensions change. */
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        if (width != screenWidth || height != screenHeight) {
            System.out.println("POSSIBLE ERROR");
        }
    }

    /*
     * Callback invoked when the Surface has been created and is ready to be
     * used.
     */
    public void surfaceCreated(SurfaceHolder holder) {
        // start the thread here so that we don't busy-wait in run()
        // waiting for the surface to be created
        if (screenWidth == 0) {
            myCanvas_w = getWidth();
            myCanvas_h = getHeight();
        } else {
            myCanvas_w = (int) screenWidth;
            myCanvas_h = (int) screenHeight;
        }

        myCanvasBitmap = Bitmap.createBitmap(myCanvas_w, myCanvas_h, Bitmap.Config.ARGB_8888);
        myCanvas = new Canvas();
        myCanvas.setBitmap(myCanvasBitmap);

        identityMatrix = new Matrix();
        thread.setRunning(true);

        if (thread.getState() == Thread.State.NEW) {
            thread.start();
        }
        if (piecesCreated) {
            Canvas canvas = holder.lockCanvas();
            myCanvas.drawARGB(255, 0, 0, 0);
            mShip.draw(myCanvas);

            canvas.drawBitmap(myCanvasBitmap, identityMatrix, null);

            holder.unlockCanvasAndPost(canvas);
        }
    }

    /*
     * Callback invoked when the Surface has been destroyed and must no longer
     * be touched. WARNING: after this method returns, the Surface/Canvas must
     * never be touched again!
     */
    public void surfaceDestroyed(SurfaceHolder holder) {
        // we have to tell thread to shut down & wait for it to finish, or else
        // it might touch the Surface after we return and explode
        //   boolean retry = true;
        System.out.println("SURFACE DESTROYED");
        thread.setRunning(false);
      /*  while (retry) {
            try {
            	System.out.println("JOINED");
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }*/
    }


}

