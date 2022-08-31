package com.example.sk;

import static java.lang.Math.max;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class SKGrid extends SurfaceView implements SurfaceHolder.Callback {
    //la grille est composee de plusieurs carres
    private int sz; // largeur et hauteur d'un carre de la grille
    private Paint textPaint;
    private int textColor;
    private int NB_CASES_X;//nb de carre maximum sur l'axe horizontal de la grille
    private int NB_CASES_Y;//nb de carre maximum sur l'axe vertical de la grille

    private SKGame gameThread;
    private MediaPlayer mediaPlayer, boomMusic;//music

    //hero :
    private SKHero hero;
    private Point new_pos;//nouvelle position atteindre pour le perso (peut changer avant que le perso ait atteint sa destination)

    //villain
    private SKVillain alien;

    //princess
    private SKPrincess peach;

    //liste de tiles ://NB_CASES_Y-2*ladersSZ
    private final int canonsSZ = 9;//nb de canons qu'on affiche (9)
    private final int wallsSZ = 11;//nb de murs porteurs qu'on affiche (12)
    private final int bricksLineSZ = 10;//nb de lignes de briques qu'on affiche (11)
    private final int laddersSZ = 10;//nb de ladders qu'on affiche en tt (11)
    private final int voidBrickSZ = 2;//nb de ladders qu'on affiche en tt (2)

    private final List<SKBullet> canons = new ArrayList<SKBullet>();
    private final List<SKTile> bricks = new ArrayList<SKTile>();
    private final List<SKTile> walls = new ArrayList<SKTile>();//walls supporting the canons
    private final List<SKTile> ladders = new ArrayList<SKTile>();//walls supporting the canons
    private final List<SKTile> voidBricks = new ArrayList<SKTile>();//2 briques suspendues

    //pour debuggage sur la console
    private static final String TAG = "SKGrid";

    //pour l'interaction avec SKTimer de SKVillain
    private boolean cannonShooting;
    private Point canons_idx; // index of the two canons currently shooting

    //game over :
    private Bitmap game_over;
    private boolean player_lost;

    //player win:
    private Bitmap game_won;
    private boolean player_win;

    protected int pos_y_winning;

    public SKGrid(Context context){//constructor
        super(context);

        // Make Game Surface focusable so it can handle events. .
        this.setFocusable(true);

        // Sét callback.
        this.getHolder().addCallback(this);
    }

/*methods :*/

    // Implements method of SurfaceHolder.Callback
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mediaPlayer = MediaPlayer.create(this.getContext(), R.raw.background);
        mediaPlayer.start();
        mediaPlayer.setLooping(true);

        boomMusic = MediaPlayer.create(this.getContext(), R.raw.explosion);

        //nb de cases a afficher :
        this.sz = 84;//taille des carre (84 de base)
        //a modifier : changer la taille du nb de case en fonction de la taille de l'ecran

        NB_CASES_X = (this.getWidth()/this.sz);
        NB_CASES_Y = (this.getHeight()/this.sz);
        NB_CASES_X++;
        //laisser cette config
        NB_CASES_Y-=1;//pas besoin sur un vrai telephone

        pos_y_winning = (NB_CASES_Y-2*laddersSZ);
        if(pos_y_winning<0){
            pos_y_winning=0;
        }

        //grid part :
        this.setBackgroundColor(0xFFFFFFFF);//background of grid
        initPaintElem();//used to draw the squared grid with a canvas

        //characters :
        Bitmap b = BitmapFactory.decodeResource(this.getResources(),R.drawable.angel);
        this.hero = new SKHero(this, b, 4, 3);
        b = BitmapFactory.decodeResource(this.getResources(),R.drawable.alien);
        this.alien = new SKVillain(this, b, 2, 2);
        b = BitmapFactory.decodeResource(this.getResources(),R.drawable.peach);
        this.peach = new SKPrincess(this,b,4,8);

        initTiles();

        //timer :
        cannonShooting = false;
        canons_idx = new Point(0,1);

        //game over:
        game_over = BitmapFactory.decodeResource(this.getResources(),R.drawable.ending_message);
        game_over = Bitmap.createScaledBitmap(game_over,(NB_CASES_X)*sz,5*sz,true);
        player_lost = false;

        //game won :
        game_won = BitmapFactory.decodeResource(this.getResources(),R.drawable.winning_game);
        game_won = Bitmap.createScaledBitmap(game_won,(NB_CASES_X)*sz,5*sz,true);
        player_win= false;

        //gameThread
        this.gameThread = new SKGame(this,holder);
        this.gameThread.setRunning(true);
        this.gameThread.start();
    }

    // Implements method of SurfaceHolder.Callback
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        this.gameThread.setRunning(false);
    }

    // Implements method of SurfaceHolder.Callback
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    //dessiner :
    public boolean isCollision(){//works
        Point p = this.hero.getPos();
        SKBullet c1 = canons.get(canons_idx.x);
        SKBullet c2 = canons.get(canons_idx.x);
        Point p_c1 = c1.getBulletPos();
        Point p_c2 = c2.getBulletPos();
        if(p.x == p_c1.x && p.y == p_c1.y){//eventuellement renvoyer true pour p.x+1 aussi pour corriger bug
            return true;
        }
        if(p.x == p_c2.x && p.y == p_c2.y){
            return true;
        }
        return false;
    }

    public void update()  {
        this.alien.update();
        this.hero.update(new_pos);

        //check if character won :
        if(this.hero.getPos().y==pos_y_winning && this.hero.getPos().x==8){
            player_win=true;
            mediaPlayer.stop();
        }

        //check for collisions with hero and bullet :
        if(isCollision()){
            this.hero.getHit();
            mediaPlayer.stop();
        }
        if(this.hero.getHP()<=0){
            if(!this.hero.getExploState()){//tant que l'animation n'est pas finie on la lance :
                this.hero.boom_animation();
                boomMusic.start();
            }
            else{
                boomMusic.stop();
                //game over : ending message appear + credits
                player_lost=true;
            }
        }

    }

    public void showGrid(Canvas canvas){//to debug
        for(int i=0;i<NB_CASES_X;i++){
            for(int j=0;j<NB_CASES_Y+1;j++){//colones
                canvas.drawRect(i*sz,j*sz, (i+1)*sz, (j+1)*sz, textPaint);
            }
        }
        //si left = right pour drawRect, alors le cote gauche et droit du rectangle sont confondus, on ne voit plus qu'une seule ligne vertical a l'ecran
        //sur l'axe horizontale du telephone : left = la ou le carre commence et right = la ou il s'arrete
        //sur l'axe verticale : meme logique mais avec top et bottom
    }

    public void drawGrid(Canvas canvas){
        //ELEMENTS STATIQUES :

        //la grille :
        //showGrid(canvas);

        //draw environnement of the game (tiles etc...)
        for(SKTile tile: canons) {
            tile.draw(canvas,this.sz);
        }
        for(SKTile tile: walls) {
            tile.draw(canvas,this.sz);
        }
        for(SKTile tile: bricks) {
            tile.draw(canvas,this.sz);
        }
        for(SKTile tile: ladders) {
            tile.draw(canvas,this.sz);
        }
        for(SKTile tile: voidBricks) {
            tile.draw(canvas,this.sz);
        }

        //ELEMENTS DYNAMIQUES :

        //finally drawing the character inside the grid : (with animations)
        update();//on update h24

        if(player_lost){
            this.alien.draw(canvas,this.sz);
            this.peach.draw(canvas,this.sz);
            shoot(canvas);//cannon shooting
            canvas.drawBitmap(game_over, 0*sz, 10*sz, null);
            //after that we wait 3 seconds and then return to the first screen
            gameThread.setRunning(false);
        }
        else if(player_win){
            this.hero.draw(canvas,this.sz);
            this.peach.draw(canvas,this.sz);
            canvas.drawBitmap(game_won, 0*sz, 10*sz, null);
            //after that we wait 3 seconds and then return to the first screen
            gameThread.setRunning(false);
        }
        else{
            this.hero.draw(canvas,this.sz);
            this.alien.draw(canvas,this.sz);
            this.peach.draw(canvas,this.sz);
            shoot(canvas);//cannon shooting
        }

    }

    @Override
    public void draw(Canvas canvas)  {
        super.draw(canvas);

        int color = (255 & 0xff) << 24 | (66 & 0xff) << 16 | (91 & 0xff) << 8 | (136 & 0xff);
        setBackgroundColor(color);
        //draw the grid first :
        drawGrid(canvas);

        invalidate();//important, sinon les evenements touch event ne fonctionnent pas
    }

    //pour les cannons qui tirent :
    public void setShooting(boolean val){//called by SKTimer periodically
        cannonShooting = val;
        //if true on creer une bullet dans une des SKTile de la liste cannon :
        if(val){
            Bitmap b = BitmapFactory.decodeResource(this.getResources(),R.drawable.bullet2);
            canons_idx = this.alien.getCanons(canonsSZ,NB_CASES_Y,this.hero.getPos());

            Point p = canons.get(canons_idx.x).getPos();
            canons.get(canons_idx.x).initBullet(b);

            p = canons.get(canons_idx.y).getPos();
            canons.get(canons_idx.y).initBullet(b);
        }
    }

    public void shoot(Canvas canvas){//shoots using only one canon for now
        if(cannonShooting){
            canons.get(canons_idx.x).decrBulletX();
            canons.get(canons_idx.x).drawBullet(canvas,this.sz);
            canons.get(canons_idx.y).decrBulletX();
            canons.get(canons_idx.y).drawBullet(canvas,this.sz);
        }
    }

    //for the character movement :
    public boolean isAnObstacle(int x, int y){//verifie qu'on ne se deplace pas sur un obstacle
        //true : on veut se deplacer sur un obstacle
        if(NB_CASES_Y%2==0){
            if(y%2==0){//change selon si les etages commencent sur une case Y paire ou non
                return false;
            }
        }
        else{
            if(y%2!=0){//change selon si les etages commencent sur une case Y paire ou non
                return false;
            }
        }
        for(SKTile tile: ladders) {
            Point l = tile.getPos();
            if(l.x == x && l.y == y){//c'est une ladder
                return false;
            }
        }
        return true;
        //fonctionne mais a modifier eventuellement qd le perso atteint le dernier etage...
    }

    //interaction utilisateur :
    @Override
    public boolean onTouchEvent(MotionEvent event) {//le personnage ne peut pas sauter et se deplace uniquement sur l'axe X
        //pour monter a l'etage il utilise des echelles
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            int x = (int)event.getX();
            int y = (int)event.getY();

            //on scale aux carreaux
            x = x/this.sz;
            y = y/this.sz;
            Log.d(TAG,"posx = "+x);
            Log.d(TAG,"posy = "+y);

            if(x>7 && y>pos_y_winning){//au dela de la zone des canons (a droite)
                x = 7; //on se dirige vers le mur !
            }

            new_pos.x = x; new_pos.y = y;

            return true;
        }
        return false;
    }

    //others :
    public int getSZGrid(){
        return this.sz;
    }

    private void setColorGrid(int A, int R, int G, int B){//we draw the grid with black line by default but we can change that here
        textColor = (A & 0xff) << 24 | (R & 0xff) << 16 | (G & 0xff) << 8 | (B & 0xff);
    }

    //initialisation :
    private void initPaintElem(){//on initialise les elements pas dans draw pour sauvegarder des ressources
        setColorGrid(255,0,0,0);// <=> 0xFF000000
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(textColor);
        //il faut preciser qu'on veut dessiner les contours seulement et pas remplir les surfaces
        textPaint.setStyle(Paint.Style.STROKE); // sans cela, les rectangles dessiner a l'ecran sont remplis !
    }

    private void initTiles(){
        //init les tableaux de tiles
        int y_pos = NB_CASES_Y-2;//position de depart pour l'axe y du premier canon
        for(int i=0;i<canonsSZ;i++){
            Bitmap b_c = BitmapFactory.decodeResource(this.getResources(),R.drawable.canon_1);
            SKBullet c = new SKBullet(this, b_c);
            c.setPos(8,y_pos);//x ne change pas, pour y on a : 23, 21, 19, 17 ...
            canons.add(c);
            y_pos -= 2;
        }
        //on possede les N cannons avec leur coordonnee a present

        y_pos = NB_CASES_Y;
        //on s'occupe des murs soutenant les canons mtn :
        for(int i=0;i<wallsSZ;i++){
            Bitmap b_m = BitmapFactory.decodeResource(this.getResources(),R.drawable.img_wall);
            SKTile m = new SKTile(this, b_m);

            if(i<1){
                m.setPos(8,y_pos);
                y_pos-=1;
            }
            else if(i<12 && i>=1) {
                m.setPos(8, y_pos);
                y_pos -= 2;
            }
            walls.add(m);
        }

        y_pos = NB_CASES_Y-1;
        boolean lineIsPair = true;//la ligne sur laquelle on se trouve est paire.
        //on s'occupe des briques mtn :
        for(int i=0; i<bricksLineSZ; i++){
            for(int j=0; j<8; j++){//on place seulement 2 briques sur
                Bitmap b_b = BitmapFactory.decodeResource(this.getResources(),R.drawable.metal);//grass must have a basic size of 84x84 at least to fully contain the grid case
                SKTile b = new SKTile(this, b_b);
                if(lineIsPair){//pair
                    b.setPos(j, y_pos);
                }
                else{//impair
                    b.setPos(j, y_pos);
                }
                bricks.add(b);
            }
            y_pos-=2;
            lineIsPair = !lineIsPair;
        }

        //mtn on s'occupe des echelles : 1 par etage max
        lineIsPair = false;
        y_pos = NB_CASES_Y-1;
        for(int i=0;i<laddersSZ;i++){
            Bitmap b_b = BitmapFactory.decodeResource(this.getResources(), R.drawable.ladder);
            SKTile b = new SKTile(this, b_b, this.sz, this.sz*2);
            if(lineIsPair){//impair
                b.setPos(1, y_pos);
            }
            else{//pair
                b.setPos(5, y_pos);
            }
            ladders.add(b);
            y_pos-=2;
            lineIsPair = !lineIsPair;
        }

        int x_pos = NB_CASES_X-3;
        //finally the princess's bricks :
        for(int i=0;i<voidBrickSZ;i++){
            Bitmap b_b = BitmapFactory.decodeResource(this.getResources(), R.drawable.img_wall);
            SKTile b = new SKTile(this, b_b);
            b.setPos(x_pos, pos_y_winning+1);
            voidBricks.add(b);
            x_pos+=1;
        }

        //init hero position and other things :
        this.hero.setPos(4,NB_CASES_Y);
        new_pos = new Point(4,NB_CASES_Y);//si on oublie d'initialiser 1 seul attribut l'application crache...
        peach.setPos(NB_CASES_X-3,pos_y_winning);
        this.alien.setPos(0,pos_y_winning-4);

        //NB_CASES_Y+=2;

    }
    //pour NB_CASES_Y=24 -> on garde cette cnfig
}