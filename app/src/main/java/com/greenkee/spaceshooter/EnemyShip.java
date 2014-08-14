package com.greenkee.spaceshooter;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;

import java.util.HashMap;

/**
 * Created by student on 7/15/2014.
 */
public class EnemyShip extends Ship {
    public static double enemyCooldown = 1;
    public static double currentEnemyCounter = 1;
    public static double minCooldown = .01;
    protected float pointValue;
    protected float damage, health;


    public EnemyShip(Context c, HashMap<String, String> hMap){
        super(c);
        componentName = hMap.get("enemyName");
        sprite = c.getResources().getDrawable(Integer.parseInt(hMap.get("enemyImage")));
        width = sprite.getIntrinsicWidth();
        height = sprite.getIntrinsicHeight();
        setSpriteAlpha(100);

        int random = (int)(Math.random()*GameView.screenWidth);
        setStartLoc((float)random, 0);

        speedX = BASE_SPEED * ( ((float)Integer.parseInt(hMap.get("speedX"))) /100);
        speedY = BASE_SPEED * ( ((float)Integer.parseInt(hMap.get("speedY"))) /100);
        damage = Integer.parseInt(hMap.get("damage"));
        health = Integer.parseInt(hMap.get("health"));
        pointValue = Integer.parseInt(hMap.get("pointValue"));
        System.out.println("speedX: " +speedX + " speedY:"+(speedY));

    }




    @Override
    public void destroy(){
        super.destroy();
        if (GameView.soundEnabled){
            GameActivity.audio.play(GameActivity.destroySound, 1, 1, 1, 0, 1);
        }
    }


    public void update(double elapsed){
        super.update(elapsed);
        if(invunlerabilityCount > invulnerabilityTime){
            vulnerable = true;
        }else{
            invunlerabilityCount+= elapsed;
        }

        if (xValue <= 0 || xValue >= GameView.screenWidth - width) {
            speedX *= -1;
        }

        /*
        currentRecharge += elapsed;

        if(currentRecharge > weaponCooldown){
            shoot();
            currentRecharge = 0;
        }*/
    }

    public float getPointValue(){
        return pointValue;
    }
}
