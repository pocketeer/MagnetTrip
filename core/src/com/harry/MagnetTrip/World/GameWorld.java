package com.harry.MagnetTrip.World;


import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.harry.MagnetTrip.Actors.*;
import com.harry.MagnetTrip.Datas.Score;
import com.harry.MagnetTrip.Map.MapGenerator;

/**
 * Created by jh on 2014-07-05.
 */
public class GameWorld implements InputProcessor, ContactListener{
    final static float DELTA_LIMT = 0.03f;
    final static float CAR_INTERVAL = 100f; //화면의 맨처음 부분으로부터 떨어진 간건
    public static final int GAME_READY = 0;
    public static final int GAME_RUNNING = 1;
    public static final int GAME_PAUSED = 2;
    public static final int GAME_OVER = 3;
    public static final int GAME_RESTART = 4;

    private int state;
    private Score score;

    float displayWidth;
    float displayHeight;

    //group variables
    protected Group backgroundGroup;
    protected Group actorGroup;
    protected Group uiGroup;

    //ui variables
    protected TextActor scoreText;

    protected ArrayList<ForceObstacle> forcePlanetList;
    protected ArrayList<MyActor> mapPieceList;
    protected Stage stage;
    protected World world;
    protected Car car;
    protected Box2DDebugRenderer debugRenderer;
    protected Viewport viewport;
    protected MapGenerator mapGenerator;

    public GameWorld(Viewport viewport) {
        this.viewport = viewport;
        displayWidth = this.viewport.getCamera().viewportWidth;
        displayHeight = this.viewport.getCamera().viewportHeight;
        Gdx.app.log("GameWorld", "width = " + displayWidth);
        Gdx.app.log("GameWorld", "height = " + displayHeight);

        mapPieceList = new ArrayList<MyActor>();
        forcePlanetList = new ArrayList<ForceObstacle>();

        world = new World(new Vector2(0, 0), true);
        debugRenderer = new Box2DDebugRenderer();
        stage = new Stage();
        score = new Score();
        state = GAME_READY;


        stage.setViewport(viewport);



        backgroundGroup = new Group();
        actorGroup = new Group();
        uiGroup = new Group();

        stage.addActor(backgroundGroup);
        stage.addActor(actorGroup);
        stage.addActor(uiGroup);

        initMap();

        car = new Car(world, new Vector2(20, displayHeight/2), 50, 30);

        scoreText = new TextActor(Gdx.files.internal("fonts/maejum.ttf") , 30, Color.CYAN, new Vector2(displayWidth-200, displayHeight-35));
        Gdx.app.log("GameWorld", "width = " + displayWidth);
        scoreText.setText("Hello World");
        actorGroup.addActor(car);
        uiGroup.addActor(scoreText);

        updateRunning(0.01f);
        setCameraPosition(0);

        Gdx.input.setInputProcessor(this);
        world.setContactListener(this);
    }

    public void initMap() {
        mapGenerator = new MapGenerator(this, "mapDatas.json", 3);
        mapGenerator.addBackgroundToWorld();
        mapGenerator.addMapPieceToWorld();
        mapGenerator.addMapPieceToWorld();
    }

    public void update() {
        float deltaTime = Gdx.graphics.getDeltaTime();

        switch (state) {
            case GAME_READY:
                updateReady();
                break;
            case GAME_RUNNING:
                updateRunning(deltaTime);
                break;
            case GAME_PAUSED:
                updatePaused();
                break;
            case GAME_OVER:
                updateGameOver();
                break;
        }
    }



    //update related
    private void updateRunning(float deltaTime) {
        world.step(deltaTime, 10, 10);
        world.clearForces();
        stage.act(deltaTime);
        updateUI();

        checkAll();
        setCameraPosition(car.getX());
    }

    private void updateReady() {

    }

    private void updatePaused () {
    }

    private void updateGameOver () {
    }

    private void updateUI() {
        updateScore();
        uiGroup.setPosition(car.getX()-CAR_INTERVAL, 0);
        scoreText.setText("Score : " + score);
    }

    private void updateScore() {
        int frameNum = (int)(car.getX()/displayWidth);
        score.setScore(frameNum);
    }

    //check realted
    private void checkAll() {
        checkGameOver();
        checkObstacle();
        checkGenerateMap();
    }

    private void checkGameOver() {
        if(car.getY()<(0-30) || car.getY() > (displayHeight+30)) {
            state = GAME_OVER;
        }
    }

    private void checkGenerateMap() {
        if ((mapGenerator.getMapCount()-mapGenerator.getMaxNumtoStay()+1)*displayWidth < car.getX()-displayHeight/2) {
            mapGenerator.addMapPieceToWorld();

            Gdx.app.log("add", "(mapGenerator.getMapCount()-1)*displayWidth = "+ (mapGenerator.getMapCount()-1)*displayWidth);
            Gdx.app.log("add", "car.getX()-displayHeight/2"+ (car.getX()-displayHeight/2));
        }
    }

    private void checkObstacle() {
        checkObstacleAction();
        checkRemoveObstacles();
    }

    private void checkObstacleAction() {
        Body carBody = car.getBody();
        for (ForceObstacle fo : forcePlanetList) {
            if(fo.isActive()) {
                fo.applyForceToCar(carBody);
            }
        }
    }

    private void checkRemoveObstacles() {
        ArrayList<ForceObstacle> obstaclestoRemove = new ArrayList<ForceObstacle>();

        for(ForceObstacle fo : forcePlanetList) {
            if (fo.lessThanFloat(car.getCenterX() - displayWidth)) {
                obstaclestoRemove.add(fo);
            }
        }

        for(ForceObstacle fo : obstaclestoRemove) {
            removeMyAcotr((MyActor)fo);
        }
    }






    public void draw() {
        stage.draw();
        //debugRenderer.render(world, viewport.getCamera().combined);
    }

    public void dispose() {
        world.dispose();
        stage.dispose();
    }



    //camera related
    private void setCameraPosition(float x) {
        viewport.getCamera().position.set(displayWidth/2 + x-CAR_INTERVAL, displayHeight/2, 0);
    }

    //Input Processor related
    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        switch (state) {
            case GAME_READY:
                state = GAME_RUNNING;
                break;
            case GAME_RUNNING:
                for (ForceObstacle fo : forcePlanetList) {
                    fo.setActive(true);
                }
                break;
            case GAME_PAUSED:
                break;
            case GAME_OVER:
                state = GAME_RESTART;
                break;
        }

        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        switch (state) {
            case GAME_READY:

                break;
            case GAME_RUNNING:
                for (ForceObstacle fo : forcePlanetList) {
                    fo.setActive(false);
                }
                break;
            case GAME_PAUSED:
                break;
            case GAME_OVER:
                break;
        }
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }

    //getter and setter
    public World getWorld() {
        return world;
    }

    public float getDisplayWidth() {
        return displayWidth;
    }

    public float getDisplayHeight() {
        return displayHeight;
    }

    public int getState() {
        return state;
    }

    /*
        public Batch getBatch() {
            return stage.getBatch();
        }
        */
    //MyActor related
    public void addObstacle(ForceObstacle forceObstacle) {
        forcePlanetList.add(forceObstacle);
        actorGroup.addActor((MyActor)forceObstacle);
    }

    public void removeMyAcotr(MyActor actor) {
        forcePlanetList.remove(actor);
        actor.destroy();
    }


    public void addBackground(MyActor background) {
        mapPieceList.add(background);
        backgroundGroup.addActor(background);
    }

    public void removeBackground(MyActor background) {
        mapPieceList.remove(background);
        background.destroy();
    }

    //contact listener related
    @Override
    public void beginContact(Contact contact) {
        state = GAME_OVER;
    }

    @Override
    public void endContact(Contact contact) {

    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }
}
