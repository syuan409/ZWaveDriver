package org.imdc.zwavedriver.zwave.messages.framework;

public enum ExclusionMode {
    ANY(0x01, "Any"),
    CONTROLLER(0x02, "Controller"),
    SLAVE(0x03, "Slave"),
    EXISTING(0x04, "Existing"),
    STOP(0x05, "Stop"),
    STOP_FAILED(0x06, "Stop Failed");

    public byte value;
    public final String name;

    ExclusionMode(int value, String name) {
        this.value = (byte) value;
        this.name = name;
    }

    @Override
    public String toString() {
        return String.format("{\"Exclusion.Mode\":{\"name\": %s}}", name);
    }
}