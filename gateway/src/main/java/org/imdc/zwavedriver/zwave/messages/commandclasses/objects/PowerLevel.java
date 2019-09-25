package org.imdc.zwavedriver.zwave.messages.commandclasses.objects;

import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import com.inductiveautomation.ignition.common.Dataset;
import com.inductiveautomation.ignition.common.util.DatasetBuilder;

public enum PowerLevel {
    NORMAL(0x00, "Normal"),
    MINUS_1_DBM(0x01, "-1 dBm"),
    MINUS_2_DBM(0x02, "-2 dBm"),
    MINUS_3_DBM(0x03, "-3 dBm"),
    MINUS_4_DBM(0x04, "-4 dBm"),
    MINUS_5_DBM(0x05, "-5 dBm"),
    MINUS_6_DBM(0x06, "-6 dBm"),
    MINUS_7_DBM(0x07, "-7 dBm"),
    MINUS_8_DBM(0x08, "-8 dBm"),
    MINUS_9_DBM(0x09, "-9 dBm");

    public final byte value;
    public final String name;

    PowerLevel(int value, String name) {
        this.value = (byte) value;
        this.name = name;
    }

    public static PowerLevel from(String level) throws DecoderException {
        for (PowerLevel l : values()) {
            if (l.name.equals(level)) {
                return l;
            }
        }
        throw new DecoderException("Unknown Power Level");
    }

    public static PowerLevel from(int level) throws DecoderException {
        for (PowerLevel l : values()) {
            if (l.value == (byte) level) {
                return l;
            }
        }
        throw new DecoderException("Unknown Power Level");
    }

    public static Dataset buildDS() {
        DatasetBuilder builder = new DatasetBuilder();
        builder.colNames("Name", "Value");
        builder.colTypes(String.class, Byte.class);
        for (PowerLevel r : values()) {
            builder.addRow(r.name, r.value);
        }
        return builder.build();
    }

    @Override
    public String toString() {
        return String.format("{\"Power.Level\":{\"name\": %s}}", name);
    }
}
