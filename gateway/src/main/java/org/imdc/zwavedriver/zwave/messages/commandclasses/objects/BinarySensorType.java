package org.imdc.zwavedriver.zwave.messages.commandclasses.objects;

import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import com.inductiveautomation.ignition.common.Dataset;
import com.inductiveautomation.ignition.common.util.DatasetBuilder;

import java.util.ArrayList;
import java.util.List;

public enum BinarySensorType {
    RESERVERD(0x00, "Reserved"),
    GENERAL(0x01, "General"),
    SMOKE(0x02, "Smoke"),
    CO(0x03, "CO"),
    CO2(0x04, "CO2"),
    HEAT(0x05, "Heat"),
    WATER(0x06, "Water"),
    FREEZE(0x07, "Freeze"),
    TAMPER(0x08, "Tamper"),
    AUX(0x09, "Aux"),
    DOOR(0x0A, "Door Window"),
    TILT(0x0B, "Tilt"),
    MOTION(0x0C, "Motion"),
    GLASS_BREAK(0x0D, "Glass Break"),
    FIRST(0xFF, "Return 1st Sensor");

    public final byte value;
    public final String name;

    BinarySensorType(int value, String name) {
        this.value = (byte) value;
        this.name = name;
    }

    public static BinarySensorType from(int type) throws DecoderException {
        for (BinarySensorType s : values()) {
            if (s.value == (byte) type) {
                return s;
            }
        }
        throw new DecoderException("Unknown Binary Sensor Type");
    }

    public static List<BinarySensorType> getTypes(List<Integer> in) {
        List<BinarySensorType> types = new ArrayList();
        if (in != null) {
            for (int i : in) {
                try {
                    types.add(from(i));
                } catch (DecoderException ignored) {
                }
            }
        }
        return types;
    }

    public static Dataset buildDS() {
        DatasetBuilder builder = new DatasetBuilder();
        builder.colNames("Name", "Value");
        builder.colTypes(String.class, Byte.class);
        for (BinarySensorType r : values()) {
            builder.addRow(r.name, r.value);
        }
        return builder.build();
    }

    @Override
    public String toString() {
        return String.format("{\"Sensor.Binary.Type\":{\"name\": %s}}", name);
    }
}
