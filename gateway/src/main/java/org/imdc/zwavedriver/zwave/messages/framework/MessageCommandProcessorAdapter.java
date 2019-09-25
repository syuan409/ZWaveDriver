package org.imdc.zwavedriver.zwave.messages.framework;

import org.imdc.zwavedriver.gateway.ZWaveWrapperFunctions;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.MultiCommandProcessor;

public abstract class MessageCommandProcessorAdapter extends ZWaveWrapperFunctions implements MessageCommandProcessor {
    private MultiCommandProcessor commandProcessor;

    public MessageCommandProcessorAdapter setMultiCommandProcessor(MultiCommandProcessor commandProcessor) {
        this.commandProcessor = commandProcessor;
        return this;
    }

    public MultiCommandProcessor getCommandProcessor() {
        return commandProcessor;
    }
}
