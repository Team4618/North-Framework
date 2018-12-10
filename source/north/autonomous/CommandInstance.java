package north.autonomous;

public class CommandInstance {
    Command command;
    double[] parameters;
    Object data;

    CommandState state;

    public CommandInstance(Command command, double[] parameters) {
        this.command = command;
        this.parameters = parameters;
        this.data = command.callInitializer(parameters);
    }

    public void reset() {
        state = null;
    }

    public boolean invoke() {
        if(state == null) {
            state = new CommandState();
            state.data = data;
        }

        state.update();
        boolean result = command.invoke(state, parameters);
        state.init = false;

        return result;
    }
}
