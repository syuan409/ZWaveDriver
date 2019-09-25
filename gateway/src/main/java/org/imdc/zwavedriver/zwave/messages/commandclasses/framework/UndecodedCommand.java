package org.imdc.zwavedriver.zwave.messages.commandclasses.framework;

import org.imdc.zwavedriver.zwave.Hex;
import org.imdc.zwavedriver.zwave.messages.commandclasses.CommandClasses;

public class UndecodedCommand extends CommandAdapter {
    private static final CommandCode commandCode = new CommandCode(CommandClasses.UNKNOWN, 0xFF);
    byte[] commandData;

    public UndecodedCommand(byte[] commandData) {
        super(commandCode);
        this.commandData = commandData;
    }

    @Override
    public String toString() {
        return String.format("{\"Unknown.Command\": {\"command\": [%s], \"data\": %s}}", getCommandCode().toString(), Hex.asString(commandData, ""));
    }
}
