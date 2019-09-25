package org.imdc.zwavedriver.zwave.messages;

import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import org.imdc.zwavedriver.zwave.messages.framework.Message;
import org.imdc.zwavedriver.zwave.messages.framework.MessageAdaptor;
import org.imdc.zwavedriver.zwave.messages.framework.MessageCommandProcessorAdapter;
import org.imdc.zwavedriver.zwave.messages.framework.MessageType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class DeleteAllReturnRoutes {

    private static final MessageType REQUEST = new MessageType(Messages.DELETE_RETURN_ROUTE, Message.Type.REQUEST);

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
        public final boolean success;

        public Event(byte[] message) throws DecoderException {
            super(message);
            in.read(); // ?
            success = in.read() == 0;
        }

        @Override
        public String toString() {
            return String.format("{\"DeleteAllReturnRoutes.Response\": {\"success\": %b}}", success);
        }
    }
}
