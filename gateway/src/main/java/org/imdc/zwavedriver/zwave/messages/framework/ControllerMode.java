package org.imdc.zwavedriver.zwave.messages.framework;

import com.inductiveautomation.ignition.common.Dataset;
import com.inductiveautomation.ignition.common.util.DatasetBuilder;

public enum ControllerMode {
    SLAVE("Slave"),
    CONTROLLER("Controller");

    public final String name;

    ControllerMode(String name) {
        this.name = name;
    }

    public static Dataset buildDS() {
        DatasetBuilder builder = new DatasetBuilder();
        builder.colNames("Name");
        builder.colTypes(String.class);
        for (ControllerMode r : values()) {
            builder.addRow(r.name);
        }
        return builder.build();
    }

    @Override
    public String toString() {
        return String.format("{\"Controller.Mode\":{\"name\": %s}}", name);
    }
}
