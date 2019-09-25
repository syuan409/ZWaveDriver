package org.imdc.zwavedriver.zwave.messages.commandclasses.framework;

import org.imdc.zwavedriver.zwave.Hex;
import org.imdc.zwavedriver.zwave.messages.commandclasses.CommandClasses;

public class CommandCode {

    private final CommandClasses commandClass;
    private final byte command;

    public CommandCode(CommandClasses commandClass, int command) {
        this.commandClass = commandClass;
        this.command = (byte) command;
    }

    public CommandClasses getCommandClass() {
        return commandClass;
    }

    public byte getCommand() {
        return command;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CommandCode that = (CommandCode) o;

        if (command != that.command) return false;
        return commandClass == that.commandClass;

    }

    @Override
    public int hashCode() {
        int result = commandClass != null ? commandClass.hashCode() : 0;
        result = 31 * result + command;
        return result;
    }

    @Override
    public String toString() {
        return String.format("{\"Command.Code\":{\"class\": %s, \"command\": %s}}", commandClass.toString(), Hex.asString(command));
    }
}
