package org.imdc.zwavedriver.zwave.messages;

import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import org.imdc.zwavedriver.zwave.messages.framework.Message;
import org.imdc.zwavedriver.zwave.messages.framework.MessageAdaptor;
import org.imdc.zwavedriver.zwave.messages.framework.MessageCommandProcessorAdapter;
import org.imdc.zwavedriver.zwave.messages.framework.MessageType;
import org.imdc.zwavedriver.zwave.messages.framework.RemoveFailedNodeStatus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class RemoveFailedNodeMessage {

    private static final MessageType REQUEST = new MessageType(Messages.REMOVE_FAILED_NODE, Message.Type.REQUEST);

    public static class Processor extends MessageCommandProcessorAdapter {
        @Override
        public Message process(MessageType messageType, byte[] message) throws DecoderException {
            return new Event(message);
        }
    }

    public static class Request extends MessageAdaptor {
        public Request(int nodeId) {
            super(REQUEST, nodeId);
        }

        @Override
        protected void addRequestData(ByteArrayOutputStream result) throws IOException {
            super.addRequestData(result);
            result.write(getNodeId());
        }
    }

    public static class Event extends MessageAdaptor {
        public final RemoveFailedNodeStatus status;

        public Event(byte[] message) throws DecoderException {
            super(message);
            in.read(); // ?
            status = RemoveFailedNodeStatus.from(in.read());
        }

        @Override
        public void update() {
            if (status == RemoveFailedNodeStatus.REMOVED) {
                removeNode(getNodeId());
            }
        }

        @Override
        public String toString() {
            return String.format("{\"RemoveFailedNodeMessage.Response\": {\"status\": %s}}", status.toString());
        }
    }
}
