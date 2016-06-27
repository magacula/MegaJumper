package com.missionbit.megajumper;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;

public class MegaJumper extends ApplicationAdapter {
    private static final int NUM_OF_PLATFORMS = 5;

    private SpriteBatch batch;
    private OrthographicCamera camera;
    private OrthographicCamera uiCamera;
    private int width, height, score;
    private Vector2 gravity;
    private ArrayList<Platform> platforms;
    private Player jumper;
    Texture background;


    //ArrayList<Platform> platforms;
    BitmapFont font;
    enum GameState {START, IN_GAME, GAME_OVER}
    GameState state;

    boolean debug = true;

    @Override
    public void create () {
        width = Gdx.graphics.getWidth();
        height = Gdx.graphics.getHeight();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, width, height);
        uiCamera = new OrthographicCamera();
        uiCamera.setToOrtho(false, width, height);
        uiCamera.update();
        jumper = new Player();
        batch = new SpriteBatch();
        platforms = new ArrayList<Platform>();
        gravity = new Vector2();
        font = new BitmapFont(Gdx.files.internal("arial.fnt"),
        Gdx.files.internal("arial.png"), false);
        resetGame();
        background = new Texture("bg.jpg");
    }

    @Override
    public void render () {
        Gdx.gl.glClearColor((float)0.5, (float)0.5, (float)0.5, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        updateGame();
        drawGame();
    }

    private void resetGame() {
        //reset any game state variables here
        score = 0;
        state = GameState.START;
        gravity.set(0, -25);

        jumper.setPosition(width / 2 - jumper.getBounds().getWidth() / 2, height / 2);
        jumper.setVelocity(0, 0);

        platforms.clear();
        for (int i = 0; i < NUM_OF_PLATFORMS; i++) {
            platforms.add(new Platform());
            platforms.get(i).setPosition((float)Math.random() * width, i * height / NUM_OF_PLATFORMS);
        }
    }

    private void updateGame() {
        //game logic stuff here
        float deltaTime = Gdx.graphics.getDeltaTime();

        //controls left-right movement, multiplier controls how responsive controls feel
        jumper.setAccel(Gdx.input.getAccelerometerX(), -250);
        jumper.getAccel();
        camera.position.y = jumper.getPosition().y;

        //updates bounds because you need your bounds to follow the visuals
        jumper.setBounds(jumper.getPosition().x, jumper.getPosition().y);
        for (int i = 0; i < NUM_OF_PLATFORMS; i++)
            platforms.get(i).setBounds(platforms.get(i).getPosition().x, platforms.get(i).getPosition().y);


        //game states
        if (state == GameState.START) {
            if (Gdx.input.justTouched()) {
                state = GameState.IN_GAME;
                jumper.setVelocity(0,250);
                jumper.getPosition().mulAdd(jumper.getVelocity(), deltaTime);
                //jumper.position.add(jumper.getVelocity().x * deltaTime, jumper.getVelocity().y * deltaTime);
            }
        }

        else if (state == GameState.IN_GAME) {
            jumper.getVelocity().add(gravity);

            //changes direction right when you change tilt threshold, comment out for unresponsive movement
            if (Gdx.input.getAccelerometerX() > 0 || Gdx.input.getAccelerometerX() < 0)
                jumper.getVelocity().x = 0;

            //update jumper velocity and update position
            jumper.getVelocity().x += jumper.getAccel();
            jumper.getPosition().mulAdd(jumper.getVelocity(), deltaTime);

            //platform logic
            float lowestPlatform = platforms.get(0).getPosition().y;
            for (int i = 0; i < NUM_OF_PLATFORMS; i++) {
                //movies platform up after it falls below the camera's "eye"
                if (platforms.get(i).getPosition().y < camera.position.y - height / 2) {
                    platforms.get(i).setPosition((float) Math.random() * width, platforms.get(i).getPosition().y + height);
                }
                //updates the y position you need to be below to die
                if (platforms.get(i).getPosition().y < lowestPlatform) {
                    lowestPlatform = platforms.get(i).getPosition().y;
                }
            }
            if (jumper.getPosition().y < 0) {
                state = GameState.GAME_OVER;
            }

            //collision code, kinda bad but it works lol
            for (int i = 0; i < NUM_OF_PLATFORMS; i++) {
                if (jumper.getPosition().y >= (platforms.get(i).getPosition().y + (platforms.get(i).getBounds().getHeight() / 2)) && jumper.getBounds().overlaps(platforms.get(i).getBounds())) {
                    jumper.setVelocity(0, 1000);
                    score += 1;
                }
            }
        }

        else { //state == GameState.GAME_OVER
            if (Gdx.input.justTouched()) {
                resetGame();
            }
        }
    }

    private void drawGame() {
        //game world camera
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        font.setColor(0, 0, 0, 1);
        batch.draw(background, camera.position.x - width / 2, camera.position.y - height /2, width, height);
        if (state == GameState.IN_GAME) {
            for (int i = 0; i < NUM_OF_PLATFORMS; i++) {
                platforms.get(i).draw(batch);
            }
            jumper.draw(batch);
        }
        batch.end();

        //game ui camera
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        //debug messages
        boolean debug = true;
        if (debug) {
            font.setScale(1);
            font.draw(batch, "Game state: " + state, 20, height - 20);
            font.draw(batch, "Accel X: " + (int)jumper.getAccel(), 20, height - 70);
            font.draw(batch, "Velocity Y: " + (int)jumper.getVelocity().y, 20, height - 120);
            font.draw(batch, "Phone resolution: " + width + ", " + height, 20, height - 170);
        }

        font.setScale(2);
        if (state == GameState.START) {
            font.draw(batch, "Tap to start!", width / 2 - font.getBounds("Tap to start!").width / 2, height / 2);
        } else if (state == GameState.IN_GAME) {
            font.draw(batch, "Score: " + score, width / 2 - font.getBounds("Score: "+ score).width / 2, height - 250);

        } else { //state == GameState.GAME_OVER
            font.draw(batch, "Score: " + score, width / 2 - font.getBounds("Score: "+ score).width / 2, Gdx.graphics.getHeight() / 2 + font.getBounds("S").height + 10);
            font.draw(batch, "Tap to restart", width / 2 - font.getBounds("Tap to restart").width / 2, Gdx.graphics.getHeight() / 2);
        }
        batch.end();
    }
}
