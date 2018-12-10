package north.autonomous;

import north.NorthRobot;

import java.util.ArrayList;

public class Node implements Executable {
    Path parent;
    public ArrayList<Path> children = new ArrayList<>();
    ArrayList<CommandInstance> commands;

    public Node(Path parent, ArrayList<CommandInstance> commands) {
        this.parent = parent;
        parent.child = this;
        this.commands = commands;
    }

    int currentlyExecuting = 0;

    public void reset() {
        currentlyExecuting = 0;
        commands.forEach(CommandInstance::reset);
        children.forEach(Path::reset);
    }

    @Override
    public Executable execute(NorthRobot robot) {
        if(currentlyExecuting < commands.size()) {
            CommandInstance currCommand = commands.get(currentlyExecuting);
            if(currCommand.invoke()) {
                currentlyExecuting++;
            }
            return this;
        }

        for(Path p : children) {
            if(robot.getConditionValue(p.condition)) {
                return p;
            }
        }

        return null;
    }
}
