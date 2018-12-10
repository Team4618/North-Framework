package north.util;

public class Vector2 {
    public double x, y;

    public Vector2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double length() {
        return Math.sqrt(x * x + y * y);
    }

    public double angle() {
        return Math.atan2(y, x);
    }

    public static Vector2 lerp(Vector2 a, double t, Vector2 b) {
        return new Vector2(InterpolatingMap.doubleLerp(a.x, t, b.x),
                           InterpolatingMap.doubleLerp(a.y, t, b.y));
    }

    public static Vector2 sub(Vector2 a, Vector2 b) {
        return new Vector2(a.x - b.x, a.y - b.y);
    }

    public static Vector2 mul(Vector2 v, double s) {
        return new Vector2(s * v.x, s * v.y);
    }
}
