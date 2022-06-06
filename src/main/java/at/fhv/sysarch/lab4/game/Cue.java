package at.fhv.sysarch.lab4.game;

import java.util.Optional;

public class Cue {
    private Optional<Double> startX = Optional.empty();
    private Optional<Double> startY = Optional.empty();
    private Optional<Double> endX = Optional.empty();
    private Optional<Double> endY = Optional.empty();

    public boolean inUse() {
        return startX.isPresent() && startY.isPresent() && endX.isPresent() && endY.isPresent();
    }

    public void release() {
        this.startX = Optional.empty();
        this.startY = Optional.empty();
        this.endX = Optional.empty();
        this.endY = Optional.empty();
    }

    public Optional<Double> getStartX() {
        return startX;
    }

    public Optional<Double> getStartY() {
        return startY;
    }

    public Optional<Double> getEndX() {
        return endX;
    }

    public Optional<Double> getEndY() {
        return endY;
    }

    public void setStartX(double x) {
        this.startX = Optional.of(x);
    }

    public void setStartY(double y) {
        this.startY = Optional.of(y);
    }

    public void setEndX(double x) {
        this.endX = Optional.of(x);
    }

    public void setEndY(double y) {
        this.endY = Optional.of(y);
    }
}
