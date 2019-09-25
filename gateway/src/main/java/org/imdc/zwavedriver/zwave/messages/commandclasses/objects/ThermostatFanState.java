package org.imdc.zwavedriver.zwave.messages.commandclasses.objects;

import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import com.inductiveautomation.ignition.common.Dataset;
import com.inductiveautomation.ignition.common.util.DatasetBuilder;

public enum ThermostatFanState {
    OFF(0x00, "Off"),
    RUNNING(0x01, "Running"),
    RUNNING_HIGH(0x02, "Running High"),
    RUNNING_MEDIUM(0x03, "Running Medium"),
    CIRCULATION_MODE(0x04, "Circulation Mode"),
    HUMIDITY_CIRCULATION_MODE(0x05, "Humidity Circulation Mode"),
    RIGHT_LEFT_CIRCULATION_MODE(0x06, "Right - Left Circulation Mode"),
    UP_DOWN_CIRCULATION_MODE(0x07, "Up - Down Circulation Mode"),
    QUIET_CIRCULATION_MODE(0x08, "Quiet Circulation Mode");

    final public byte value;
    final public String name;

    ThermostatFanState(int value, String name) {
        this.value = (byte) value;
        this.name = name;
    }

    public static ThermostatFanState from(int state) throws DecoderException {
        for (ThermostatFanState m : values()) {
            if (m.value == (byte) state) {
                return m;
            }
        }
        throw new DecoderException("Unknown Fan State");
    }

    public static Dataset buildDS() {
        DatasetBuilder builder = new DatasetBuilder();
        builder.colNames("Name", "Value");
        builder.colTypes(String.class, Byte.class);
        for (ThermostatFanState r : values()) {
            builder.addRow(r.name, r.value);
        }
        return builder.build();
    }

    @Override
    public String toString() {
        return String.format("{\"ThermostatFan.State\":{\"name\": %s}}", name);
    }
}
