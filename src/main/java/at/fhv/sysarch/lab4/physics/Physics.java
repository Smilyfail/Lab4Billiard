package at.fhv.sysarch.lab4.physics;

import at.fhv.sysarch.lab4.game.Ball;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.Step;
import org.dyn4j.dynamics.StepListener;
import org.dyn4j.dynamics.World;
import org.dyn4j.dynamics.contact.ContactListener;
import org.dyn4j.dynamics.contact.ContactPoint;
import org.dyn4j.dynamics.contact.PersistedContactPoint;
import org.dyn4j.dynamics.contact.SolvedContactPoint;
import org.dyn4j.geometry.Vector2;

import java.util.concurrent.atomic.AtomicInteger;

public class Physics implements ContactListener, StepListener {
    private World world;
    private BallPocketedListener ballPocketedListener;
    private BallsCollisionListener ballsCollisionListener;
    private ObjectsRestListener objectsRestListener;

    public Physics() {
        this.world = new World();
        this.world.setGravity(World.ZERO_GRAVITY);
        this.world.addListener(this);
    }

    public World getWorld() {
        return world;
    }

    @Override
    public boolean begin(ContactPoint point) {
        Object object1 = point.getBody1().getUserData();
        Object object2 = point.getBody2().getUserData();

        if (object1 instanceof Ball && object2 instanceof Ball) {
            ballsCollisionListener.onBallsCollide((Ball) object1, (Ball) object2);
        }

        return true;
    }

    @Override
    public void end(Step step, World world) {
        AtomicInteger activeBalls = new AtomicInteger();

        world.getBodies().forEach(body -> {
            if (body.getLinearVelocity().equals(new Vector2(0, 0))) {
                activeBalls.getAndIncrement();
            }
        });

        if (activeBalls.get() == world.getBodyCount()) {
            objectsRestListener.objectsAreResting();
        }
    }

    @Override
    public boolean persist(PersistedContactPoint point) {
        if (point.isSensor()) {
            Body body1 = point.getBody1();
            Body body2 = point.getBody2();

            Body ball;
            Body pocket;

            if (body1.getUserData() instanceof Ball) {
                ball = body1;
                pocket = body2;
            } else {
                ball = body2;
                pocket = body1;
            }

            if (pocket.contains(ball.getWorldCenter())) {
                ballPocketedListener.onBallPocketed((Ball) ball.getUserData());
            }
        }

        return true;
    }

    public void setBallPocketedListener(BallPocketedListener ballPocketedListener) {
        this.ballPocketedListener = ballPocketedListener;
    }

    public void setBallsCollisionListener(BallsCollisionListener ballsCollisionListener) {
        this.ballsCollisionListener = ballsCollisionListener;
    }

    public void setObjectsRestListener(ObjectsRestListener objectsRestListener) {
        this.objectsRestListener = objectsRestListener;
    }





    // no code needed below
    @Override
    public void begin(Step step, World world) {

    }

    @Override
    public void updatePerformed(Step step, World world) {

    }

    @Override
    public void postSolve(Step step, World world) {

    }

    @Override
    public void sensed(ContactPoint point) {

    }

    @Override
    public void end(ContactPoint point) {

    }

    @Override
    public boolean preSolve(ContactPoint point) {
        return true;
    }

    @Override
    public void postSolve(SolvedContactPoint point) {

    }
}
