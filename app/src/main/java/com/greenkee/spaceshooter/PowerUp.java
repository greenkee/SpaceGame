package com.greenkee.spaceshooter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;

/**
 * Created by student on 7/17/2014.
 */
public class PowerUp extends GameComponent {
    public static double powerUpCooldown = 10;
    public static double currentPowerUpCounter = 0;
    public static double minPowerUpCooldown = .01;
    public static int pointsForPowerUp = 5000;
    public static int currentPowerUpProgress = 0;

    protected float speed = BASE_SPEED/2;

    private boolean collectable;

    public PowerUp(Context c, int drawableResourceID){
        super(c);
        sprite = c.getResources().getDrawable(drawableResourceID);

        collectable = true;
        int random = (int)(Math.random()*GameView.screenWidth);
        setStartLoc((float)random, 0);
        System.out.println("RAND: " + random);

        width = sprite.getIntrinsicWidth();
        height = sprite.getIntrinsicHeight();

        speedY = speed;

        System.out.println("X: " + xValue + " Y: " + yValue + " W: " + width + " H: " + height);

    }

    public void destroy(){
        collectable = false;
    }

    public boolean getCollectable(){
        return collectable;
    }


}
