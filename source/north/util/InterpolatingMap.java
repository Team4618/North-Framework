package north.util;

import java.util.TreeMap;

public class InterpolatingMap<V> extends TreeMap<Double, V> {
    public interface Interpolator<V> { V interpolate(V a, double t, V b); }

    Interpolator<V> lerp;

    public InterpolatingMap(Interpolator<V> lerp) {
        this.lerp = lerp;
    }

    public V getInterpolated(double key) {
        V gotval = get(key);
        if (gotval == null) {
            Double topBound = ceilingKey(key);
            Double bottomBound = floorKey(key);

            if (topBound == null && bottomBound == null) {
                return null;
            } else if (topBound == null) {
                return get(bottomBound);
            } else if (bottomBound == null) {
                return get(topBound);
            }

            V topElem = get(topBound);
            V bottomElem = get(bottomBound);
            return lerp.interpolate(bottomElem, topBound - bottomBound, topElem);
        } else {
            return gotval;
        }
    }

    public static double doubleLerp(double a, double t, double b) {
        return a + t * (b - a);
    }
}
