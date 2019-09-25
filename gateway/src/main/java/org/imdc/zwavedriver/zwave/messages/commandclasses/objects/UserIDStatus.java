package org.imdc.zwavedriver.zwave.messages.commandclasses.objects;

import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import com.inductiveautomation.ignition.common.Dataset;
import com.inductiveautomation.ignition.common.util.DatasetBuilder;

public enum UserIDStatus {
    AVAILABLE(0x00, "Available"),
    OCCUPIED(0x01, "Occupied"),
    RESERVED(0x02, "Reserved"),
    UNAVAILABLE(0x03, "Unavailable");

    public final byte value;
    public final String name;

    UserIDStatus(int value, String name) {
        this.value = (byte) value;
        this.name = name;
    }

    public static UserIDStatus from(String status) throws DecoderException {
        for (UserIDStatus s : values()) {
            if (s.name.equals(status)) {
                return s;
            }
        }
        throw new DecoderException("Unknown User ID Status");
    }

    public static UserIDStatus from(int status) throws DecoderException {
        for (UserIDStatus s : values()) {
            if (s.value == (byte) status) {
                return s;
            }
        }
        throw new DecoderException("Unknown User ID Status");
    }

    public static Dataset buildDS() {
        DatasetBuilder builder = new DatasetBuilder();
        builder.colNames("Name", "Value");
        builder.colTypes(String.class, Byte.class);
        for (UserIDStatus r : values()) {
            builder.addRow(r.name, r.value);
        }
        return builder.build();
    }

    @Override
    public String toString() {
        return String.format("{\"UserID.Status\":{\"name\": %s}}", name);
    }
}
