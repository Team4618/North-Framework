package north.autonomous;

import north.IDriveAndNavigation.PathProgress;
import north.NorthRobot;
import north.curves.SegmentedCurve;
import north.util.InterpolatingMap;
import north.util.Vector2;

import java.util.ArrayList;

public class Path implements Executable {
    public static class DiscreteEvent {
        double distance;
        CommandInstance command;
    }

    public static class ContinuousEvent {
        Command command;
        InterpolatingMap<Double> valueAt = new InterpolatingMap<>(InterpolatingMap::doubleLerp);

        void update(double distance) {
            double value = valueAt.getInterpolated(distance);
            command.invoke(null, new double[]{ value });
        }
    }

    Node parent;
    Node child;

    String condition;
    SegmentedCurve path;

    ArrayList<DiscreteEvent> discreteEvents;
    ArrayList<ContinuousEvent> continuousEvents;

    Object turnData;
    Object pathData;

    public Path(NorthRobot robot,
                Node parent, ArrayList<Vector2> points,
                ArrayList<DiscreteEvent> discreteEvents, ArrayList<ContinuousEvent> continuousEvents)
    {
        this.parent = parent;
        parent.children.add(this);
        this.path = new SegmentedCurve(points);
        this.discreteEvents = discreteEvents;
        this.continuousEvents = continuousEvents;

        turnData = robot.driveAndNav.calculateTurn(path.getHeadingAt(0).angle());
        pathData = robot.driveAndNav.calculatePath(path);
    }

    boolean doneTurn = false;
    ArrayList<DiscreteEvent> discreteEventQueue = new ArrayList<>();
    CommandState turnCommand;
    CommandState pathCommand;

    public void reset() {
        doneTurn = false;
        discreteEventQueue.clear();
        discreteEventQueue.addAll(discreteEvents);
        turnCommand = null;
        pathCommand = null;

        child.reset();
    }

    @Override
    public Executable execute(NorthRobot robot) {
        if(doneTurn) {
            if(pathCommand == null) {
                pathCommand = new CommandState();
                pathCommand.data = pathData;
            }

            pathCommand.update();
            PathProgress progress = robot.driveAndNav.drivePath(pathCommand, path);
            pathCommand.init = false;

            for (DiscreteEvent e : discreteEventQueue) {
                if (progress.distance > e.distance) {
                    e.command.invoke();
                    discreteEventQueue.remove(e);
                }
            }

            for (ContinuousEvent e : continuousEvents) {
                e.update(progress.distance);
            }

            return progress.done ? child : this;
        } else {
            if(turnCommand == null) {
                turnCommand = new CommandState();
                turnCommand.data = turnData;
            }

            turnCommand.update();
            doneTurn = robot.driveAndNav.turnToAngle(turnCommand, path.getHeadingAt(0).angle());
            turnCommand.init = false;

            return this;
        }
    }
}
