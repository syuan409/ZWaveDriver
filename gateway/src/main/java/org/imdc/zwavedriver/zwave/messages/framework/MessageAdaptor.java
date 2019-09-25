package org.imdc.zwavedriver.zwave.messages.framework;

import org.imdc.zwavedriver.gateway.ZWavePath;
import org.imdc.zwavedriver.gateway.ZWaveWrapperFunctions;
import org.imdc.zwavedriver.zwave.Hex;
import org.imdc.zwavedriver.zwave.messages.Messages;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

public abstract class MessageAdaptor extends ZWaveWrapperFunctions implements Message {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public static final byte Z_WAVE_REQUEST = 0;
    private Byte nodeId = null;
    private final MessageType messageType;
    protected ByteArrayInputStream in;
    protected int messageLength;
    protected Command command;
    protected Date sentDate;
    protected boolean priority = false;
    protected Status status = Status.UNKNOWN;

    public MessageAdaptor(MessageType messageType) {
        this(messageType, null);
    }

    public MessageAdaptor(MessageType messageType, Integer nodeId) {
        this.messageType = messageType;
        if (nodeId != null) {
            this.nodeId = nodeId.byteValue();
        }
    }

    public MessageAdaptor(byte[] messageData) throws DecoderException {
        messageType = decodeMessageId(messageData);
        messageLength = messageData.length - 2;
        in = new ByteArrayInputStream(messageData);
        in.read();
        in.read();
    }

    public byte[] getMessageData(byte[] messageData) {
        return Arrays.copyOfRange(messageData, 2, messageData.length);
    }

    @Override
    public byte[] encode() {
        try {
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            addRequestData(result);
            return result.toByteArray();
        } catch (IOException e) {
            // This should not happen
            return new byte[0];
        }
    }

    protected void addRequestData(ByteArrayOutputStream result) throws IOException {
        result.write(Z_WAVE_REQUEST);
        result.write(messageType.getMessage().getValue());
    }

    @Override
    public void setNodeId(int nodeId) {
        if (this.nodeId == null) {
            this.nodeId = (byte) nodeId;
        }
    }

    @Override
    public byte getNodeId() {
        return nodeId == null ? (byte) 0xFF : nodeId;
    }

    @Override
    public Command getCommand() {
        return command;
    }

    @Override
    public void setCommand(Command command) {
        this.command = command;
    }

    @Override
    public MessageType getMessageType() {
        return messageType;
    }

    @Override
    public String getMessageId() {
        return String.format("node-%s-msg-%s", Hex.asString(getNodeId()), messageType.getMessage().getHex());
    }

    public void setPriority(boolean priority) {
        this.priority = priority;
    }

    @Override
    public boolean hasPriority() {
        return priority;
    }

    @Override
    public Date getSentDate() {
        return sentDate;
    }

    @Override
    public void sent() {
        this.sentDate = new Date();
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public void failed() {
        logger.info("Message " + getMessageId() + " failed: " + String.format("{\"node\": %s, \"message\": %s}", Hex.asString(getNodeId()), toString()));
        setStatus(Status.FAILED);
    }

    @Override
    public void success() {
        setStatus(Status.SUCCESS);
    }

    public static MessageType decodeMessageId(byte[] message) throws DecoderException {
        if (message == null || message.length < 2) {
            throw new DecoderException("Invalid message buffer");
        }
        return new MessageType(Messages.from(message[1]), message[0] == 0 ? Type.REQUEST : Type.RESPONSE);
    }

    @Override
    public void update() {

    }

    protected void updateStatusTag(ZWavePath path, String status) {
        if (path.getNodeId() != 0) {
            String currentStatus = (String) readTag(path.tag("Status"));
            if (currentStatus != null && status.equals("Unknown")) {
                return;
            }

            if (currentStatus == null || !currentStatus.equals("Failed")) {
                updateTag(path.tag("Status"), status);
            }
        }
    }
}
