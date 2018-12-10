package north.curves;

import north.util.InterpolatingMap;
import north.util.Vector2;

import java.util.List;

public class SegmentedCurve {
    InterpolatingMap<Vector2> points = new InterpolatingMap<>(Vector2::lerp);
    public double length = 0;

    public static Vector2 quadraticBezier(Vector2 a, Vector2 b, Vector2 c, double t) {
        Vector2 result = new Vector2(0, 0);
        result.x = Math.pow(1 - t, 2) * a.x + 2 * t * (1 - t) * b.x + Math.pow(t, 2) * c.x;
        result.y = Math.pow(1 - t, 2) * a.y + 2 * t * (1 - t) * b.y + Math.pow(t, 2) * c.y;
        return result;
    }

    public void segmentQuadraticBezierCurve(Vector2 a, Vector2 b, Vector2 c) {
        double ds = 0.0001; //this creates approx 10000 segments, thats too many
        Vector2 lastv = quadraticBezier(a, b, c, 0);
        for (double s = ds; s <= 1.0 - ds; s += ds) {
            Vector2 v = quadraticBezier(a, b, c, s);

            points.put(length, lastv);
            length += Math.sqrt((v.x - lastv.x) * (v.x - lastv.x) + (v.y - lastv.y) * (v.y - lastv.y));
            lastv = v;
        }
    }

    public SegmentedCurve(List<Vector2> in_points) {
        if(in_points.size() == 2) {
            Vector2 a = in_points.get(0);
            Vector2 b = in_points.get(0);
            length = Math.sqrt((b.x - a.x) * (b.x - a.x) + (b.y - a.y) * (b.y - a.y));
            points.put(0.0, a);
            points.put(length, b);
        } else if(in_points.size() > 2) {
            int i = 0;
            for (; i < in_points.size() - 3; i += 2) {
                Vector2 p0 = in_points.get(i);
                Vector2 p1 = in_points.get(i + 1);
                Vector2 p2 = in_points.get(i + 2);

                Vector2 mid = new Vector2((p1.x + p2.x) / 2, (p1.y + p2.y) / 2);
                in_points.add(i + 2, mid);
                segmentQuadraticBezierCurve(p0, p1, mid);
            }

            segmentQuadraticBezierCurve(in_points.get(i), in_points.get(i + 1), in_points.get(i + 2));
        } else {
            System.err.println("Cannot form a curve with " + in_points.size() + " points");
        }
    }

    public Vector2 getPositionAt(double distance) {
        return points.getInterpolated(distance);
    }

    public Vector2 getHeadingAt(double distance) {
        double epsilon = 0.001;
        return Vector2.mul(Vector2.sub(points.getInterpolated(distance + epsilon), points.getInterpolated(distance)), (1 / epsilon));
    }
}
