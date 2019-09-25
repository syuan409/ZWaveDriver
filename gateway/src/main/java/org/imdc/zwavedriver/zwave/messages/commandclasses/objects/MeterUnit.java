package org.imdc.zwavedriver.zwave.messages.commandclasses.objects;

import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import com.inductiveautomation.ignition.common.Dataset;
import com.inductiveautomation.ignition.common.util.DatasetBuilder;

public enum MeterUnit {
    KWH(new MeterType[]{MeterType.ELECTRIC_METER, MeterType.HEATING_METER, MeterType.COOLING_METER}, 0x00, 0x01, "KWh", "Power"),
    KVAh(MeterType.ELECTRIC_METER, 0x01, 0x02, "kVAh", "Energy"),
    W(MeterType.ELECTRIC_METER, 0x02, 0x04, "W", "Power"),
    PULSES(new MeterType[]{MeterType.ELECTRIC_METER, MeterType.GAS_METER, MeterType.WATER_METER}, 0x03, 0x08, "Pulses", "Count"),
    V(MeterType.ELECTRIC_METER, 0x04, 0x10, "V", "Voltage"),
    A(MeterType.ELECTRIC_METER, 0x05, 0x20, "A", "Current"),
    POWER_FACTOR(MeterType.ELECTRIC_METER, 0x06, 0x40, "Power Factor", "Power Factor"),
    MST(new MeterType[]{MeterType.ELECTRIC_METER, MeterType.GAS_METER, MeterType.WATER_METER}, 0x07, 0x80, "MST", "MST"),
    CUBIC_METERS(new MeterType[]{MeterType.GAS_METER, MeterType.WATER_METER}, 0x00, 0x01, "Cubic Meters", "Volume"),
    CUBIC_FEET(new MeterType[]{MeterType.GAS_METER, MeterType.WATER_METER}, 0x01, 0x02, "Cubic Feet", "Volume"),
    GALLONS(MeterType.WATER_METER, 0x02, 0x04, "US gallons", "Volume");

    final public MeterType[] types;
    final public byte scale;
    final public byte supportedBitMask;
    final public String unit;
    final public String name;

    MeterUnit(MeterType type, int scale, int supportedBitMask, String unit, String name) {
        this(new MeterType[]{type}, scale, supportedBitMask, unit, name);
    }

    MeterUnit(MeterType[] types, int scale, int supportedBitMask, String unit, String name) {
        this.types = types;
        this.scale = (byte) scale;
        this.supportedBitMask = (byte) supportedBitMask;
        this.unit = unit;
        this.name = name;
    }

    public static MeterUnit fromMeterScale(MeterType meterType, int scale) throws DecoderException {
        for (MeterUnit u : values()) {
            for (MeterType t : u.types) {
                if ((t == meterType) && (u.scale == (byte) scale)) {
                    return u;
                }
            }
        }
        throw new DecoderException("Unknown Meter Unit");
    }

    public static Dataset buildDS() {
        return buildDS(null);
    }

    public static Dataset buildDS(MeterType meterType) {
        DatasetBuilder builder = new DatasetBuilder();
        builder.colNames("Name", "Type", "Scale", "SupportedBitMask");
        builder.colTypes(String.class, Byte.class, Byte.class, String.class);
        for (MeterUnit u : values()) {
            for (MeterType t : u.types) {
                if (meterType == null || meterType == t) {
                    builder.addRow(u.unit, t.name, u.scale, u.supportedBitMask);
                }
            }
        }
        return builder.build();
    }

    @Override
    public String toString() {
        return String.format("{\"Meter.Unit\":{\"name\": %s, \"unit\": %s}}", name, unit);
    }
}
