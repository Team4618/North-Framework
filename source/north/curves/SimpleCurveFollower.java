package north.curves;

import north.IDriveAndNavigation.PathProgress;
import north.autonomous.CommandState;
import north.util.InterpolatingMap;
import north.util.Vector2;

import java.util.function.Consumer;

public class SimpleCurveFollower {
    public static class Trajectory {
        public double t, vl, pl, vr, pr, angle;
        public Trajectory (double t, double vell, double posl, double velr, double posr, double angle) {
            this.t = t;
            this.vl = vell;
            this.pl = posl;
            this.vr = velr;
            this.pr = posr;
            this.angle = angle;
        }
    }

    public static class Profile {
        public Trajectory[] profile;
        public double dt;

        public Profile(Trajectory[] profile, double dt) {
            this.profile = profile;
            this.dt = dt;
        }

        public Trajectory getTrajectoryAt(double t) {
            int i = (int) (t / dt);
            i = Math.max(0, Math.min(i, profile.length));
            return profile[i];
        }

        public double length() {
            return dt * profile.length;
        }
    }

    public static double profileTime(double tAccel, double tDeccel, double distance, double speed) {
        return (distance / speed) + 0.5 * (tAccel + tDeccel);
    }

    public static double trapazoidalProfile(double t, double tAccel, double tDeccel, double distance, double speed) {
        double tTotal = profileTime(tAccel, tDeccel, distance, speed);
        double deccelTime = tTotal - tDeccel;

        if (t >= deccelTime) {
            return InterpolatingMap.doubleLerp(speed, (t - deccelTime) / tDeccel, 0);
        } else if (t <= tAccel) {
            return InterpolatingMap.doubleLerp(0, t / tAccel, speed);
        }

        return speed;
    }

    public static Profile buildProfile(SegmentedCurve path, double tAccel, double tDeccel, double nominalSpeed, boolean backwards, double wheelbase) {
        double dt = 0.01;

        double pl = 0;
        double pr = 0;
        double distance = 0;

        double tTotal = profileTime(tAccel, tDeccel, path.length, nominalSpeed);
        Vector2 lp = null;
        System.out.println(tTotal / dt + " slices");
        int slices = (int)Math.ceil(tTotal / dt);
        Trajectory[] samples = new Trajectory[slices];

        for (int i = 0; i < slices; i++) {
            double t = dt * i;
            double speed = trapazoidalProfile(t, tAccel, tDeccel, path.length, nominalSpeed);
            Vector2 p = path.getPositionAt(distance);
            Vector2 pn = path.getPositionAt(distance + speed * dt);
            Vector2 h = new Vector2((pn.x - p.x) / dt, (pn.y - p.y) / dt);

            double dtheta = 0;
            if (lp != null) {
                Vector2 lasth = new Vector2((p.x - lp.x) / dt, (p.y - lp.y) / dt);
                double htheta = h.angle();
                double ltheta = lasth.angle();
                dtheta = (htheta - ltheta) / dt;

                double dtheta_ppi = (htheta - (ltheta + 2 * Math.PI)) / dt;
                if(Math.abs(dtheta_ppi) < Math.abs(dtheta))
                    dtheta = dtheta_ppi;

                double dtheta_npi = (htheta - (ltheta - 2 * Math.PI)) / dt;
                if(Math.abs(dtheta_npi) < Math.abs(dtheta))
                    dtheta = dtheta_npi;
            }

            System.out.println("s=" + h.length() + " a=" + dtheta + " @ t%=" + (t / tTotal) + " d%=" + (distance / path.length));

            double vl = (0.5) * (2 * (backwards ? -1 : 1) * h.length() - wheelbase * dtheta);
            pl += vl * dt;
            double vr = (0.5) * (2 * (backwards ? -1 : 1) * h.length() + wheelbase * dtheta);
            pr += vr * dt;
            distance += speed * dt;
            if (speed != 0)
                lp = p;
            samples[i] = new Trajectory(t, vl, pl, vr, pr, Math.toDegrees(h.angle()));
        }

        return new Profile(samples, dt);
    }

    public static PathProgress follow(CommandState<Profile> state, Runnable zeroEncoders, Consumer<Trajectory> setPositionSetpoints) {
        if(state.init) {
            zeroEncoders.run();
        }

        Profile profile = state.data;
        boolean running = state.elapsedTime < profile.length();

        if(running) {
            Trajectory currTraj = profile.getTrajectoryAt(state.elapsedTime);
            setPositionSetpoints.accept(currTraj);
        }

        //TODO: distance
        return new PathProgress(running, 0/*profile.dt * */);
    }
}
