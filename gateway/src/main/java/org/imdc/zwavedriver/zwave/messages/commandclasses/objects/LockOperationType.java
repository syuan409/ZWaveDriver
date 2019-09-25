package org.imdc.zwavedriver.zwave.messages.commandclasses.objects;

import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import com.inductiveautomation.ignition.common.Dataset;
import com.inductiveautomation.ignition.common.util.DatasetBuilder;

import java.util.ArrayList;
import java.util.List;

public enum LockOperationType {
    CONSTANT(0x01, "Constant"),
    TIMED(0x02, "Timed");

    public final byte value;
    public final String name;

    LockOperationType(int value, String name) {
        this.value = (byte) value;
        this.name = name;
    }

    public static LockOperationType from(int mode) throws DecoderException {
        for (LockOperationType m : values()) {
            if (m.value == (byte) mode) {
                return m;
            }
        }
        throw new DecoderException("Unknown Lock Mode");
    }

    public static List<LockOperationType> getTypes(List<Integer> in) {
        List<LockOperationType> types = new ArrayList();
        if (in != null) {
            for (int i : in) {
                try {
                    types.add(from(i));
                } catch (DecoderException ignored) {
                }
            }
        }
        return types;
    }

    public static Dataset buildDS() {
        DatasetBuilder builder = new DatasetBuilder();
        builder.colNames("Name", "Value");
        builder.colTypes(String.class, Byte.class);
        for (LockOperationType r : values()) {
            builder.addRow(r.name, r.value);
        }
        return builder.build();
    }

    @Override
    public String toString() {
        return String.format("{\"Lock.Mode\":{\"name\": %s}}", name);
    }
}
