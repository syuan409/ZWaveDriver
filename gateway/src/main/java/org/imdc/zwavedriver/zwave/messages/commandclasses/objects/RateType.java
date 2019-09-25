package org.imdc.zwavedriver.zwave.messages.commandclasses.objects;

import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import com.inductiveautomation.ignition.common.Dataset;
import com.inductiveautomation.ignition.common.util.DatasetBuilder;

public enum RateType {
    UNSPECIFIED(0x00, "Unspecified"),
    IMPORT(0x01, "Import"),
    EXPORT(0x02, "Export"),
    RESERVED(0x03, "Reserved");

    final public byte value;
    final public String name;

    RateType(int value, String name) {
        this.value = (byte) value;
        this.name = name;
    }

    public static RateType from(int size) throws DecoderException {
        for (RateType t : values()) {
            if (t.value == (byte) size) {
                return t;
            }
        }
        throw new DecoderException("Unknown Rate Type");
    }

    public static Dataset buildDS() {
        DatasetBuilder builder = new DatasetBuilder();
        builder.colNames("Name", "Value");
        builder.colTypes(String.class, Byte.class);
        for (RateType r : values()) {
            builder.addRow(r.name, r.value);
        }
        return builder.build();
    }

    @Override
    public String toString() {
        return String.format("{\"Rate.Type\":{\"name\": %s}}", name);
    }
}
