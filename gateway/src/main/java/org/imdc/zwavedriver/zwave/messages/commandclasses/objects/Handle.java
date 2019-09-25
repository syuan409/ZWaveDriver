package org.imdc.zwavedriver.zwave.messages.commandclasses.objects;

import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import com.inductiveautomation.ignition.common.Dataset;
import com.inductiveautomation.ignition.common.util.DatasetBuilder;

import java.util.ArrayList;
import java.util.List;

public enum Handle {
    OUTSIDE_HANDLE1(0x10, "Outside Handle 1"),
    OUTSIDE_HANDLE2(0x20, "Outside Handle 2"),
    OUTSIDE_HANDLE3(0x40, "Outside Handle 3"),
    OUTSIDE_HANDLE4(0x80, "Outside Handle 4"),
    INSIDE_HANDLE1(0x01, "Inside Handle 1"),
    INSIDE_HANDLE2(0x02, "Inside Handle 2"),
    INSIDE_HANDLE3(0x04, "Inside Handle 3"),
    INSIDE_HANDLE4(0x08, "Inside Handle 4");

    public final byte value;
    public final String name;

    Handle(int value, String name) {
        this.value = (byte) value;
        this.name = name;
    }

    public static List<Handle> getHandles(int b) {
        List<Handle> handles = new ArrayList();
        for (Handle h : values()) {
            if ((h.value & (byte) b) != 0) {
                handles.add(h);
            }
        }
        return handles;
    }

    public static Handle from(int handle) throws DecoderException {
        for (Handle h : values()) {
            if (h.value == (byte) handle) {
                return h;
            }
        }
        throw new DecoderException("Unknown Handle");
    }

    public static Dataset buildDS() {
        DatasetBuilder builder = new DatasetBuilder();
        builder.colNames("Name", "Value");
        builder.colTypes(String.class, Byte.class);
        for (Handle r : values()) {
            builder.addRow(r.name, r.value);
        }
        return builder.build();
    }

    @Override
    public String toString() {
        return String.format("{\"Handle\":{\"name\": %s}}", name);
    }
}
