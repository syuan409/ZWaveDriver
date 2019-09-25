package org.imdc.zwavedriver.zwave.messages.framework;

public enum RemoveFailedNodeStatus {
    OK(0x00, "Started"),
    REMOVED(0x01, "Done"),
    FAILED(0x02, "Failed"),
    UNKNOWN(0xFF, "Unknown");

    public final byte value;
    public final String name;

    RemoveFailedNodeStatus(int value, String name) {
        this.value = (byte) value;
        this.name = name;
    }

    public static RemoveFailedNodeStatus from(int value) {
        for (RemoveFailedNodeStatus status : RemoveFailedNodeStatus.values()) {
            if (status.value == (byte) value) {
                return status;
            }
        }
        return UNKNOWN;
    }

    @Override
    public String toString() {
        return String.format("{\"Remove.Failed.Node.Status\":{\"name\": %s}}", name);
    }
}
