package north;

import north.autonomous.Command;
import north.autonomous.CommandState;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class Subsystem {

    public abstract void init();
    public void updateParameters() { }
    public void periodic() { }
    public abstract void postState();
    public abstract String name();

    public boolean periodicEnabled = true;
    public HashMap<String, Field> parameters = new HashMap<>();
    public HashMap<String, Command> commands = new HashMap<>();

    public Subsystem() { Subsystems.subsystems.put(name(), this); }
    
    public void initSystem() {
        for(Field param_variable : this.getClass().getDeclaredFields()) {
            if(param_variable.isAnnotationPresent(north.annotations.Parameter.class)) {
                if((param_variable.getType() == double.class) || (param_variable.getType() == double[].class)) {
                    parameters.put(param_variable.getName(), param_variable);
                } else {
                    System.err.println(name() + ":" + param_variable.getName() + ": Invalid type " + param_variable.getType());
                }
            }
        }

        //TODO: load parameters from local file

        for(Method function : this.getClass().getDeclaredMethods()) {
            if(function.isAnnotationPresent(north.annotations.Command.class)) {
                north.annotations.Command annotation = function.getAnnotation(north.annotations.Command.class);
                ArrayList<String> params = new ArrayList<>();
                boolean error = false;

                Parameter[] parameters = function.getParameters();
                for(int i = 0; i < parameters.length; i++) {
                    Parameter param = parameters[i];
                    if((i == 0) && (param.getType() == CommandState.class)) {

                    } else if(param.getType() == double.class) {
                        params.add(param.getName());
                    } else {
                        System.err.println(name() + ":" + function.getName() + ": Invalid type " + param.getType() + ":" + param.getName());
                        error = true;
                    }
                }

                if((function.getReturnType() != boolean.class) && (function.getReturnType() != void.class)) {
                    System.err.println(name() + ":" + function.getName() + ": Invalid return type " + function.getReturnType() + ", must be boolean or void");
                    error = true;
                }

                Method initializer = null;
                if(annotation.initializer().length() > 0) {
                    for(Method m : this.getClass().getDeclaredMethods()) {
                        if(m.getName().equals(annotation.initializer()))
                            initializer = m;
                    }

                    if(initializer == null) {
                        System.err.println("Initializer " + annotation.initializer() + " for command " + this.name() + ":" + function.getName() + " not found");
                        error = true;
                    }
                }

                if(!error)
                    commands.put(function.getName(), new Command(this, function, params.toArray(new String[0]), initializer));
            }
        }

        init();
    }

    public void updateParameterValue(String name, double value) {
        if(parameters.containsKey(name)) {
            Field p = parameters.get(name);
            //TODO
            updateParameters();
        }
    }

    /*
    public void PostState(String name, Units unit, double value) {
       
    }
    */
}