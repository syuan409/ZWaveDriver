package org.imdc.zwavedriver.zwave.messages.commandclasses.objects;

import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import com.inductiveautomation.ignition.common.Dataset;
import com.inductiveautomation.ignition.common.util.DatasetBuilder;

import java.util.ArrayList;
import java.util.List;

public enum Color {
    WARM_WHITE(0x00, "Warm White"),
    COLD_WHITE(0x01, "Cold White"),
    RED(0x02, "Red"),
    GREEN(0x03, "Green"),
    BLUE(0x04, "Blue"),
    AMBER(0x05, "Amber"),
    CYAN(0x06, "Cyan"),
    PURPLE(0x07, "Purple");

    final public byte value;
    final public String name;

    Color(int value, String name) {
        this.value = (byte) value;
        this.name = name;
    }

    public static Color from(String color) throws DecoderException {
        for (Color c : values()) {
            if (c.name.equals(color)) {
                return c;
            }
        }
        throw new DecoderException("Unknown Color");
    }

    public static Color from(int color) throws DecoderException {
        for (Color c : values()) {
            if (c.value == (byte) color) {
                return c;
            }
        }
        throw new DecoderException("Unknown Color");
    }

    public static List<Color> getColors(List<Integer> in) {
        List<Color> colors = new ArrayList();
        if (in != null) {
            for (int i : in) {
                try {
                    colors.add(Color.from(i));
                } catch (DecoderException ignored) {
                }
            }
        }
        return colors;
    }

    public static Dataset buildDS() {
        DatasetBuilder builder = new DatasetBuilder();
        builder.colNames("Name", "Value");
        builder.colTypes(String.class, Byte.class);
        for (Color r : values()) {
            builder.addRow(r.name, r.value);
        }
        return builder.build();
    }

    @Override
    public String toString() {
        return String.format("{\"Color\":{\"name\": %s}}", name);
    }
}
