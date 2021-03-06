package com.harry.MagnetTrip.Actors;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.harry.MagnetTrip.Assets;

/**
 * Created by jh on 2014-07-16.
 */
public class Planet extends Box2dActor implements ForceObstacle {
    protected boolean isActive;
    protected int magneticRange;
    protected Animation planetFieldActiveAnimation;
    protected Animation planetFieldInactiveAnimation;
    protected Animation currentAnimation;


    public Planet(World world, Animation planetFieldactiveAnimation, Vector2 pos, int magneticRange, float width, float height) {
        super(world, Assets.planetAnimation, pos, BodyDef.BodyType.StaticBody, width, height);
        this.magneticRange = magneticRange;
        this.isActive = false;
        this.planetFieldActiveAnimation = planetFieldactiveAnimation;
        this.planetFieldInactiveAnimation = Assets.planetFieldInactiveAnimation;
        this.currentAnimation = this.planetFieldInactiveAnimation;
        makeCircleFixture(width/2, 1f, 0);
        setTouchable(Touchable.enabled);
    }



    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        currentFrame = currentAnimation.getKeyFrame(stateTime, true);
        batch.draw(currentFrame, getX()-(float)(magneticRange -1)/2*getWidth(), getY()-(float)(magneticRange -1)/2*getHeight(), getOriginX(), getOriginY(),
                getWidth()* magneticRange, getHeight()* magneticRange, getScaleX(), getScaleY(), getRotation());
    }



    protected int getMagnetPower() {
        return magneticRange;
    } //나중에 바꾸기


    @Override
    public void applyForceToCar(Body carBody) {

    }

    @Override
    public boolean isActive() {
        return isActive;
    }

    @Override
    public void setActive(boolean isActive) {
        this.isActive = isActive;
        if (isActive) {
            currentAnimation = planetFieldActiveAnimation;
        } else {
            currentAnimation = planetFieldInactiveAnimation;
        }
    }

    @Override
    public boolean lessThanFloat(float standard) { //기준점 보다 위치가 낮은가?
        if(getCenterX() < standard) {
           return true;
        }
        return false;
    }
}
