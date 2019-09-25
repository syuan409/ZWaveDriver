package org.imdc.zwavedriver.zwave.messages.commandclasses.objects;

import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import com.inductiveautomation.ignition.common.Dataset;
import com.inductiveautomation.ignition.common.util.DatasetBuilder;

public enum Weekday {
    UNKNOWN(0x00, "Unknown"),
    MONDAY(0x01, "Monday"),
    TUESDAY(0x02, "Tuesday"),
    WEDNESDAY(0x03, "Wednesday"),
    THURSDAY(0x04, "Thursday"),
    FRIDAY(0x05, "Friday"),
    SATURDAY(0x06, "Saturday"),
    SUNDAY(0x07, "Friday");

    public final byte value;
    public final String name;

    Weekday(int value, String name) {
        this.value = (byte) value;
        this.name = name;
    }

    public static Weekday from(String type) throws DecoderException {
        for (Weekday w : values()) {
            if (w.name.equals(type)) {
                return w;
            }
        }
        throw new DecoderException("Unknown Weekday");
    }

    public static Weekday from(int type) throws DecoderException {
        for (Weekday w : values()) {
            if (w.value == (byte) type) {
                return w;
            }
        }
        throw new DecoderException("Unknown Weekday");
    }

    public static Dataset buildDS() {
        DatasetBuilder builder = new DatasetBuilder();
        builder.colNames("Name", "Value");
        builder.colTypes(String.class, Byte.class);
        for (Weekday r : values()) {
            builder.addRow(r.name, r.value);
        }
        return builder.build();
    }

    @Override
    public String toString() {
        return String.format("{\"Weekday\":{\"name\": %s}}", name);
    }
}
