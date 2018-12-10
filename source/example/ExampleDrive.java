package example;

import north.IDriveAndNavigation;
import north.RobotState;
import north.Subsystem;
import north.annotations.Command;
import north.autonomous.CommandState;
import north.curves.SegmentedCurve;
import north.curves.SimpleCurveFollower;

public class ExampleDrive extends Subsystem implements IDriveAndNavigation<Object, SimpleCurveFollower.Profile> {

    @Override
    public void init() {

    }

    @Override
    public void postState() {

    }

    public String name() { return ""; }

    @Override
    public void setState(RobotState state) {

    }

    @Override
    public RobotState getState() {
        return null;
    }

    @Override
    public boolean turnToAngle(CommandState<Object> state, double angle) {
        return false;
    }

    @Override
    public SimpleCurveFollower.Profile calculatePath(SegmentedCurve path) {
        //TODO: other parameters
        return SimpleCurveFollower.buildProfile(path, 0, 0, 0, false, 0);
    }

    @Override
    public PathProgress drivePath(CommandState<SimpleCurveFollower.Profile> state, SegmentedCurve path) {
        return SimpleCurveFollower.follow(state, () -> {}, t -> {});
    }

    @Command(initializer = "test")
    public void testCommand() {

    }
}
