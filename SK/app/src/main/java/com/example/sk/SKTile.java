package com.example.sk;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.util.Log;

public class SKTile {
    private static final String TAG = "SKTile";

    /*for the tile*/
    protected int pos_x;
    protected int pos_y;
    protected int w;
    protected int h;
    protected Bitmap img;

    protected SKGrid gameSurface;

    public SKTile(SKGrid gS, Bitmap image){
        //EXPLICATION : getWidth ne renvoit pas exactement la largeur de l'image, pour l'obtenir on doit diviser par le ration 2.625.
        w = gS.getSZGrid();//(int)(image.getWidth()/2.625) avant
        h = gS.getSZGrid();
        img = Bitmap.createScaledBitmap(image,w,h,true);
        pos_x = pos_y = 0;
        gameSurface=gS;
    }

    public SKTile(SKGrid gS, Bitmap image, int width, int height){
        //EXPLICATION : getWidth ne renvoit pas exactement la largeur de l'image, pour l'obtenir on doit diviser par le ration 2.625.
        w = width;
        h = height;
        img = Bitmap.createScaledBitmap(image,w,h,true);
        pos_x = pos_y = 0;
        gameSurface=gS;
    }

    public Point getPos(){//returns the previous position of character
        return (new Point(pos_x,pos_y));
    }

    public void setPos(int x, int y){
        pos_x = x; pos_y = y;
    }

    public void draw(Canvas canvas, int sz)  {
        //the new coordinates must correspond to a square inside the grid
        //x must be an int between 0*sz and NB_CASES_X*sz and same for y
        if(img==null) {Log.d(TAG,"Erreur, image == null !"); return;}//protection
        canvas.drawBitmap(img, pos_x*sz, pos_y*sz, null);
    }

}
