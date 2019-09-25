package org.imdc.zwavedriver.zwave.messages.commandclasses.objects;

import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import com.inductiveautomation.ignition.common.Dataset;
import com.inductiveautomation.ignition.common.util.DatasetBuilder;

import java.util.ArrayList;
import java.util.List;

public enum ProtectionState {
    UNPROTECTED(0x00, "Unprotected"),
    SEQUENCE(0x01, "Protection by Sequence"),
    NO_OP(0x02, "No Operation");

    public final byte value;
    public final String name;

    ProtectionState(int value, String name) {
        this.value = (byte) value;
        this.name = name;
    }

    public static ProtectionState from(String state) throws DecoderException {
        for (ProtectionState s : values()) {
            if (s.name.equals(state)) {
                return s;
            }
        }
        throw new DecoderException("Unknown Weekday");
    }

    public static ProtectionState from(int state) throws DecoderException {
        for (ProtectionState s : values()) {
            if (s.value == (byte) state) {
                return s;
            }
        }
        throw new DecoderException("Unknown Protection State");
    }

    public static List<ProtectionState> getStates(List<Integer> in) {
        List<ProtectionState> states = new ArrayList();
        if (in != null) {
            for (int i : in) {
                try {
                    states.add(from(i));
                } catch (DecoderException ignored) {
                }
            }
        }
        return states;
    }

    public static Dataset buildDS() {
        DatasetBuilder builder = new DatasetBuilder();
        builder.colNames("Name", "Value");
        builder.colTypes(String.class, Byte.class);
        for (ProtectionState r : values()) {
            builder.addRow(r.name, r.value);
        }
        return builder.build();
    }

    @Override
    public String toString() {
        return String.format("{\"Protection.State\":{\"name\": %s}}", name);
    }
}
