package org.imdc.zwavedriver.zwave.messages;

import org.imdc.zwavedriver.zwave.Hex;
import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import org.imdc.zwavedriver.zwave.messages.framework.ExclusionMode;
import org.imdc.zwavedriver.zwave.messages.framework.Message;
import org.imdc.zwavedriver.zwave.messages.framework.MessageAdaptor;
import org.imdc.zwavedriver.zwave.messages.framework.MessageCommandProcessorAdapter;
import org.imdc.zwavedriver.zwave.messages.framework.MessageType;
import org.imdc.zwavedriver.zwave.messages.framework.RemoveStatus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class RemoveNodeMessage {

    private static final MessageType REQUEST = new MessageType(Messages.REMOVE_NODE, Message.Type.REQUEST);

    public static class Processor extends MessageCommandProcessorAdapter {
        @Override
        public Message process(MessageType messageType, byte[] message) throws DecoderException {
            return new Event(message);
        }
    }

    public static class Request extends MessageAdaptor {
        private ExclusionMode exclusionMode;

        public Request(ExclusionMode exclusionMode) {
            super(REQUEST);
            this.exclusionMode = exclusionMode;
        }

        @Override
        protected void addRequestData(ByteArrayOutputStream result) throws IOException {
            super.addRequestData(result);
            result.write(exclusionMode.value);
            result.write(0xFF); // ??
        }
    }

    public static class Event extends MessageAdaptor {
        public final RemoveStatus status;
        public final byte nodeId;

        public Event(byte[] message) throws DecoderException {
            super(message);

            in.read(); // ??
            status = RemoveStatus.from(in.read());
            nodeId = (byte) in.read();
        }

        @Override
        public void update() {
            if (status == RemoveStatus.DONE) {
                removeNode(nodeId);
            }
        }

        @Override
        public String toString() {
            return String.format("{\"RemoveNodeMessage.Event\": {\"status\": \"%s\", \"node\": %s}}", status.toString(), Hex.asString(nodeId));
        }
    }
}
