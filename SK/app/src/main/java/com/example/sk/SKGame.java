package com.example.sk;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

public class SKGame extends Thread{
    private boolean running;
    private SKGrid gameSurface;
    private SurfaceHolder surfaceHolder;
    private GameActivity game;

    public SKGame(SKGrid gameSurface, SurfaceHolder surfaceHolder){
        this.gameSurface= gameSurface;
        this.surfaceHolder= surfaceHolder;
        game = (GameActivity) gameSurface.getContext();
    }

    @Override
    public void run(){
        long startTime = System.nanoTime();
        while(running)  {
            Canvas canvas= null;
            try {
                canvas = this.surfaceHolder.lockCanvas();// locking the canvas
                synchronized (canvas)  {
                    this.gameSurface.update();
                    this.gameSurface.draw(canvas);
                }
            }catch(Exception e)  {

            } finally {
                if(canvas!= null)  {
                    this.surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
            long now = System.nanoTime() ;
            long waitTime = (now - startTime)/1000000;
            if(waitTime < 10)  {
                waitTime= 10; // Millisecond.
            }
            try {
                this.sleep(waitTime);
            } catch(InterruptedException e)  {

            }
            startTime = System.nanoTime();
            System.out.print(".");
        }

        //end of the game loop :
        try {
            sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        game.finish();
        return;
    }

    public void setRunning(boolean running)  {
        this.running=running;
    }
}
