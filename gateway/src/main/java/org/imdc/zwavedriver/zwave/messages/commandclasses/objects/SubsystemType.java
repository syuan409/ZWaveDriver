package org.imdc.zwavedriver.zwave.messages.commandclasses.objects;

import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import com.inductiveautomation.ignition.common.Dataset;
import com.inductiveautomation.ignition.common.util.DatasetBuilder;

import java.util.ArrayList;
import java.util.List;

public enum SubsystemType {
    UNDEFINED(0x00, "Undefined"),
    AUDIBLE(0x01, "Audible"),
    VISUAL(0x02, "Visual");

    public final byte value;
    public final String name;

    SubsystemType(int value, String name) {
        this.value = (byte) value;
        this.name = name;
    }

    public static SubsystemType from(int type) throws DecoderException {
        for (SubsystemType s : values()) {
            if (s.value == (byte) type) {
                return s;
            }
        }
        throw new DecoderException("Unknown Subsystem Type");
    }

    public static List<SubsystemType> getTypes(List<Integer> in) {
        List<SubsystemType> types = new ArrayList();
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
        for (SubsystemType r : values()) {
            builder.addRow(r.name, r.value);
        }
        return builder.build();
    }

    @Override
    public String toString() {
        return String.format("{\"Subsystem.Type\":{\"name\": %s}}", name);
    }
}
