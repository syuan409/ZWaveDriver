package org.imdc.zwavedriver.zwave.messages.framework;

public enum RequestNeighborUpdateStatus {
    STARTED(0x21, "Started"),
    DONE(0x22, "Done"),
    FAILED(0x23, "Failed"),
    UNKNOWN(0xFF, "Unknown");

    public final byte value;
    public final String name;

    RequestNeighborUpdateStatus(int value, String name) {
        this.value = (byte) value;
        this.name = name;
    }

    public static RequestNeighborUpdateStatus from(int value) {
        for (RequestNeighborUpdateStatus status : RequestNeighborUpdateStatus.values()) {
            if (status.value == (byte) value) {
                return status;
            }
        }
        return UNKNOWN;
    }

    @Override
    public String toString() {
        return String.format("{\"Request.Neighbor.Update.Status\":{\"name\": %s}}", name);
    }
}
