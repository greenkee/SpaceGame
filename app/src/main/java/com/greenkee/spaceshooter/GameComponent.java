package com.greenkee.spaceshooter;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

/**
 * Created by student on 7/17/2014.
 */
public class GameComponent {

    protected final float BASE_SPEED = (float)(GameView.screenWidth*.8);

    protected final float MAX_SPEED = BASE_SPEED;

    protected float xValue; //left
    protected float yValue; //top
    protected float width, height;
    protected Context c;

    protected Drawable sprite;
    protected float speedX;
    protected float speedY;
    protected String componentName;

    public GameComponent( Context c){

        this.c =  c;

    }


    public void setStartLoc(float x, float y){
        xValue = x;
        yValue = y;
    }
    public void stop(){
        speedX = 0;
        speedY = 0;
    }


    public void setSpriteAlpha(int a){
        sprite.setAlpha(a);
    }

    public void update(double elapsed){
        xValue += (elapsed* speedX);
        yValue += (elapsed* speedY);

        sprite.setBounds((int)xValue, (int)yValue, (int)(xValue+width), (int)(yValue+ height));
    }

    public void draw(Canvas canvas){
        sprite.draw(canvas);
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

    public void setPosition(float x, float y){
        xValue = x;
        yValue = y;
    }

    public boolean inContact(float x, float y){
        boolean b = false;
        if(x >= xValue && x<= xValue + width){
            if(y >= yValue && y <= yValue + height){
                b = true;
            }
        }
        return b;
    }

    public String getComponentName(){
        return componentName;
    }
}
