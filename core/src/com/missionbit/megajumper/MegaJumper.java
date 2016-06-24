package com.missionbit.megajumper;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;

public class MegaJumper extends ApplicationAdapter {
    SpriteBatch batch;
    OrthographicCamera camera;
    OrthographicCamera uiCamera;
    int width, height, score;
    Vector2 gravity;
    Player jumper;
    Platform platform;
    Platform platform2;
    //ArrayList<Platform> platforms;
    BitmapFont font;
    enum GameState {START, IN_GAME, GAME_OVER}
    GameState state;

    boolean debug = true;

    @Override
    public void create () {
        width = Gdx.graphics.getWidth();
        height = Gdx.graphics.getHeight();
        jumper = new Player();
        batch = new SpriteBatch();
        platform = new Platform(0, 0);
        platform2 = new Platform (0,0);
        gravity = new Vector2();
        font = new BitmapFont(Gdx.files.internal("arial.fnt"),
        Gdx.files.internal("arial.png"), false);
        resetGame();
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
        platform.setPosition(width / 2 - platform.getBounds().getWidth() / 2, height / 2 - 500);
        platform2.setPosition(width / 2 - platform2.getBounds().getWidth() / 2, height / 2 - 250);
    }

    private void updateGame() {
        //game logic stuff here
        float deltaTime = Gdx.graphics.getDeltaTime();

        //controls left-right movement, multiplier controls how responsive controls feel
        jumper.setAccel(Gdx.input.getAccelerometerX(), -250);
        jumper.getAccel();

        //updates bounds because you need your bounds to follow the visuals
        jumper.setBounds(jumper.getPosition().x, jumper.getPosition().y);
        platform.setBounds(platform.getPosition().x, platform.getPosition().y);
        platform2.setBounds(platform2.getPosition().x, platform2.getPosition().y);


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
            if (Gdx.input.getAccelerometerX() > 0 ||Gdx.input.getAccelerometerX() < 0) jumper.getVelocity().x = 0;

            //update jumper velocity and update position
            jumper.getVelocity().x += jumper.getAccel();
            jumper.getPosition().mulAdd(jumper.getVelocity(), deltaTime);

            if (jumper.getPosition().y < 0) {
                state = GameState.GAME_OVER;
            }

            //collision code, kinda bad but it works lol
            if (jumper.getPosition().y >= (platform.getPosition().y + (platform.getBounds().getHeight() / 2)) && jumper.getBounds().overlaps(platform.getBounds())) {
                jumper.setVelocity(0, 850);
                score+=1;
            }
            if (jumper.getPosition().y >= (platform2.getPosition().y + (platform2.getBounds().getHeight() / 2)) && jumper.getBounds().overlaps(platform2.getBounds())) {
                jumper.setVelocity(0, 500);
                score += 1;
            }
        }

        else { //state == GameState.GAME_OVER
            if (Gdx.input.justTouched()) {
                resetGame();
            }
        }
    }

    private void drawGame() {
        batch.begin();
        font.setColor(0, 0, 0, 1);

        //debug messages
        if (debug) {
            font.setScale((float)0.5);
            font.draw(batch, "Game state: " + state, 20, Gdx.graphics.getHeight() - 20);
            font.draw(batch, "Accel X: " + (int)jumper.getAccel(), 20, Gdx.graphics.getHeight() - 70);
            font.draw(batch, "Velocity Y: " + (int)jumper.getVelocity().y, 20, Gdx.graphics.getHeight() - 120);
            font.draw(batch, "Phone resolution: " + width + ", " + height, 20, Gdx.graphics.getHeight() - 170);
        }

        font.setScale(2);
        if (state == GameState.START) {
            font.draw(batch, "Tap to start!", Gdx.graphics.getWidth() / 2 - font.getBounds("Tap to start!").width / 2, Gdx.graphics.getHeight() / 2);
        } else if (state == GameState.IN_GAME) {
            platform.draw(batch);
            platform2.draw(batch);
            jumper.draw(batch);
            font.draw(batch, "Score: " + score, Gdx.graphics.getWidth() / 2 - font.getBounds("Score: "+ score).width / 2, Gdx.graphics.getHeight() - 250);

        } else { //state == GameState.GAME_OVER
            font.draw(batch, "Score: " + score, Gdx.graphics.getWidth() / 2 - font.getBounds("Score: "+ score).width / 2, Gdx.graphics.getHeight() / 2 + font.getBounds("S").height + 10);
            font.draw(batch, "Tap to restart", Gdx.graphics.getWidth() / 2 - font.getBounds("Tap to restart").width / 2, Gdx.graphics.getHeight() / 2);
        }
        batch.end();
    }
}
