package at.fhv.sysarch.lab4.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import at.fhv.sysarch.lab4.physics.BallPocketedListener;
import at.fhv.sysarch.lab4.physics.BallsCollisionListener;
import at.fhv.sysarch.lab4.physics.ObjectsRestListener;
import at.fhv.sysarch.lab4.physics.Physics;
import at.fhv.sysarch.lab4.rendering.Renderer;
import javafx.scene.input.MouseEvent;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.RaycastResult;
import org.dyn4j.geometry.Ray;
import org.dyn4j.geometry.Vector2;

public class Game implements BallPocketedListener, BallsCollisionListener, ObjectsRestListener {
    private final Renderer renderer;
    private final Physics physics;

    private Player currentPlayer = Player.ONE;
    private int playerOneScore = 0;
    private int playerTwoScore = 0;

    private boolean isCalculatingResults = false;

    private boolean foulOccurred = false;
    private boolean whiteIsPocketed = false;
    private boolean whiteTouchedColor = false;

    private double physicsStartX;
    private double physicsStartY;
    private double physicsEndX;
    private double physicsEndY;

    public Game(Renderer renderer, Physics physics) {
        this.renderer = renderer;
        this.physics = physics;

        physics.setBallPocketedListener(this);
        physics.setBallsCollisionListener(this);
        physics.setObjectsRestListener(this);

        renderer.setActionMessage("Player " + currentPlayer.name() + " is now on turn!");

        this.initWorld();
    }

    public void onMousePressed(MouseEvent e) {
        double mouseX = e.getX();
        double mouseY = e.getY();

        renderer.getCue().setStartX(mouseX);
        renderer.getCue().setStartY(mouseY);

        this.physicsStartX = this.renderer.screenToPhysicsX(mouseX);
        this.physicsStartY = this.renderer.screenToPhysicsY(mouseY);
    }

    public void setOnMouseDragged(MouseEvent e) {
        double mouseX = e.getX();
        double mouseY = e.getY();

        renderer.getCue().setEndX(mouseX);
        renderer.getCue().setEndY(mouseY);

        this.physicsEndX = renderer.screenToPhysicsX(mouseX);
        this.physicsEndY = renderer.screenToPhysicsY(mouseY);
    }

    public void onMouseReleased(MouseEvent e) {
        renderer.getCue().release();

        if (!isCalculatingResults) {
            Vector2 start = new Vector2(physicsStartX, physicsStartY);
            Vector2 direction = new Vector2(physicsStartX - physicsEndX, physicsStartY - physicsEndY);

            if (!direction.equals(new Vector2(0, 0))) {
                Ray ray = new Ray(start, direction);
                ArrayList<RaycastResult> rayHits = new ArrayList<>();
                this.physics.getWorld().raycast(ray, 0.2, false, true, rayHits);

                Optional<RaycastResult> rayHitBall = rayHits.stream().filter(rayHit -> rayHit.getBody().getUserData() instanceof Ball).findFirst();

                if (rayHitBall.isPresent()) {
                    isCalculatingResults = true;
                    renderer.setStrikeMessage("Calculating ...");
                    renderer.setFoulMessage("");

                    Body ballBody = rayHitBall.get().getBody();
                    Ball ball = (Ball) ballBody.getUserData();

                    if (!ball.isWhite()) {
                        renderer.setFoulMessage("Cue hit color");

                        if (!foulOccurred) {
                            if (currentPlayer == Player.ONE) {
                                playerOneScore--;
                                renderer.setPlayer1Score(playerOneScore);
                            } else {
                                playerTwoScore--;
                                renderer.setPlayer2Score(playerTwoScore);
                            }
                        }
                    }

                    double dx = physicsStartX - physicsEndX;
                    double dy = physicsStartY - physicsEndY;
                    double strength = Math.min(Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2)) * 1000, 400);

                    ballBody.applyForce(new Vector2(dx, dy).multiply(strength));
                }
            }
        }
    }

    private void placeBalls(List<Ball> balls) {
        Collections.shuffle(balls);

        // positioning the billard balls IN WORLD COORDINATES: meters
        int row = 0;
        int col = 0;
        int colSize = 5;

        double y0 = -2*Ball.Constants.RADIUS*2;
        double x0 = -Table.Constants.WIDTH * 0.25 - Ball.Constants.RADIUS;

        for (Ball b : balls) {
            double y = y0 + (2 * Ball.Constants.RADIUS * row) + (col * Ball.Constants.RADIUS);
            double x = x0 + (2 * Ball.Constants.RADIUS * col);

            b.setPosition(x, y);
            b.getBody().setLinearVelocity(0, 0);
            physics.getWorld().addBody(b.getBody());
            renderer.addBall(b);

            row++;

            if (row == colSize) {
                row = 0;
                col++;
                colSize--;
            }
        }
    }

    private void initWorld() {
        List<Ball> balls = new ArrayList<>();
        
        for (Ball b : Ball.values()) {
            if (b == Ball.WHITE)
                continue;

            balls.add(b);
        }
       
        this.placeBalls(balls);

        Ball.WHITE.setPosition(Table.Constants.WIDTH * 0.25, 0);
        physics.getWorld().addBody(Ball.WHITE.getBody());
        renderer.addBall(Ball.WHITE);
        
        Table table = new Table();
        physics.getWorld().addBody(table.getBody());
        renderer.setTable(table);
    }

    @Override
    public boolean onBallPocketed(Ball b) {
        if (b.isWhite()) {
            renderer.setFoulMessage("White pocketed");

            if (!foulOccurred) {
                if (currentPlayer == Player.ONE) {
                    playerOneScore--;
                    renderer.setPlayer1Score(playerOneScore);
                } else {
                    playerTwoScore--;
                    renderer.setPlayer2Score(playerTwoScore);
                }
            }

            whiteIsPocketed = true;
        }

        if (!foulOccurred) {
            if (currentPlayer == Player.ONE) {
                playerOneScore++;
                renderer.setPlayer1Score(playerOneScore);
            } else {
                playerTwoScore++;
                renderer.setPlayer2Score(playerTwoScore);
            }
        }

        physics.getWorld().removeBody(b.getBody());
        renderer.removeBall(b);
        return true;
    }

    @Override
    public void onBallsCollide(Ball b1, Ball b2) {
        if (b1.isWhite() || b2.isWhite()) {
            whiteTouchedColor = true;
        }
    }

    @Override
    public void objectsAreResting() {
        if (isCalculatingResults) {
            if (whiteIsPocketed) {
                Ball.WHITE.setPosition(Table.Constants.WIDTH * 0.25, 0);
                Body white = Ball.WHITE.getBody();
                white.setLinearVelocity(new Vector2(0,0));
                physics.getWorld().addBody(white);
                renderer.addBall(Ball.WHITE);
                whiteIsPocketed = false;
            }

            if (!whiteTouchedColor && !foulOccurred) {
                renderer.setFoulMessage("White did not touch color");

                if (currentPlayer == Player.ONE) {
                    playerOneScore--;
                    renderer.setPlayer1Score(--playerOneScore);
                } else {
                    playerTwoScore--;
                    renderer.setPlayer2Score(playerTwoScore);
                }
            }

            currentPlayer = (currentPlayer == Player.ONE) ? Player.TWO : Player.ONE;

            foulOccurred = false;
            whiteIsPocketed = false;
            whiteTouchedColor = false;

            renderer.setActionMessage("Player " + currentPlayer.name() + " is now on turn!");
            renderer.setStrikeMessage("");

            isCalculatingResults = false;
        }
    }
}