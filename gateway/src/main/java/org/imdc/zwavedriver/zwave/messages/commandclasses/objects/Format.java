package org.imdc.zwavedriver.zwave.messages.commandclasses.objects;

import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;

public enum Format {
    SIGNED_INTEGER(0x00, "Signed Integer"),
    UNSIGNED_INTEGER(0x01, "Unsigned Integer"),
    ENUMERATED(0x02, "Enumerated"),
    BIT_FIELD(0x03, "Bits");

    final public byte value;
    final public String name;

    Format(int value, String display) {
        this.value = (byte) value;
        this.name = display;
    }

    public static Format from(int format) throws DecoderException {
        for (Format f : values()) {
            if (f.value == (byte) format) {
                return f;
            }
        }
        throw new DecoderException("Unknown Format");
    }

    public String intToString(int value) {
        if (this == UNSIGNED_INTEGER) {
            return Long.toString(value & 0xFFFFFFFFL);
        }

        return Integer.toString(value);
    }

    @Override
    public String toString() {
        return String.format("{\"Format\":{\"name\": %s}}", name);
    }
}
