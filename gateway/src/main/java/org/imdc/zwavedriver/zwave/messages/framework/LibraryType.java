package org.imdc.zwavedriver.zwave.messages.framework;

public enum LibraryType {
    UNKNOWN(0x00, "Unknown"),
    STATIC_CONTROLLER(0x01, "Static Controller"),
    CONTROLLER(0x02, "Controller"),
    ENHANCED_SLAVE(0x03, "Enhanced Slave"),
    SLAVE(0x04, "Slave"),
    INSTALLER(0x05, "Installer"),
    ROUTING_SLAVE(0x06, "Routing Slave"),
    BRIDGE_CONTROLLER(0x07, "Bridge Controller"),
    DEVICE_UNDER_TEST(0x08, "Device Under Test");

    public final byte value;
    public final String name;

    LibraryType(int value, String name) {
        this.value = (byte) value;
        this.name = name;
    }

    public static LibraryType from(int value) throws DecoderException {
        for (LibraryType status : LibraryType.values()) {
            if (status.value == (byte) value) {
                return status;
            }
        }
        throw new DecoderException("Unknown library type");
    }

    @Override
    public String toString() {
        return String.format("{\"Library.Type\":{\"name\": %s}}", name);
    }
}
