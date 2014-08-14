package com.greenkee.spaceshooter;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;

/**
 * Created by student on 7/14/2014.
 */
public abstract class Ship extends GameComponent{

    protected float startX, startY;
    protected boolean vulnerable;
    protected double invulnerabilityTime = .5;
    protected double invunlerabilityCount = 0;


    public Ship( Context c){
        super(c);
        vulnerable = false;


        //width = GameView.screenWidth / 4;
        //height = width;


    }



    public void destroy(){
        speedX = 0;
        speedY = 0;
        vulnerable = false;

    }

    public boolean getVulnerable(){
        return vulnerable;
    }





    public void update(double elapsed){
        super.update(elapsed);

       // System.out.println("El: "+ elapsed + " SX:"+ speedX + " SY:"+ speedY);
    }

    public void draw(Canvas canvas){
        if(vulnerable){
            setSpriteAlpha(255);
        }else{
            setSpriteAlpha(100);
        }

        super.draw(canvas);
        //System.out.println("X: "+ xValue + " Y:"+ yValue);

        //canvas.drawRect(xValue, yValue, xValue + width, yValue + height, shipPaint);
    }



    protected void normalizeSpeed() {
        if( (Math.abs(speedX) + Math.abs(speedY)) > MAX_SPEED){
            /*int angle = 0;
            if(speedX > 0 && speedY > 0){
                angle = 45;
            } else if(speedX < 0 && speedY > 0){
                angle = 135;
            } else if (speedX < 0 && speedY < 0){
                angle = 225;
            }else if (speedX > 0 && speedY < 0){
                angle = 315;
            }*/
            double vectorLength = Math.sqrt(Math.pow(speedX, 2) + Math.pow(speedY, 2));
            speedX /=vectorLength;
            speedY /= vectorLength;

        }
    }
}
