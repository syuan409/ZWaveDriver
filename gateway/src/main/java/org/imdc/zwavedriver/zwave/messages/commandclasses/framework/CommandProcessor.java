package org.imdc.zwavedriver.zwave.messages.commandclasses.framework;

import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;

public interface CommandProcessor {
    CommandCode[] getCommandCodes();

    Command process(CommandCode commandCode, CommandArgument argument, byte[] commandData) throws DecoderException;
}
