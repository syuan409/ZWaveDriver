package org.imdc.zwavedriver.zwave.messages.commandclasses.objects;

import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import com.inductiveautomation.ignition.common.Dataset;
import com.inductiveautomation.ignition.common.util.DatasetBuilder;

public enum MeterType {
    ELECTRIC_METER(0x01, "Electric"),
    GAS_METER(0x02, "Gas"),
    WATER_METER(0x03, "Water"),
    HEATING_METER(0x04, "Heating"),
    COOLING_METER(0x05, "Cooling");

    final public byte value;
    final public String name;

    MeterType(int value, String name) {
        this.value = (byte) value;
        this.name = name;
    }

    public static MeterType from(int type) throws DecoderException {
        for (MeterType t : MeterType.values()) {
            if (t.value == (byte) type) {
                return t;
            }
        }
        throw new DecoderException("Unknown Meter Type");
    }

    public static Dataset buildDS() {
        DatasetBuilder builder = new DatasetBuilder();
        builder.colNames("Name", "Value");
        builder.colTypes(String.class, Byte.class);
        for (MeterType r : values()) {
            builder.addRow(r.name, r.value);
        }
        return builder.build();
    }

    @Override
    public String toString() {
        return String.format("{\"Meter.Type\":{\"name\": %s}}", name);
    }
}
