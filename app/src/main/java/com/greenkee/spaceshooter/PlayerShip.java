package com.greenkee.spaceshooter;

import android.content.Context;
import android.graphics.Paint;

/**
 * Created by student on 7/15/2014.t
 */
public class PlayerShip extends Ship {
    protected float deadBand;
    protected double weaponCooldown = .25;
    protected double currentRecharge = 0;
    public static double minWeaponCooldown = .01;
    public PlayerShip( Context c){
        super(c);


        sprite = c.getResources().getDrawable(R.drawable.player_ship);
        width = sprite.getIntrinsicWidth();
        height = sprite.getIntrinsicHeight();

        invunlerabilityCount = invulnerabilityTime+1;

        deadBand = width / 4;

    }

    public void setWeaponCooldown(double d){
        weaponCooldown = d;
    }

    protected void shoot(){

        Projectile p =  new Projectile(xValue + width/2 - Projectile.getLaserWidth()/2, yValue, c, 0, -BASE_SPEED*2);
        GameView.playerProjectiles.add(p);
    }

    @Override
    public void setStartLoc(float x, float y){
        startX = x;
        startY = y;
        super.setStartLoc(x,y);
    }

    public void reset(){
        xValue = startX;
        yValue = startY;
        speedX = 0;
        speedY = 0;
    }


    public void update(double elapsed){
        super.update(elapsed);
        if(invunlerabilityCount > invulnerabilityTime){
            vulnerable = true;
        }else{
            invunlerabilityCount+= elapsed;
        }
        currentRecharge += elapsed;

        if(currentRecharge > weaponCooldown){
            shoot();
            currentRecharge = 0;
        }
    }

    @Override
    public void destroy(){
        super.destroy();
        if (GameView.soundEnabled){
            GameActivity.audio.play(GameActivity.playerDeathSound, 1, 1, 1, 0, 1);
        }
    }

    public void move(int x, int y){
        if( (Math.abs((xValue+ width/2) - x) )> (deadBand)){
            if(x - width/2 > xValue){
                speedX = 1;
            }else{
                speedX = -1;
            }
        }else{
            speedX = 0;
        }

        if((Math.abs((yValue+height/2) - y) ) > (deadBand)){
            if(y - height/2 > yValue){
                speedY = 1;
            }else{
                speedY = -1;
            }
        }else{
            speedY = 0;
        }

        normalizeSpeed();
        speedX *= BASE_SPEED;
        speedY *= BASE_SPEED;
        //System.out.println("MOVE: " + speedX + "; " + speedY);
    }

    /*
    @Override
    public boolean inContact(float x, float y){
        boolean b = false;
        if(super.inContact(x,y)) {
            if (y >= yValue + height / 2 && y <= yValue + height) {
                if ((x >= xValue && x <= xValue + width)) {
                    b = true;
                }
            } else if (y >= yValue && y < yValue + height / 2) {
                if (x >= xValue + width / 3 && x <= xValue + (2 * width / 3)) {
                    b = true;
                } else {
                    float xSize = Math.abs(xValue - x);
                    float ySize = Math.abs(yValue - y);
                }
            }
        }


        return b;
    }
    */




}
