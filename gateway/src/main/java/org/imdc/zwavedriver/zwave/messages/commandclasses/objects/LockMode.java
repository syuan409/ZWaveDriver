package org.imdc.zwavedriver.zwave.messages.commandclasses.objects;

import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import com.inductiveautomation.ignition.common.Dataset;
import com.inductiveautomation.ignition.common.util.DatasetBuilder;

import java.util.ArrayList;
import java.util.List;

public enum LockMode {
    UNSECURED(0x00, "Unsecured"),
    UNSECURED_TIMEOUT(0x01, "Unsecured Timeout"),
    INSIDE_UNSECURED(0x10, "Inside Unsecured"),
    INSIDE_UNSECURED_TIMEOUT(0x11, "Inside Unsecured Timeout"),
    OUTSIDE_UNSECURED(0x20, "Outside Unsecured"),
    OUTSIDE_UNSECURED_TIMEOUT(0x21, "Outside Unsecured Timeout"),
    UNKNOWN(0xFE, "Unknown"),
    SECURED(0xFF, "Secured");

    public final byte value;
    public final String name;

    LockMode(int value, String name) {
        this.value = (byte) value;
        this.name = name;
    }

    public static LockMode from(String mode) throws DecoderException {
        for (LockMode l : values()) {
            if (l.name.equals(mode)) {
                return l;
            }
        }
        throw new DecoderException("Unknown Lock Mode");
    }

    public static LockMode from(int mode) throws DecoderException {
        for (LockMode m : values()) {
            if (m.value == (byte) mode) {
                return m;
            }
        }
        throw new DecoderException("Unknown Lock Mode");
    }

    public static List<LockMode> getModes(List<Integer> in) {
        List<LockMode> modes = new ArrayList();
        if (in != null) {
            for (int i : in) {
                try {
                    modes.add(from(i));
                } catch (DecoderException ignored) {
                }
            }
        }
        return modes;
    }

    public static Dataset buildDS() {
        DatasetBuilder builder = new DatasetBuilder();
        builder.colNames("Name", "Value");
        builder.colTypes(String.class, Byte.class);
        for (LockMode r : values()) {
            builder.addRow(r.name, r.value);
        }
        return builder.build();
    }

    @Override
    public String toString() {
        return String.format("{\"Lock.Mode\":{\"name\": %s}}", name);
    }
}
