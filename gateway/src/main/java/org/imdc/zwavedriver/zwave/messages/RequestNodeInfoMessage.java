package org.imdc.zwavedriver.zwave.messages;

import org.imdc.zwavedriver.gateway.ZWavePath;
import org.imdc.zwavedriver.zwave.Hex;
import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import org.imdc.zwavedriver.zwave.messages.framework.Message;
import org.imdc.zwavedriver.zwave.messages.framework.MessageAdaptor;
import org.imdc.zwavedriver.zwave.messages.framework.MessageCommandProcessorAdapter;
import org.imdc.zwavedriver.zwave.messages.framework.MessageType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Request the controller for information about which command classes a specific node supports.
 * The actual result of the request will be returned as an ApplicationUpdateMessage-message.
 */
public class RequestNodeInfoMessage {

    private static final MessageType REQUEST = new MessageType(Messages.REQUEST_NODE_INFO, Message.Type.REQUEST);

    public static class Processor extends MessageCommandProcessorAdapter {
        @Override
        public Message process(MessageType messageType, byte[] message) throws DecoderException {
            return new Response(message);
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

        @Override
        public void failed() {
            super.failed();
            ZWavePath path = getPath(getNodeId());
            updateStatusTag(path, "Disconnected");
        }

        @Override
        public String getMessageId() {
            return String.format("node-%s-msg-node-info", Hex.asString(getNodeId()));
        }
    }

    public static class Response extends MessageAdaptor {
        public final byte nodeId;

        public Response(byte[] message) throws DecoderException {
            super(message);
            nodeId = (byte) in.read();
            setNodeId(nodeId);
        }

        @Override
        public String toString() {
            return String.format("{\"RequestNodeInfoMessage.Response\": {\"node\": %s}}", Hex.asString(nodeId));
        }
    }
}
