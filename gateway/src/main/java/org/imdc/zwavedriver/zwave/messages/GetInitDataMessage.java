package org.imdc.zwavedriver.zwave.messages;

import org.imdc.zwavedriver.gateway.ZWavePath;
import org.imdc.zwavedriver.zwave.ByteUtilities;
import org.imdc.zwavedriver.zwave.Hex;
import org.imdc.zwavedriver.zwave.messages.framework.ControllerMode;
import org.imdc.zwavedriver.zwave.messages.framework.ControllerType;
import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import org.imdc.zwavedriver.zwave.messages.framework.Message;
import org.imdc.zwavedriver.zwave.messages.framework.MessageAdaptor;
import org.imdc.zwavedriver.zwave.messages.framework.MessageCommandProcessorAdapter;
import org.imdc.zwavedriver.zwave.messages.framework.MessageType;

import java.util.List;

public class GetInitDataMessage {

    private static final MessageType REQUEST = new MessageType(Messages.GET_INIT_DATA, Message.Type.REQUEST);

    public static class Processor extends MessageCommandProcessorAdapter {
        @Override
        public Message process(MessageType messageType, byte[] message) throws DecoderException {
            return new Response(message);
        }
    }

    public static class Request extends MessageAdaptor {
        public Request() {
            super(REQUEST);
        }
    }

    public static class Response extends MessageAdaptor {
        public final ControllerMode mode;
        public final ControllerType type;
        public final List<Byte> nodes;

        public Response(byte[] message) throws DecoderException {
            super(message);

            byte unknown = (byte) in.read();
            byte controller = (byte) in.read();
            mode = (controller & 0x01) != 0 ? ControllerMode.SLAVE : ControllerMode.CONTROLLER;
            type = (controller & 0x04) != 0 ? ControllerType.SECONDARY : ControllerType.PRIMARY;
            if (in.read() != ByteUtilities.NUMBER_OF_NODE_BYTES) {
                throw new DecoderException("Wrong number of node bytes");
            }
            this.nodes = ByteUtilities.getNodesFromBitString(in);
        }

        @Override
        public void update() {
            ZWavePath path = getPath();

            updateTag(path.tag("ControllerMode"), mode.name);
            updateTag(path.tag("ControllerType"), type.name);

            for (byte nodeId : nodes) {
                addNode(nodeId);
            }
        }

        @Override
        public String toString() {
            return String.format("{\"GetInitDataMessage.Response\":{\"mode\":\"%s\", \"type\":\"%s\", \"nodes\":[%s]}}", mode.toString(), type.toString(), Hex.asString(nodes));
        }
    }

}
