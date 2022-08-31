package com.example.sk;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.Log;

import java.util.Random;

public class SKVillain extends SKPerso{

    private static final String TAG = "SKVillain";
    private int cpt;//when it reaches 10 we change the image
    private int colY, colX;
    private int flapSpeed;
    protected enum CHARACTER_STATE {
        RIGHT,
        LEFT,
        IDLE;
    }
    private CHARACTER_STATE m_state;
    private SKTimer cannon_countdown;//to shoot periodically the cannons

    public SKVillain(SKGrid gS, Bitmap image, int r, int c){
        super(gS,image,r,c);

        scaleLargeImg(gameSurface.getSZGrid()*6, gameSurface.getSZGrid()*6);
        colY=1; colX=0;
        perso_img = getSubImageAt(colX,colY);//idle

        pos_x = pos_y = 0;

        flapSpeed = 10;
        cpt = 0;
        m_state = CHARACTER_STATE.RIGHT;
        cannon_countdown = new SKTimer(gS, "cannonThread", 2000);//we shoot every 2 seconds
    }

    public void changeImg(){
        if(cpt==flapSpeed){
           switch(m_state){
               case RIGHT:
                   colX=1;
                   gameSurface.setShooting(false);
                   if(pos_x<6) pos_x++;
                   else {m_state=CHARACTER_STATE.IDLE; cannon_countdown.setWaitState(true, true,5);}
                   break;
               case LEFT:
                   gameSurface.setShooting(false);
                   colX=0;
                   if(pos_x>1) pos_x--;
                   else { m_state=CHARACTER_STATE.IDLE; cannon_countdown.setWaitState(true, false,2);}
                   break;
               case IDLE:
                   //on reste 5 secondes à cette position
                   if(!cannon_countdown.getWaitState()){
                        if(pos_x==6){
                            m_state=CHARACTER_STATE.LEFT;
                        }
                        else if(pos_x==1){
                            m_state=CHARACTER_STATE.RIGHT;
                        }
                   }
                   break;
           }
           colY++; if(colY>1)   colY=0;
           perso_img = getSubImageAt(colX,colY);//idle
           cpt=0;
        }
        else{
            cpt++;
        }
    }

    public void update(){
        changeImg();
    }

    public Point getCanons(int canonsSZ, int NB_CASES_Y, Point perso_pos){//renvoit les deux index des deux canons qui doivent tirer
        //index est compris entre 0 et canonsSZ passé en argument
        Point p = new Point(0,0);

        //on s'ajuste en fonction de la position du perso
        p.x=(NB_CASES_Y-perso_pos.y)/2;// au debut vaut forcement 0
        p.x-=1;
        if(p.x<0){
            p.x=0;
        }

        //Random r = new Random();
        //p.x = r.nextInt((max - min) + 1) + min;
        //p.y = r.nextInt((max - min) + 1) + min;

        p.y = p.x+1;
        if(p.y>=canonsSZ){
            p.y = canonsSZ-1;
        }
        if(p.x>=canonsSZ){
            p.x = canonsSZ-2;
        }

        //Log.d(TAG,"p.x = "+p.x);
        //Log.d(TAG,"p.y = "+p.y);

        return p;
    }//un appel de Random provoque : img_bullet=null

}
