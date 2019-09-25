package org.imdc.zwavedriver.zwave.messages.commandclasses.objects;

import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;

public enum LibraryType {
    UNDEFINED(0x00, "Undefined"),
    STATIC_CONTROLLER(0x01, "Static Controller"),
    CONTROLLER(0x02, "Controller"),
    ENHANCED_SLAVE(0x03, "Enhanced Slave"),
    SLAVE(0x04, "Slave"),
    INSTALLER(0x05, "Installer"),
    ROUTING_SLAVE(0x06, "Routing Slave"),
    BRIDGE_CONTROLLER(0x07, "Bridge Controller"),
    DEVICE_UNDER_TEST(0x08, "Device Under Test"),
    AV_REMOTE(0x0A, "AV Remote"),
    AV_DEVICE(0x0B, "AV Device");

    public final byte value;
    public final String name;

    LibraryType(int value, String name) {
        this.value = (byte) value;
        this.name = name;
    }

    public static LibraryType from(int type) throws DecoderException {
        for (LibraryType l : values()) {
            if (l.value == (byte) type) {
                return l;
            }
        }
        throw new DecoderException("Unknown Library Type");
    }

    @Override
    public String toString() {
        return String.format("{\"Library.Type\":{\"name\": %s}}", name);
    }
}
