package north;

import north.autonomous.CommandState;
import north.curves.SegmentedCurve;

public interface IDriveAndNavigation<TurnData, PathData> {
    void setState(RobotState state);
    RobotState getState();

    default TurnData calculateTurn(double angle) { return null; }
    boolean turnToAngle(CommandState<TurnData> state, double angle);

    class PathProgress {
        public boolean done;
        public double distance;

        public PathProgress(boolean done, double distance) {
            this.done = done;
            this.distance = distance;
        }
    }

    default PathData calculatePath(SegmentedCurve path) { return null; }
    PathProgress drivePath(CommandState<PathData> state, SegmentedCurve path); //returns distance driven along the path
}
