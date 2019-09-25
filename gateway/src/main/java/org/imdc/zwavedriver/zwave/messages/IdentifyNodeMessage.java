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

public class IdentifyNodeMessage {

    private static final MessageType REQUEST = new MessageType(Messages.IDENTIFY_NODE, Message.Type.REQUEST);

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
    }

    public static class Response extends MessageAdaptor {
        public final boolean isListening;
        public final boolean isRouting;
        public final boolean isFrequentlyListening;
        public final byte version;
        public final byte basicDeviceClass;
        public final byte genericDeviceClass;
        public final byte specificDeviceClass;

        public Response(byte[] message) throws DecoderException {
            super(message);

            int byte1 = in.read();
            this.isListening = (byte1 & 0x80) != 0;
            this.isRouting = (byte1 & 0x40) != 0;
            this.version = (byte) ((byte1 & 0x07) + 1);
            int byte2 = in.read();
            this.isFrequentlyListening = (byte2 & 0x60) != 0;
            in.read();
            this.basicDeviceClass = (byte) in.read();
            this.genericDeviceClass = (byte) in.read();
            this.specificDeviceClass = (byte) in.read();
        }

        @Override
        public void update() {
            ZWavePath path = getPath(getNodeId());

            updateTag(path.tag("IsListening"), isListening);
            updateTag(path.tag("IsRouting"), isRouting);
            updateTag(path.tag("IsFrequentListening"), isFrequentlyListening);
            updateTag(path.tag("Version"), version);
            updateTag(path.tag("BasicDeviceClass"), Hex.asString(basicDeviceClass));
            updateTag(path.tag("GenericDeviceClass"), Hex.asString(genericDeviceClass));
            updateTag(path.tag("SpecificDeviceClass"), Hex.asString(specificDeviceClass));
            updateTag(path.tag("NodeType"), getControllerNodeId() == getNodeId() ? "Controller" : "Node");
            updateStatusTag(path, getControllerNodeId() == getNodeId() ? "Connected" : "Unknown");
        }

        @Override
        public String toString() {
            return String.format("{\"IdentifyNodeMessage.Response\":{\"isListening\":%b, \"isRouting\":%b, \"isFrequentlyListening\":%b, \"version\":%d, \"basicDeviceClass\":%s, \"genericDeviceClass\":%s, \"specificDeviceClass\":%s }}",
                    isListening, isRouting, isFrequentlyListening, version, Hex.asString(basicDeviceClass), Hex.asString(genericDeviceClass), Hex.asString(specificDeviceClass));
        }
    }

}
