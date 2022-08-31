package com.example.sk;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.util.Log;

public class SKBullet extends SKTile{
    private static final String TAG = "SKBullet";

    private Bitmap img_bullet;
    private int bullet_w, bullet_h;
    private int bullet_x, bullet_y;//position of bullet on screen
    private int velocity_bullet;
    private int cpt;//when it reached velocity we change the position of the bullet (so that the bullet go slower)

    public SKBullet(SKGrid gS, Bitmap image){
        super(gS,image);
        velocity_bullet = 10;
        bullet_x = bullet_y = 0;
        cpt = 0;
    }

    public void initBullet(Bitmap image){//called only when we want to create a bullet
        bullet_w = (int)(image.getWidth()/2.625);
        bullet_h = (int)(image.getHeight()/2.625);
        bullet_x = pos_x-1;
        bullet_y = pos_y;
        img_bullet = Bitmap.createScaledBitmap(image, bullet_w, bullet_h,true);
        if(img_bullet==null){ Log.d(TAG,"Image null");}
    }

    public void drawBullet(Canvas canvas, int sz)  {
        if(img_bullet==null){ return;}
        if(bullet_x>-1) canvas.drawBitmap(img_bullet, bullet_x*sz, bullet_y*sz, null);//le if => to remove undesirable effect in animation
    }

    public Point getBulletPos(){
        return (new Point(bullet_x,bullet_y));
    }

    public void decrBulletX(){
        if(bullet_x<0) {
            bullet_x=-1;
            gameSurface.setShooting(false);//normally this should prevent from shooting at the next iteration of draw in SKGrid but actually it's too slow so we use a small if statement inside the draw method oft his class
        }
        else{
            if(cpt==velocity_bullet){
                bullet_x--;
                cpt=0;
            }
            else{
                cpt++;
            }
        }
    }
}
