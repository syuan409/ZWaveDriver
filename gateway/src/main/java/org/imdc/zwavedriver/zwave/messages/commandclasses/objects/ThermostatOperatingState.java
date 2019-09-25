package org.imdc.zwavedriver.zwave.messages.commandclasses.objects;

import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import com.inductiveautomation.ignition.common.Dataset;
import com.inductiveautomation.ignition.common.util.DatasetBuilder;

public enum ThermostatOperatingState {
    IDLE(0x00, 0, 0x01, "Idle"),
    HEATING(0x01, 0, 0x02, "Heating"),
    COOLING(0x02, 0, 0x04, "Cooling"),
    FAN_ONLY(0x03, 0, 0x08, "Fan Only"),
    PENDING_HEAT(0x04, 0, 0x10, "Pending Heat"),
    PENDING_COOL(0x05, 0, 0x20, "Pending Cool"),
    VENT(0x06, 0, 0x40, "Vent"),
    AUX_HEATING(0x07, 0, 0x80, "Aux Heating"),
    SECOND_STATE_HEATING(0x08, 1, 0x01, "2nd Stage Heating"),
    SECOND_STATE_COOLING(0x09, 1, 0x02, "2nd Stage Cooling"),
    SECOND_STATE_AUX_HEAT(0x0A, 1, 0x04, "2nd Stage Aux Heat"),
    THIRD_STATE_AUX_HEAT(0x0B, 1, 0x08, "3rd Stage Aux Heat");

    final public byte value;
    final public byte bitMaskByte;
    final public byte bitMask;
    final public String name;

    ThermostatOperatingState(int value, int bitMaskByte, int bitMask, String name) {
        this.value = (byte) value;
        this.name = name;
        this.bitMaskByte = (byte) bitMaskByte;
        this.bitMask = (byte) bitMask;
    }

    public static ThermostatOperatingState from(int state) throws DecoderException {
        for (ThermostatOperatingState m : values()) {
            if (m.value == (byte) state) {
                return m;
            }
        }
        throw new DecoderException("Unknown Operating State");
    }

    public static Dataset buildDS() {
        DatasetBuilder builder = new DatasetBuilder();
        builder.colNames("Name", "Value", "BitMaskByte", "BitMask");
        builder.colTypes(String.class, Byte.class, Byte.class, Byte.class);
        for (ThermostatOperatingState r : values()) {
            builder.addRow(r.name, r.value, r.bitMaskByte, r.bitMask);
        }
        return builder.build();
    }

    @Override
    public String toString() {
        return String.format("{\"ThermostatOperating.State\":{\"name\": %s}}", name);
    }
}
