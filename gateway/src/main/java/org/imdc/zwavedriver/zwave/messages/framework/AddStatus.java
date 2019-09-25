package org.imdc.zwavedriver.zwave.messages.framework;

public enum AddStatus {
    LEARN_READY(0x01, "Learn Ready"),
    NODE_FOUND(0x02, "Node Found"),
    ADDING_SLAVE(0x03, "Adding Slave"),
    ADDING_CONTROLLER(0x04, "Adding Controller"),
    PROTOCOL_DONE(0x05, "Protocol Done"),
    DONE(0x06, "Done"),
    FAILED(0x07, "Failed");

    public final byte value;
    public final String name;

    AddStatus(int value, String name) {
        this.value = (byte) value;
        this.name = name;
    }

    public static AddStatus from(int value) throws DecoderException {
        for (AddStatus status : AddStatus.values()) {
            if (status.value == (byte) value) {
                return status;
            }
        }
        throw new DecoderException("Unknown value");
    }

    @Override
    public String toString() {
        return String.format("{\"Add.Status\":{\"name\": %s}}", name);
    }
}
