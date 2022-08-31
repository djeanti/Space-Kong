package com.example.sk;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.util.Log;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class SKPerso {
    protected static final String TAG = "SKPerso";

    /*for the large image :*/
    protected Bitmap large_img;/*contains all the images for the character*/
    protected final int rowCount;
    protected final int colCount;

    /*for the character*/
    protected int pos_x;
    protected int pos_y;
    protected int perso_width;
    protected int perso_height;
    protected Bitmap perso_img;//image courante a afficher (au debut IDLE)

    protected SKGrid gameSurface;

    public SKPerso(SKGrid gS, Bitmap image, int r, int c) {
        colCount = c;
        rowCount = r;
        large_img = image;
        scaleLargeImg(gS.getSZGrid()*c,gS.getSZGrid()*r);

        pos_x = pos_y = 0;
        gameSurface = gS;//aucune utilité pour le moment

        perso_img = getSubImageAt(0,0);
    }

    public void scaleLargeImg(int x, int y){
        large_img = Bitmap.createScaledBitmap(large_img, x, y,true);
        perso_width = large_img.getWidth() / colCount;
        perso_height = large_img.getHeight() / rowCount;
    }

    public Bitmap getSubImageAt(int row, int col){/*gives the current image to show of the character !*/
        Bitmap b = Bitmap.createBitmap(large_img, col* perso_width, row* perso_height , perso_width, perso_height);
        return b;
    }

    public void setImgPerso(Bitmap b){//pour modifier perso_img
        perso_img=b;
    }

    public void setPos(int x, int y){//returns the previous position of character
        pos_x = x; pos_y = y;
    }

    public Point getPos(){//returns the previous position of character
        return (new Point(pos_x,pos_y));
    }

    public Point getPersoSZ(){
        return (new Point(perso_width,perso_height));
    }


    public void draw(Canvas canvas, int sz)  {//tant que le persoonnage n'a pas atteint sa destination (new_pos) on bloque le programme.
        //the new coordinates must correspond to a square inside the grid
        //x must be an int between 0*sz and NB_CASES_X*sz and same for y
        if(perso_img==null) {Log.d(TAG,"Image null !"); return;}
        canvas.drawBitmap(perso_img, pos_x*sz, pos_y*sz, null);
    }

}