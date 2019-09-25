package org.imdc.zwavedriver.zwave.messages.framework;

import com.inductiveautomation.ignition.common.Dataset;
import com.inductiveautomation.ignition.common.util.DatasetBuilder;

public enum ControllerType {
    PRIMARY("Primary"),
    SECONDARY("Secondary");

    public final String name;

    ControllerType(String name) {
        this.name = name;
    }

    public static Dataset buildDS() {
        DatasetBuilder builder = new DatasetBuilder();
        builder.colNames("Name");
        builder.colTypes(String.class);
        for (ControllerType r : values()) {
            builder.addRow(r.name);
        }
        return builder.build();
    }

    @Override
    public String toString() {
        return String.format("{\"Controller.Type\":{\"name\": %s}}", name);
    }
}
