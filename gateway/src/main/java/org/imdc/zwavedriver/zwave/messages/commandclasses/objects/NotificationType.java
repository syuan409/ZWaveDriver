package org.imdc.zwavedriver.zwave.messages.commandclasses.objects;

import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import com.inductiveautomation.ignition.common.Dataset;
import com.inductiveautomation.ignition.common.util.DatasetBuilder;

import java.util.ArrayList;
import java.util.List;

public enum NotificationType {
    SMOKE(0x01, "Smoke"),
    CO(0x02, "CO"),
    CO2(0x03, "CO2"),
    HEAT(0x04, "Heat"),
    WATER(0x05, "Water"),
    ACCESS_CONTROL(0x06, "Access Control"),
    HOME_SECURITY(0x07, "Home Security"),
    POWER_MANAGEMENT(0x08, "Power Management"),
    SYSTEM(0x09, "System"),
    EMERGENCY(0x0A, "Emergency"),
    CLOCK(0x0B, "Clock"),
    APPLIANCE(0x0C, "Appliance"),
    HOME_HEALTH(0x0D, "Home Health"),
    SIREN(0x0E, "Siren"),
    WATER_VALVE(0x0F, "Water Valve"),
    WEATHER(0x10, "Weather"),
    IRRIGATION(0x11, "Irrigation"),
    GAS(0x12, "Gas"),
    PEST_CONTROL(0x13, "Pest Control"),
    LIGHT_SENSOR(0x14, "Light Sensor"),
    WATER_QUALITY_MONITORING(0x15, "Water Quality Monitoring"),
    HOME_MONITORING(0x16, "Home Monitoring"),
    PENDING(0xFF, "Pending");

    final public byte value;
    final public String name;

    NotificationType(int value, String name) {
        this.value = (byte) value;
        this.name = name;
    }

    public static NotificationType from(String type) throws DecoderException {
        for (NotificationType t : values()) {
            if (t.name.equals(type)) {
                return t;
            }
        }
        throw new DecoderException("Unknown Notification Type");
    }

    public static NotificationType from(int type) throws DecoderException {
        for (NotificationType t : NotificationType.values()) {
            if (t.value == (byte) type) {
                return t;
            }
        }
        throw new DecoderException("Unknown Notification Type");
    }

    public static List<NotificationType> getTypes(List<Integer> in) {
        List<NotificationType> types = new ArrayList();
        if (in != null) {
            for (int i : in) {
                try {
                    types.add(NotificationType.from(i));
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
        for (NotificationType r : values()) {
            builder.addRow(r.name, r.value);
        }
        return builder.build();
    }

    @Override
    public String toString() {
        return String.format("{\"Notification.Type\":{\"name\": %s}}", name);
    }
}
