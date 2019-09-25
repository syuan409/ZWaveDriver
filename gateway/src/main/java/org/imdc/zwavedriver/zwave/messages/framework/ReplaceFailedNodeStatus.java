package org.imdc.zwavedriver.zwave.messages.framework;

public enum ReplaceFailedNodeStatus {
    OK(0x00, "Started"),
    WAITING(0x03, "Started"),
    DONE(0x04, "Done"),
    FAILED(0x05, "Failed"),
    UNKNOWN(0xFF, "Unknown");

    public final byte value;
    public final String name;

    ReplaceFailedNodeStatus(int value, String name) {
        this.value = (byte) value;
        this.name = name;
    }

    public static ReplaceFailedNodeStatus from(int value) {
        for (ReplaceFailedNodeStatus status : ReplaceFailedNodeStatus.values()) {
            if (status.value == (byte) value) {
                return status;
            }
        }
        return UNKNOWN;
    }

    @Override
    public String toString() {
        return String.format("{\"Replace.Failed.Node.Status\":{\"name\": %s}}", name);
    }
}
