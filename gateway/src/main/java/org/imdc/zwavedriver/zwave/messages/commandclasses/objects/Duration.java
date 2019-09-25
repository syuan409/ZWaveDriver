package org.imdc.zwavedriver.zwave.messages.commandclasses.objects;

import com.inductiveautomation.ignition.common.Dataset;
import com.inductiveautomation.ignition.common.util.DatasetBuilder;

public class Duration {
    public enum DurationMode {
        INSTANT(0x00, "Instant"),
        SECOND(0x01, "Second"),
        MINUTE(0x02, "Minute"),
        DEFAULT(0xFF, "Default");

        public final byte value;
        public final String name;

        DurationMode(int value, String name) {
            this.value = (byte) value;
            this.name = name;
        }

        public static Dataset buildDS() {
            DatasetBuilder builder = new DatasetBuilder();
            builder.colNames("Name", "Value");
            builder.colTypes(String.class, Byte.class);
            for (DurationMode r : values()) {
                builder.addRow(r.name, r.value);
            }
            return builder.build();
        }

        @Override
        public String toString() {
            return String.format("{\"Duration.Mode\":{\"name\": %s}}", name);
        }
    }

    public DurationMode mode;
    private byte value;

    public Duration(int duration) {
        this.mode = from(duration);
        this.value = fromDurationValue(this.mode, duration);
    }

    public int getValue() {
        if (mode == DurationMode.MINUTE) {
            return value + 0x80;
        } else {
            return value;
        }
    }

    private DurationMode from(int duration) {
        if (duration == 0xFF) {
            return DurationMode.DEFAULT;
        } else if (duration >= 0x01 && duration <= 0x7F) {
            return DurationMode.SECOND;
        } else if (duration >= 0x80 && duration <= 0xFE) {
            return DurationMode.MINUTE;
        } else {
            return DurationMode.INSTANT;
        }
    }

    private byte fromDurationValue(DurationMode mode, int durationValue) {
        if (mode == DurationMode.SECOND) {
            return (byte) durationValue;
        } else if (mode == DurationMode.MINUTE) {
            return (byte) (durationValue - 0x80);
        } else {
            return mode.value;
        }
    }

    @Override
    public String toString() {
        return String.format("{\"Duration\":{\"mode\": %s, \"value\": %d}}", mode.toString(), value);
    }
}
