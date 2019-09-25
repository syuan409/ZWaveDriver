package org.imdc.zwavedriver.zwave.messages.commandclasses.objects;

import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import com.inductiveautomation.ignition.common.Dataset;
import com.inductiveautomation.ignition.common.util.DatasetBuilder;

public enum ThermostatSetpointType {
    HEATING(0x01, "Heating"),
    COOLING(0x02, "Cooling"),
    FURNACE(0x07, "Furnance"),
    DRY_AIR(0x08, "Dry Air"),
    MOIST_AIR(0x09, "Moist Air"),
    AUTO_CHANGEOVER(0x0A, "Auto Changeover"),
    ENERGY_SAVE_HEATING(0x0B, "Energy Save Heating"),
    ENERGY_SAVE_COOLING(0x0C, "Energy Save Cooling"),
    AWAY_HEATING(0x0D, "Away Heating"),
    AWAY_COOLING(0x0E, "Away Cooling"),
    FULL_POWER(0x0F, "Full Power");

    final public byte value;
    final public String name;

    ThermostatSetpointType(int value, String name) {
        this.value = (byte) value;
        this.name = name;
    }

    public static ThermostatSetpointType from(String type) throws DecoderException {
        for (ThermostatSetpointType t : values()) {
            if (t.name.equals(type)) {
                return t;
            }
        }
        throw new DecoderException("Unknown Thermostat Setpoint Type");
    }

    public static ThermostatSetpointType from(int type) throws DecoderException {
        for (ThermostatSetpointType u : values()) {
            if (u.value == (byte) type) {
                return u;
            }
        }
        throw new DecoderException("Unknown Thermostat Setpoint Type");
    }

    public static Dataset buildDS() {
        DatasetBuilder builder = new DatasetBuilder();
        builder.colNames("Name", "Value");
        builder.colTypes(String.class, Byte.class);
        for (ThermostatSetpointType u : values()) {
            builder.addRow(u.name, u.value);
        }
        return builder.build();
    }

    @Override
    public String toString() {
        return String.format("{\"Thermostat.Setpoint.Type\":{\"name\": %s}}", name);
    }
}
