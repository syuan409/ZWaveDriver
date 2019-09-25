package org.imdc.zwavedriver.zwave.messages.framework;

public class DecoderException extends Exception {
    public DecoderException(String message) {
        super(message);
    }

    public DecoderException(String message, Throwable ex) {
        super(message, ex);
    }

    public static void assertTrue(boolean value, String message) throws DecoderException {
        if (!value) {
            throw new DecoderException(message);
        }
    }
}
