package org.imdc.zwavedriver.zwave.messages.commandclasses.objects;

import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import com.inductiveautomation.ignition.common.Dataset;
import com.inductiveautomation.ignition.common.util.DatasetBuilder;

public enum ThermostatMode {
    OFF(0x00, "Off"),
    HEAT(0x01, "Heat"),
    COOL(0x02, "Cool"),
    AUTO(0x03, "Auto"),
    AUXILIARY(0x04, "Auxiliary"),
    RESUME(0x05, "Resume"),
    FAN(0x06, "Fan"),
    FURNACE(0x07, "Furnace"),
    DRY(0x08, "Dry"),
    MOIST(0x09, "Moist"),
    AUTO_CHANGEOVER(0x0A, "Auto Changeover"),
    ENERGY_HEAT(0x0B, "Energy Heat"),
    ENERGY_COOL(0x0C, "Energy Cool"),
    AWAY(0x0D, "Away"),
    FULL_POWER(0x0F, "Full Power"),
    MANUFACTURER_SPECIFIC(0x1F, "Manufacturer Specific");

    final public byte value;
    final public String name;

    ThermostatMode(int value, String name) {
        this.value = (byte) value;
        this.name = name;
    }

    public static ThermostatMode from(String mode) throws DecoderException {
        for (ThermostatMode m : values()) {
            if (m.name.equals(mode)) {
                return m;
            }
        }
        throw new DecoderException("Unknown Mode");
    }

    public static ThermostatMode from(int mode) throws DecoderException {
        for (ThermostatMode m : values()) {
            if (m.value == (byte) mode) {
                return m;
            }
        }
        throw new DecoderException("Unknown Mode");
    }

    public static Dataset buildDS() {
        DatasetBuilder builder = new DatasetBuilder();
        builder.colNames("Name", "Value");
        builder.colTypes(String.class, Byte.class);
        for (ThermostatMode r : values()) {
            builder.addRow(r.name, r.value);
        }
        return builder.build();
    }

    @Override
    public String toString() {
        return String.format("{\"Thermostat.Mode\":{\"name\": %s}}", name);
    }
}
