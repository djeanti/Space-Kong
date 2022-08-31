package com.example.sk;

import android.graphics.Bitmap;
import android.util.Log;

public class SKExplo{

    private final String TAG = "SKExplo";

    /*for the large image :*/
    private Bitmap large_img;/*contains all the images for the character*/
    private final int rowCount;
    private final int colCount;

    /*current explosion frame :*/
    private int w;
    private int h;
    private Bitmap img;//image courante a afficher (au debut IDLE)

    private SKGrid gameSurface;

    //to change the image :
    private int l,c;//parcourir l'image large
    private int cpt;//for velocity
    private int explo_speed;

    private boolean animation_ended;

    public SKExplo(SKGrid gS, Bitmap image, int r, int c){
        rowCount = r; colCount = c;
        gameSurface = gS;
        large_img = image;
        w = large_img.getWidth() / colCount;
        h = large_img.getHeight() / rowCount;
        img = getSubImageAt(0,0);//idle

        //others :
        l=0; c=0;
        cpt = 0;
        explo_speed = 10;
        animation_ended=false;
    }

    public void reScale(int sz){
        w = sz;
        h = sz;
        large_img = Bitmap.createScaledBitmap(large_img, w*rowCount, h*colCount,true);
        img = getSubImageAt(l,c);//idle
    }

    public boolean getAnimState(){
        return animation_ended;
    }

    public Bitmap getSubImageAt(int row, int col){/*gives the current image to show of the character !*/
        Bitmap b = Bitmap.createBitmap(large_img, col* w, row* h , w, h);
        return b;
    }

    public Bitmap getNxtImg(){
        //changing the image here
        //on recupere les images de l'explosion dans l'ordre :
        if(cpt==explo_speed){
            if(c<colCount-1){
                c++;
            }
            if(l<rowCount-1){
                l++;
                c=0;
            }
            cpt=0;
        }
        else{
            cpt++;
        }

        if(c==colCount-1 && l==rowCount-1){
            animation_ended=true;
        }

        img = getSubImageAt(l,c);
        return img;
    }

}
