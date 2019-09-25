package org.imdc.zwavedriver.zwave.messages;

import org.imdc.zwavedriver.gateway.ZWavePath;
import org.imdc.zwavedriver.zwave.ByteUtilities;
import org.imdc.zwavedriver.zwave.Hex;
import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import org.imdc.zwavedriver.zwave.messages.framework.Message;
import org.imdc.zwavedriver.zwave.messages.framework.MessageAdaptor;
import org.imdc.zwavedriver.zwave.messages.framework.MessageCommandProcessorAdapter;
import org.imdc.zwavedriver.zwave.messages.framework.MessageType;

import java.util.Arrays;
import java.util.Date;

public class ApplicationUpdateMessage {

    private static final MessageType REQUEST = new MessageType(Messages.APPLICATION_UPDATE, Message.Type.REQUEST);
    public static final byte NODE_INFO_RECEIVED = (byte) 0x84;
    public static final byte NODE_INFO_REQ_DONE = (byte) 0x82;
    public static final byte NODE_INFO_REQ_FAILED = (byte) 0x81;
    public static final byte ROUTING_PENDING = (byte) 0x80;
    public static final byte NEW_ID_ASSIGNED = (byte) 0x40;
    public static final byte DELETE_DONE = (byte) 0x20;
    public static final byte SUC_ID = (byte) 0x10;
    public static final byte COMMAND_CLASS_MARK = (byte) 0xEF;

    public static class Processor extends MessageCommandProcessorAdapter {
        @Override
        public Message process(MessageType messageType, byte[] message) throws DecoderException {
            return new Event(message);
        }
    }

    public static class Event extends MessageAdaptor {
        public byte nodeId;
        public final byte updateState;
        public final byte basicDeviceClass;
        public final byte genericDeviceClass;
        public final byte specificDeviceClass;
        public final byte[] supportedCommandClasses;
        public final byte[] controlledCommandClasses;

        public Event(byte[] message) throws DecoderException {
            super(message);

            updateState = (byte) in.read();
            if (updateState == NODE_INFO_RECEIVED) {
                nodeId = (byte) in.read();
                setNodeId(nodeId);
                in.read();
                basicDeviceClass = (byte) in.read();
                genericDeviceClass = (byte) in.read();
                specificDeviceClass = (byte) in.read();
                int numberOfCommandClasses = messageLength - 5;
                byte[] allCommandClasses = new byte[numberOfCommandClasses];
                in.read(allCommandClasses, 0, numberOfCommandClasses);
                int separatorPosition = ByteUtilities.find(allCommandClasses, COMMAND_CLASS_MARK);
                supportedCommandClasses = Arrays.copyOfRange(allCommandClasses, 0, separatorPosition);
                controlledCommandClasses = Arrays.copyOfRange(allCommandClasses, separatorPosition + (separatorPosition == numberOfCommandClasses ? 0 : 1), numberOfCommandClasses);
            } else {
                nodeId = 0;
                basicDeviceClass = 0;
                genericDeviceClass = 0;
                specificDeviceClass = 0;
                supportedCommandClasses = new byte[0];
                controlledCommandClasses = new byte[0];
            }
        }

        @Override
        public void update() {
            ZWavePath path = getPath(getNodeId());

            if(isInitNode(nodeId)) {
                if (updateState == NODE_INFO_RECEIVED) {
                    updateStatusTag(path, "Connected");
                    addCommandClasses(nodeId, supportedCommandClasses, false);
                } else {
                    updateStatusTag(path, "Disconnected");
                }
            } else {
                String refreshCommands = (String) readTag(path.tag("ApplicationUpdateRefreshCommands"));
                refreshCommands(path, refreshCommands);
            }

            updateTag(path.tag("ApplicationUpdate"), new Date());
        }

        @Override
        public String getMessageId() {
            return String.format("node-%s-msg-node-info", Hex.asString(nodeId));
        }

        @Override
        public String toString() {
            return String.format("{\"ApplicationUpdateMessage.Event\": {\"updateState\": %s, \"node\": %s, \"supportedClasses\": [%s], \"controlledClasses\": [%s]}}",
                    Hex.asString(updateState), Hex.asString(nodeId), Hex.asString(supportedCommandClasses), Hex.asString(controlledCommandClasses));
        }
    }
}
