package com.greenkee.spaceshooter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;

/**
 * Created by student on 7/15/2014.
 */
public class Projectile {
    private float speedX, speedY, xValue, yValue;
    private float width, height;
    private Drawable sprite;

    private Paint projectilePaint;

    protected static float laserWidth;



    public Projectile(float x, float y, Context c, float dX, float dY){
        xValue = x;//left
        yValue = y;//bot
        sprite = c.getResources().getDrawable(R.drawable.laser_green);

        speedX = dX;
        speedY = dY;

        width = sprite.getIntrinsicWidth();
        height = sprite.getIntrinsicHeight();

        projectilePaint = new Paint();
        projectilePaint.setARGB(255, 0, 255, 0);

    }
    public void update(double elapsed){

        xValue += (elapsed* speedX);
        yValue += (elapsed* speedY);

        sprite.setBounds((int)(xValue), (int)(yValue), (int)(xValue+width), (int)(yValue+ height));


    }

    public void draw(Canvas canvas){
        sprite.draw(canvas);
        //canvas.drawRect(xValue, yValue, xValue + width, yValue + height, projectilePaint);
    }

    public void destroy(){
        speedX = 0;
        speedY = 0;
    }

    public float getX(){
        return xValue;
    }

    public float getY(){
        return yValue;
    }

    public float getWidth(){
        return width;
    }

    public float getHeight(){
        return height;
    }

    public static float getLaserWidth(){
        return laserWidth;
    }

    public static void setLaserWidth(float f){
        laserWidth = f;
    }

}
