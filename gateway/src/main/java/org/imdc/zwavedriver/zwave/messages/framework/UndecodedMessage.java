package org.imdc.zwavedriver.zwave.messages.framework;

import org.imdc.zwavedriver.zwave.Hex;

public class UndecodedMessage extends MessageAdaptor {
    private final byte[] messageData;

    public UndecodedMessage(byte[] messageData) throws DecoderException {
        super(messageData);
        this.messageData = messageData;
    }

    @Override
    public String toString() {
        return String.format("{\"UndecodedMessage\": {\"messageId\": [%s], \"data\": %s}}", getMessageType().toString(), Hex.asString(getMessageData(messageData), ""));
    }

    @Override
    public String getMessageId() {
        return "msg-unknown";
    }
}
