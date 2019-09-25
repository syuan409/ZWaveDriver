package org.imdc.zwavedriver.zwave.messages.commandclasses.objects;

import com.inductiveautomation.ignition.common.Dataset;
import com.inductiveautomation.ignition.common.util.DatasetBuilder;

public class Timeout {
    public enum TimeoutMode {
        NO_TIMER(0x00, "No Timer"),
        SECOND(0x01, "Second"),
        MINUTE(0x02, "Minute"),
        NO_TIMEOUT(0xFF, "No Timeout");

        public final byte value;
        public final String name;

        TimeoutMode(int value, String name) {
            this.value = (byte) value;
            this.name = name;
        }

        public static Dataset buildDS() {
            DatasetBuilder builder = new DatasetBuilder();
            builder.colNames("Name", "Value");
            builder.colTypes(String.class, Byte.class);
            for (TimeoutMode r : values()) {
                builder.addRow(r.name, r.value);
            }
            return builder.build();
        }

        @Override
        public String toString() {
            return String.format("{\"Timeout.Mode\":{\"name\": %s}}", name);
        }
    }

    public TimeoutMode mode;
    private byte value;

    public Timeout(int value) {
        this.mode = from(value);
        this.value = fromTimeoutValue(this.mode, value);
    }

    public int getValue() {
        if (mode == TimeoutMode.MINUTE) {
            return value + 0x41;
        } else {
            return value;
        }
    }

    private TimeoutMode from(int value) {
        if (value == 0xFF) {
            return TimeoutMode.NO_TIMEOUT;
        } else if (value >= 0x01 && value <= 0x3C) {
            return TimeoutMode.SECOND;
        } else if (value >= 0x41 && value <= 0xFE) {
            return TimeoutMode.MINUTE;
        } else {
            return TimeoutMode.NO_TIMER;
        }
    }

    private byte fromTimeoutValue(TimeoutMode mode, int value) {
        if (mode == TimeoutMode.SECOND) {
            return (byte) value;
        } else if (mode == TimeoutMode.MINUTE) {
            return (byte) (value - 0x41);
        } else {
            return mode.value;
        }
    }

    @Override
    public String toString() {
        return String.format("{\"Timeout\":{\"mode\": %s, \"value\": %d}}", mode.toString(), value);
    }
}
