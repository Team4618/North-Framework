package north;

import edu.wpi.first.wpilibj.TimedRobot;
import north.autonomous.Executable;
import north.autonomous.Node;
import north.network.Network;
import north.util.Button;

import java.lang.reflect.Method;
import java.util.HashMap;

public abstract class NorthRobot extends TimedRobot {
    public static String name;
    public static double width;
    public static double length;
    public static IDriveAndNavigation driveAndNav;

    public static volatile Node autoRootNode; //NOTE: set by the network thread
    public static Executable currentlyExecuting;
    public static HashMap<String, Method> logic = new HashMap<>();
    
    public void init(String name, double width, double length, IDriveAndNavigation driveSubsystem, Class logicProvider) {
        NorthRobot.name = name;
        NorthRobot.width = width;
        NorthRobot.length = length;
        NorthRobot.driveAndNav = driveSubsystem;

        if(logicProvider != null) {
            for(Method m : logicProvider.getDeclaredMethods()) {
                if(m.isAnnotationPresent(north.annotations.Logic.class)){
                    if((m.getReturnType() == boolean.class) && (m.getParameterCount() == 0)) {
                        logic.put(m.getName(), m);
                    } else {
                        System.err.println("Logic Provider " + m.getName() + ": incorrect signature");
                    }
                }
            }
        }

        Subsystems.init();
        Network.init();
    }

    public boolean getConditionValue(String condition) {
        if(logic.containsKey(condition)) {
            Method conditionFunc = logic.get(condition);
            try {
                Object res = conditionFunc.invoke(null);
                if(res instanceof Boolean)
                    return (Boolean) res;
            } catch (Exception e) { e.printStackTrace(); }
        } else {
            System.err.println("Condition " + condition + " not found");
        }
        return false;
    }

    public void robotPeriodic() {
        RobotState state = driveAndNav.getState();
        //nav update
        //send state
    }
    /**
    public void robotPeriodic() {
         super.robotPeriodic();
         ...
    }
    */
    
    //------------------------------------------
    public void autonomousInit() {
        if(autoRootNode != null) {
            autoRootNode.reset();
            currentlyExecuting = autoRootNode;
        }
        Subsystems.subsystems.values().forEach(s -> s.periodicEnabled = true);
    }
    /**
    public void autonomousInit() {
         super.autonomousInit();
         ...
    }
    */
    
    public void autonomousPeriodic() {
        currentlyExecuting = currentlyExecuting.execute(this);
        Subsystems.periodic();
    }
    //-------------------------------------------
    
    //-------------------------------------------
    public void teleopInit() {
        Button.resetAll();
        Subsystems.subsystems.values().forEach(s -> s.periodicEnabled = true);
    }
    /**
    public void teleopInit() {
         super.teleopInit();
         ...
    }
    */
    
    public abstract void doTeleop();
    public void teleopPeriodic() {
        Button.tickAll();
        doTeleop();
        Subsystems.periodic();
    }
    //------------------------------------------
    
    //------------------------------------------
    public abstract void disableForTest();
    public void testInit() {
        teleopInit();
        disableForTest();
    }

    public void testPeriodic() {
        teleopPeriodic();
    }
    //-----------------------------------------
}