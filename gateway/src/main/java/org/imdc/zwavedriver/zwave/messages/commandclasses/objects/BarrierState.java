package org.imdc.zwavedriver.zwave.messages.commandclasses.objects;

import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import com.inductiveautomation.ignition.common.Dataset;
import com.inductiveautomation.ignition.common.util.DatasetBuilder;

public enum BarrierState {
    CLOSED(0x00, "Closed"),
    POSITION(0x01, "Position"),
    CLOSING(0xFC, "Closing"),
    STOPPED(0xFD, "Stopped"),
    OPENING(0xFE, "Opening"),
    OPEN(0xFF, "Open");

    final public byte value;
    final public String name;

    BarrierState(int value, String name) {
        this.value = (byte) value;
        this.name = name;
    }

    public static BarrierState from(String state) throws DecoderException {
        for (BarrierState s : BarrierState.values()) {
            if (s.name.equals(state)) {
                return s;
            }
        }
        throw new DecoderException("Unknown State");
    }

    public static BarrierState from(int state) throws DecoderException {
        if (state >= 0x01 && state <= 0x63) {
            return POSITION;
        }

        for (BarrierState s : BarrierState.values()) {
            if (s.value == (byte) state) {
                return s;
            }
        }
        throw new DecoderException("Unknown State");
    }

    public static Dataset buildDS() {
        DatasetBuilder builder = new DatasetBuilder();
        builder.colNames("Name", "Value");
        builder.colTypes(String.class, Byte.class);
        for (BarrierState r : values()) {
            builder.addRow(r.name, r.value);
        }
        return builder.build();
    }

    @Override
    public String toString() {
        return String.format("{\"State\":{\"name\": %s}}", name);
    }
}
