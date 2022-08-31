package com.example.sk;

import android.graphics.Bitmap;

public class SKPrincess extends SKPerso{
    public SKPrincess(SKGrid gS, Bitmap image, int r, int c){
        super(gS,image,r,c);
        setPos(10,4);
        scaleLargeImg(gameSurface.getSZGrid()*8, gameSurface.getSZGrid()*4);
        setImgPerso(getSubImageAt(0,0));
    }
}
