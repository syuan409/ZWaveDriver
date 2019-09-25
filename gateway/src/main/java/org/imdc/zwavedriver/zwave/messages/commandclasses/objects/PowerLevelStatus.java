package org.imdc.zwavedriver.zwave.messages.commandclasses.objects;

import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import com.inductiveautomation.ignition.common.Dataset;
import com.inductiveautomation.ignition.common.util.DatasetBuilder;

public enum PowerLevelStatus {
    FAILED(0x00, "Failed"),
    SUCCESS(0x01, "Success"),
    PROGRESS(0x02, "In Progress");

    public final byte value;
    public final String name;

    PowerLevelStatus(int value, String name) {
        this.value = (byte) value;
        this.name = name;
    }

    public static PowerLevelStatus from(int level) throws DecoderException {
        for (PowerLevelStatus l : values()) {
            if (l.value == (byte) level) {
                return l;
            }
        }
        throw new DecoderException("Unknown Power Level Status");
    }

    public static Dataset buildDS() {
        DatasetBuilder builder = new DatasetBuilder();
        builder.colNames("Name", "Value");
        builder.colTypes(String.class, Byte.class);
        for (PowerLevelStatus r : values()) {
            builder.addRow(r.name, r.value);
        }
        return builder.build();
    }

    @Override
    public String toString() {
        return String.format("{\"Power.Level.Status\":{\"name\": %s}}", name);
    }
}
