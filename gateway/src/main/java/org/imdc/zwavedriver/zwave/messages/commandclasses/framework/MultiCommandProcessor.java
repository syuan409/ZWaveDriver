package org.imdc.zwavedriver.zwave.messages.commandclasses.framework;

import org.imdc.zwavedriver.zwave.messages.commandclasses.CommandClasses;
import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import org.imdc.zwavedriver.zwave.messages.framework.MultiMessageProcessor;

import java.util.HashMap;
import java.util.Map;

public class MultiCommandProcessor {

    private MultiMessageProcessor messageProcessor;
    private Map<CommandCode, CommandProcessor> processors = new HashMap();

    public MultiCommandProcessor(MultiMessageProcessor messageProcessor) {
        this.messageProcessor = messageProcessor;

        for (CommandClasses c : CommandClasses.values()) {
            if (c.getProcessor() != null) {
                addCommandProcessor(c.getProcessor().setMultiMessageProcessor(messageProcessor).setMultiCommandProcessor(this));
            }
        }
    }

    public Command process(CommandArgument commandArgument, byte[] commandData) throws DecoderException {
        CommandCode commandCode = CommandAdapter.decodeCommandCode(commandData);
        CommandProcessor processor = processors.get(commandCode);
        if (processor != null) {
            return processor.process(commandCode, commandArgument, commandData);
        }
        return new UndecodedCommand(commandData);
    }

    private void addCommandProcessor(CommandProcessor processor) {
        for (CommandCode commandCode : processor.getCommandCodes()) {
            processors.put(commandCode, processor);
        }
    }
}
