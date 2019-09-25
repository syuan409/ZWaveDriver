package org.imdc.zwavedriver.zwave.messages.framework;

import org.imdc.zwavedriver.zwave.messages.Messages;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.MultiCommandProcessor;

import java.util.HashMap;
import java.util.Map;

public class MultiMessageProcessor {

    private Map<MessageType, MessageCommandProcessor> processors = new HashMap();
    private MultiCommandProcessor commandProcessor;

    public MultiMessageProcessor() {
        commandProcessor = new MultiCommandProcessor(this);

        for (Messages m : Messages.values()) {
            for (Message.Type t : Message.Type.values()) {
                addMessageProcessor(m, t, m.getProcessor().setMultiCommandProcessor(commandProcessor));
            }
        }
    }

    public Message process(byte[] messageBytes) throws DecoderException {
        MessageType messageType = MessageAdaptor.decodeMessageId(messageBytes);
        MessageCommandProcessor processor = processors.get(messageType);
        if (processor != null) {
            return processor.process(messageType, messageBytes);
        }
        return new UndecodedMessage(messageBytes);
    }

    private void addMessageProcessor(Messages message, Message.Type type, MessageCommandProcessor processor) {
        processors.put(new MessageType(message, type), processor);
    }
}
