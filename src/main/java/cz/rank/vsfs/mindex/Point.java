package cz.rank.vsfs.mindex;

import net.jcip.annotations.Immutable;

@Immutable
public class Point implements Distanceable<Point> {
    private final double x;
    private final double y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public double distance(final Point point) {
        final double distX = x - point.x;
        final double distY = y - point.y;
        return Math.sqrt(distX * distX + distY * distY);
    }

    @Override
    public String toString() {
        return "Point [x=" + x + ", y=" + y + "]";
    }

}
