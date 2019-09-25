package org.imdc.zwavedriver.zwave.messages.commandclasses.objects;

import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import com.inductiveautomation.ignition.common.Dataset;
import com.inductiveautomation.ignition.common.util.DatasetBuilder;

public enum Direction {
    UP(0x00, "Up"),
    DOWN(0x01, "Down"),
    NONE(0x03, "None");

    final public byte value;
    final public String name;

    Direction(int value, String name) {
        this.value = (byte) value;
        this.name = name;
    }

    public static Direction from(String direction) throws DecoderException {
        for (Direction d : values()) {
            if (d.name.equals(direction)) {
                return d;
            }
        }
        throw new DecoderException("Unknown Direction");
    }

    public static Dataset buildDS() {
        DatasetBuilder builder = new DatasetBuilder();
        builder.colNames("Name", "Value");
        builder.colTypes(String.class, Byte.class);
        for (Direction r : values()) {
            builder.addRow(r.name, r.value);
        }
        return builder.build();
    }

    @Override
    public String toString() {
        return String.format("{\"Direction\":{\"name\": %s}}", name);
    }
}
