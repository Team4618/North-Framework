package north.autonomous;

import edu.wpi.first.wpilibj.Timer;

public class CommandState<T> {
    public double startTime;
    public double elapsedTime;
    public boolean init = true;
    public T data;

    public CommandState() {
        startTime = Timer.getFPGATimestamp();
    }

    public void update() {
        elapsedTime = Timer.getFPGATimestamp() - startTime;
    }
}
