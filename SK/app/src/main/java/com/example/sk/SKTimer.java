package com.example.sk;

import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class SKTimer extends TimerTask{
    private static final String TAG = "SKTimer";
    private Timer timer;
    private Thread thread;
    private int periodTimer;
    private int t0, t1;
    private int villainWaitAmount;
    private boolean villainWait;
    private boolean villain_is_shooting;

    //pour acceder à la methode setShooting
    private SKGrid gameSurface;

    public SKTimer(SKGrid gS, String threadName, int _periodTimer){
        super();
        periodTimer = _periodTimer;
        t0 = t1 = 0;
        gameSurface = gS;
        villainWait=false;
        villain_is_shooting=false;
        villainWaitAmount = 0;
        thread = new Thread(threadName);//on creer d'abord le thread
        this.timer = new Timer(threadName);//puis on creer le timer
        this.timer.schedule(this, 500, periodTimer);//task s'execute toutes les 1000 secondes et commence au bout de 500 millisecondes
    }

    @Override
    public void run(){
        try {
            t0++;
            if(t0>1){//on commence a tirer apres 2 secondes d'attente
                if(villainWait){
                    //we wait for 5 secondes during which we shoot
                    t1++;
                    if(t1<villainWaitAmount){//t1 creates application crash
                        if(villain_is_shooting){
                            gameSurface.setShooting(true);//on compte jusqua 2 secondes
                        }
                        else{
                            gameSurface.setShooting(false);//on compte jusqua 2 secondes
                        }
                    }
                    else{//on peut repartir
                        t1=0;
                        villainWait=false;
                        gameSurface.setShooting(false);
                    }
                }
                else{
                    gameSurface.setShooting(false);//on compte jusqua 2 secondes
                }
            }
        } catch (Exception e) {
            Log.d(TAG,"Erreur survenue pour la classe SKTimer ");
        }
    }

    public void setWaitState(boolean val, boolean isShooting, int nb){
        villainWait=val; villainWaitAmount = nb;
        villain_is_shooting = isShooting;
    }//nb = number of second we wait inside the run() method

    public boolean getWaitState(){//tells the villain if he can move again
        return villainWait;
    }

    public void finalize()//Destructor
    {
        this.timer.cancel();
    }

}
