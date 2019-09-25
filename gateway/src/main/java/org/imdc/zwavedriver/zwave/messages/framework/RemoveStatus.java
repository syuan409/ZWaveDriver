package org.imdc.zwavedriver.zwave.messages.framework;

public enum RemoveStatus {
    LEARN_READY(0x01, "Learn Ready"),
    NODE_FOUND(0x02, "Node Found"),
    REMOVING_SLAVE(0x03, "Removing Slave"),
    REMOVING_CONTROLLER(0x04, "Removing Controller"),
    PROTOCOL_DONE(0x05, "Protocol Done"),
    DONE(0x06, "Done"),
    FAILED(0x07, "Failed");

    public final byte value;
    public final String name;

    RemoveStatus(int value, String name) {
        this.value = (byte) value;
        this.name = name;
    }

    public static RemoveStatus from(int value) throws DecoderException {
        for (RemoveStatus status : RemoveStatus.values()) {
            if (status.value == (byte) value) {
                return status;
            }
        }
        throw new DecoderException("Unknown value");
    }

    @Override
    public String toString() {
        return String.format("{\"Remove.Status\":{\"name\": %s}}", name);
    }
}
