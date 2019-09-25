package org.imdc.zwavedriver.zwave.messages;

import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.Command;
import org.imdc.zwavedriver.zwave.messages.framework.*;
import org.imdc.zwavedriver.zwave.messages.framework.Message;
import org.imdc.zwavedriver.zwave.messages.framework.MessageType;

public class NoOperationMessage {

    private static final MessageType REQUEST = new MessageType(Messages.NO_OPERATION, Message.Type.REQUEST);

    public static class Request extends SendDataMessage.Request {
        public Request(int nodeId, Command command) {
            this(nodeId, command, SendDataMessage.TRANSMIT_OPTIONS_ALL);
        }

        public Request(int nodeId, Command command, int transmitOptions) {
            super(REQUEST, nodeId, command, transmitOptions);
        }
    }
}
