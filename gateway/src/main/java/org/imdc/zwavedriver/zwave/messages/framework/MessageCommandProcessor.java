package org.imdc.zwavedriver.zwave.messages.framework;

public interface MessageCommandProcessor {
    Message process(MessageType messageType, byte[] message) throws DecoderException;
}
