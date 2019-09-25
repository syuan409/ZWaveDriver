package org.imdc.zwavedriver.zwave.messages;

import org.imdc.zwavedriver.gateway.ZWavePath;
import org.imdc.zwavedriver.zwave.ByteUtilities;
import org.imdc.zwavedriver.zwave.Hex;
import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import org.imdc.zwavedriver.zwave.messages.framework.Message;
import org.imdc.zwavedriver.zwave.messages.framework.MessageAdaptor;
import org.imdc.zwavedriver.zwave.messages.framework.MessageCommandProcessorAdapter;
import org.imdc.zwavedriver.zwave.messages.framework.MessageType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class GetRoutingInfoMessage {

    private static final MessageType REQUEST = new MessageType(Messages.GET_ROUTING_INFO, Message.Type.REQUEST);
    private static final byte FUNCTION_ID = 0x03;

    public static class Processor extends MessageCommandProcessorAdapter {
        @Override
        public Message process(MessageType messageType, byte[] message) throws DecoderException {
            return new Response(message);
        }
    }

    public static class Request extends MessageAdaptor {
        final public byte nodeId;
        private final boolean includeBadNodes;
        private final boolean includeNonRepeaters;

        public Request(int nodeId) {
            this(nodeId, false, false);
        }

        public Request(int nodeId, boolean includeBadNodes, boolean includeNonRepeaters) {
            super(REQUEST, nodeId);
            this.nodeId = (byte) nodeId;
            this.includeBadNodes = includeBadNodes;
            this.includeNonRepeaters = includeNonRepeaters;
        }

        @Override
        protected void addRequestData(ByteArrayOutputStream result) throws IOException {
            super.addRequestData(result);
            result.write(nodeId);
            result.write(includeBadNodes ? 1 : 0);
            result.write(includeNonRepeaters ? 1 : 0);
            result.write(FUNCTION_ID);
        }
    }

    public static class Response extends MessageAdaptor {
        public final List<Byte> nodes;

        public Response(byte[] message) throws DecoderException {
            super(message);
            this.nodes = ByteUtilities.getNodesFromBitString(in);
        }

        @Override
        public void update() {
            ZWavePath path = getPath(getNodeId());
            updateTag(path.tag("Neighbors"), Hex.asString(nodes));

            if(isHealNode(getNodeId())){
                healNetworkNodeFix(getNodeId());
            }
        }

        @Override
        public String toString() {
            return String.format("{\"GetRoutingInfoMessage.Response\":{\"nodes\":[%s]}}", Hex.asString(nodes));
        }
    }

}
