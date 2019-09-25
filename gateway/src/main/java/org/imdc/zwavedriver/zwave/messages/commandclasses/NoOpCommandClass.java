package org.imdc.zwavedriver.zwave.messages.commandclasses;

import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.*;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandAdapter;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandClass;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandCode;

public class NoOpCommandClass implements CommandClass {

    private static final CommandCode SET = new CommandCode(CommandClasses.NOOP, 0x00);

    public static class Set extends CommandAdapter {
        public Set() {
            super(SET);
        }
    }
}
