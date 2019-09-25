package org.imdc.zwavedriver.zwave.messages.commandclasses.framework;

import org.imdc.zwavedriver.gateway.ZWavePath;
import org.imdc.zwavedriver.zwave.messages.commandclasses.CommandClasses;

public interface Command {
    CommandCode getCommandCode();

    byte[] encode();

    CommandClasses getCommandClass();

    boolean process();

    void setEncapsulatedCommand(Command encapsulatedCommand);

    Command getEncapsulatedCommand();

    void sent(byte nodeId);

    void update(ZWavePath path, int version, boolean secure);

    void notificationUpdate(ZWavePath path);
}
