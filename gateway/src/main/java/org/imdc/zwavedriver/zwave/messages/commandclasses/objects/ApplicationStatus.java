package org.imdc.zwavedriver.zwave.messages.commandclasses.objects;

import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import com.inductiveautomation.ignition.common.Dataset;
import com.inductiveautomation.ignition.common.util.DatasetBuilder;

public enum ApplicationStatus {
    LATER(0x00, "Later"),
    WAIT(0x01, "Wait"),
    QUEUED(0x02, "Queued");

    public final byte value;
    public final String name;

    ApplicationStatus(int value, String name) {
        this.value = (byte) value;
        this.name = name;
    }

    public static ApplicationStatus from(int type) throws DecoderException {
        for (ApplicationStatus s : ApplicationStatus.values()) {
            if (s.value == (byte) type) {
                return s;
            }
        }
        throw new DecoderException("Unknown Application Status");
    }

    public static Dataset buildDS() {
        DatasetBuilder builder = new DatasetBuilder();
        builder.colNames("Name", "Value");
        builder.colTypes(String.class, Byte.class);
        for (ApplicationStatus r : values()) {
            builder.addRow(r.name, r.value);
        }
        return builder.build();
    }

    @Override
    public String toString() {
        return String.format("{\"Application.Status\":{\"name\": %s}}", name);
    }
}
