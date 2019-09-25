package org.imdc.zwavedriver.zwave.messages.commandclasses.objects;

import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import com.inductiveautomation.ignition.common.Dataset;
import com.inductiveautomation.ignition.common.util.DatasetBuilder;

public enum ThermostatFanMode {
    AUTO_LOW(0x00, "Auto Low"),
    LOW(0x01, "Low"),
    AUTO_HIGH(0x02, "Auto High"),
    HIGH(0x03, "High"),
    AUTO_MEDIUM(0x04, "Auto Medium"),
    MEDIUM(0x05, "Medium"),
    CIRCULATION(0x06, "Circulation"),
    HUMIDITY_CIRCULATION(0x07, "Humidity Circulation"),
    LEFT_RIGHT(0x08, "Left and Right"),
    UP_DOWN(0x09, "Up and Down"),
    QUIET(0x0A, "Quiet");

    final public byte value;
    final public String name;

    ThermostatFanMode(int value, String name) {
        this.value = (byte) value;
        this.name = name;
    }

    public static ThermostatFanMode from(String mode) throws DecoderException {
        for (ThermostatFanMode m : values()) {
            if (m.name.equals(mode)) {
                return m;
            }
        }
        throw new DecoderException("Unknown Fan Mode");
    }

    public static ThermostatFanMode from(int mode) throws DecoderException {
        for (ThermostatFanMode m : values()) {
            if (m.value == (byte) mode) {
                return m;
            }
        }
        throw new DecoderException("Unknown Fan Mode");
    }

    public static Dataset buildDS() {
        DatasetBuilder builder = new DatasetBuilder();
        builder.colNames("Name", "Value");
        builder.colTypes(String.class, Byte.class);
        for (ThermostatFanMode r : values()) {
            builder.addRow(r.name, r.value);
        }
        return builder.build();
    }

    @Override
    public String toString() {
        return String.format("{\"ThermostatFan.Mode\":{\"name\": %s}}", name);
    }
}
