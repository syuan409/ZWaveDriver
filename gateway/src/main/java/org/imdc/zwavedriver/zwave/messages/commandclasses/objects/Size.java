package org.imdc.zwavedriver.zwave.messages.commandclasses.objects;

import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;

public enum Size {
    BIT8(0x01, "8 Bits"),
    BIT16(0x02, "16 Bits"),
    BIT24(0x03, "24 Bits"),
    BIT32(0x04, "32 Bits");

    final public byte value;
    final public String name;

    Size(int value, String name) {
        this.value = (byte) value;
        this.name = name;
    }

    public static Size from(String size) throws DecoderException {
        for (Size s : values()) {
            if (s.name.equals(size)) {
                return s;
            }
        }
        throw new DecoderException("Unknown Size");
    }

    public static Size from(int size) throws DecoderException {
        for (Size s : Size.values()) {
            if (s.value == (byte) size) {
                return s;
            }
        }
        throw new DecoderException("Unknown Size");
    }
}
