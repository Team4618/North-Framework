package example;

import north.NorthRobot;

public class ExampleRobot extends NorthRobot {

    public static ExampleDrive drive = new ExampleDrive();

    public void robotInit() {
        super.init("testbot", 1, 1.5, drive, null);
    }

    @Override
    public void doTeleop() {

    }

    @Override
    public void disableForTest() {

    }
}