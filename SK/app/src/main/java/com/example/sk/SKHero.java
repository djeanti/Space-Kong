package com.example.sk;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.util.Log;

public class SKHero extends SKPerso{
    private static final String TAG = "SKHero";

    /*hero only :*/
    protected int m_health;
    protected int velocity;//speed of the character, the bigger the number, the slower the character !
    protected enum CHARACTER_STATE {
        IDLE,//getSubImageAt(0,1);

        WALK_L,

        WALK_R,

        CLIMBUP,

        CLIMBDOWN;
    }
    protected int c_L, c_R, c_M, c_IDLE;//permette de savoir quelle image chargee pour chaque catégorie (WALK_L, ...)
    protected int maxWaitIDLE;//la valeur jusqu'a laquelle c_IDLE s'increment avant d'etre reset
    protected SKHero.CHARACTER_STATE m_state;//current state (au debut IDLE)
    protected int cpt;//when it reaches 10 we change the image

    //explosion bitmap :
    protected SKExplo explosions;

    public SKHero(SKGrid gS, Bitmap image, int r, int c){
        super(gS,image,r,c);
        perso_img = getSubImageAt(0,1);//idle
        //others variables :
        cpt = c_L = c_R = c_M = c_IDLE = 0;
        m_state = SKHero.CHARACTER_STATE.IDLE;
        velocity = 7;
        maxWaitIDLE = 25;

        //hero stats :
        m_health = 100;

        //explosion :
        Bitmap b = BitmapFactory.decodeResource(gameSurface.getResources(),R.drawable.explosions);
        explosions = new SKExplo(gS,b,5,5);
        explosions.reScale(gameSurface.getSZGrid());
    }

    //game over :
    public void getHit(){//one hit is game over for now
        if(m_health<0){
            return;
        }
        m_health -= 100;
    }

    public boolean getExploState(){
        return explosions.getAnimState();
    }

    public int getHP()  {return m_health;}

    public void boom_animation(){
        if(m_health<=0){
            idle();//the character can't move and can't update his position anymore;
            //animation :
            perso_img=explosions.getNxtImg();
        }
    }

    public void setSpeed(int speed){
        if(speed<0){
            speed = 0;
        }
        if(speed > 100){
            speed = 70;//trop lent sinon
        }
        velocity = speed;
    }

    //position :

    //les deux methodes suivantes, incrPos et decrPos ne concerne que l'axe X !
    public void incrPosX(){//incremente pos_x de sorte a nous laisser le temps de voir le perso bouger a l'ecran du tel
        if(cpt==velocity){
            cpt=0;
            pos_x++;
            if(pos_x>8) pos_x=8;
            changeCurrentImg();
        }
        else{
            cpt++;
        }
    }

    public void decrPosX(){
        if(cpt==velocity){
            cpt=0;
            pos_x--;
            if(pos_x<0) {pos_x = 0;}
            changeCurrentImg();
        }
        else{
            cpt++;
        }
    }


    //la methode suivante permet de decrementer y et de changer l'image (pour monter)
    public void decrPosY(){
        if(cpt==velocity){
            cpt=0;
            pos_y--;//on monte d'une case
            changeCurrentImg();
        }
        else{
            cpt++;
        }
    }

    public void incrPosY(){
        if(cpt==velocity){
            cpt=0;
            pos_y++;//on descend d'une case
            changeCurrentImg();
        }
        else{
            cpt++;
        }
    }

    public void idle(){//when we do nothing
        m_state = CHARACTER_STATE.IDLE;
        c_IDLE++;
        if(c_IDLE==maxWaitIDLE){//wait 100 iterations (100 is a good ratio, 1000 is too long and 10 is barely visible) before showing idle so that we can have a smoother animation...
            c_IDLE = 0;
            changeCurrentImg();
        }
    }

    public void update(Point new_pos){//get the next position of character
        if(m_health>0)
        {
            if(new_pos.x==pos_x){//axe X on est bon
                if(new_pos.y==pos_y){//position atteinte (ouf !)
                    idle();
                }
                else{//on veut savoir si on a cliqué sur une echelle ou sur une brique
                    if(pos_y>new_pos.y){//on veut monter
                        if( !gameSurface.isAnObstacle(pos_x,pos_y-1) ){//car (pos_x,pos_y-1) = nouvelle pos une fois qu'on a appelé decrposY donc on verifie que c'est pas un obstacke avant d'y aller
                            m_state = CHARACTER_STATE.CLIMBUP;
                            decrPosY();
                        }
                        else{
                            idle();
                        }
                    }
                    else{//pos_y<new_pos.y
                        if( !gameSurface.isAnObstacle(pos_x,pos_y+1) ){
                            m_state = CHARACTER_STATE.CLIMBDOWN;
                            incrPosY();
                        }
                        else{
                            idle();
                        }
                    }
                }
            }
            else{
                if(pos_x<new_pos.x){
                    if( !gameSurface.isAnObstacle(pos_x+1,pos_y) ){
                        m_state = CHARACTER_STATE.WALK_L;
                        incrPosX();
                    }
                    else{
                        idle();
                    }
                }
                else{//pos_x>new_pos.x
                    if( !gameSurface.isAnObstacle(pos_x-1,pos_y) ){
                        m_state = CHARACTER_STATE.WALK_R;
                        decrPosX();
                    }
                    else{
                        idle();
                    }
                }
            }
        }
    }

    public void changeCurrentImg(){
        switch(m_state){
            case WALK_L://col qui change a chaque fois
                perso_img = getSubImageAt(2,c_L);
                c_L++;
                if(c_L>2){
                    c_L = 0;
                }
                break;
            case WALK_R:
                perso_img = getSubImageAt(1,c_R);
                c_R++;
                if(c_R>2){
                    c_R = 0;
                }
                break;
            case CLIMBDOWN: case CLIMBUP:
                perso_img = getSubImageAt(3,c_M);
                c_M++;
                if(c_M>2){
                    c_M = 0;
                }
                break;
            case IDLE:
                perso_img = getSubImageAt(0,1);//idle
                break;
            default:
                break;
        }
    }

}
