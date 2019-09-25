package org.imdc.zwavedriver.zwave.messages.commandclasses.objects;

import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import com.inductiveautomation.ignition.common.Dataset;
import com.inductiveautomation.ignition.common.util.DatasetBuilder;

public enum SwitchType {
    UNDEFINED(0x00, "Undefined"),
    OFF_ON(0x01, "Off/On"),
    DOWN_UP(0x02, "Down/Up"),
    CLOSE_OPEN(0x03, "Close/Open"),
    COUNTERCLOCKWISE_CLOCKWISE(0x04, "Counter Clockwise/Clockwise"),
    LEFT_RIGHT(0x05, "Left/Right"),
    REVERSE_FORWARD(0x06, "Reverse/Forward"),
    PULL_PUSH(0x07, "Pull/Push");

    public final byte value;
    public final String name;

    SwitchType(int value, String name) {
        this.value = (byte) value;
        this.name = name;
    }

    public static SwitchType from(int type) throws DecoderException {
        for (SwitchType s : values()) {
            if (s.value == (byte) type) {
                return s;
            }
        }
        throw new DecoderException("Unknown Switch Type");
    }

    public static Dataset buildDS() {
        DatasetBuilder builder = new DatasetBuilder();
        builder.colNames("Name", "Value");
        builder.colTypes(String.class, Byte.class);
        for (SwitchType r : values()) {
            builder.addRow(r.name, r.value);
        }
        return builder.build();
    }

    @Override
    public String toString() {
        return String.format("{\"Switch.Type\":{\"name\": %s}}", name);
    }
}
