package org.imdc.zwavedriver.zwave.messages;

import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import org.imdc.zwavedriver.zwave.messages.framework.Message;
import org.imdc.zwavedriver.zwave.messages.framework.MessageAdaptor;
import org.imdc.zwavedriver.zwave.messages.framework.MessageCommandProcessorAdapter;
import org.imdc.zwavedriver.zwave.messages.framework.MessageType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AssignReturnRoute {

    private static final MessageType REQUEST = new MessageType(Messages.ASSIGN_RETURN_ROUTE, Message.Type.REQUEST);

    public static class Processor extends MessageCommandProcessorAdapter {
        @Override
        public Message process(MessageType messageType, byte[] message) throws DecoderException {
            return new Event(message);
        }
    }

    public static class Request extends MessageAdaptor {
        private byte toNodeId;

        public Request(int fromNodeId, int toNodeId) {
            super(REQUEST, fromNodeId);
            this.toNodeId = (byte) toNodeId;
        }

        @Override
        protected void addRequestData(ByteArrayOutputStream result) throws IOException {
            super.addRequestData(result);
            result.write(getNodeId());
            result.write(toNodeId);
        }
    }

    public static class Event extends MessageAdaptor {
        public final boolean success;

        public Event(byte[] message) throws DecoderException {
            super(message);
            in.read(); // ?
            success = in.read() == 0;
        }

        @Override
        public String toString() {
            return String.format("{\"AssignReturnRoute.Response\": {\"success\": %b}}", success);
        }
    }
}
