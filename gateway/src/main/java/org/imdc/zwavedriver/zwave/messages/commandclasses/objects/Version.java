package org.imdc.zwavedriver.zwave.messages.commandclasses.objects;

import java.io.ByteArrayInputStream;

public class Version {
    public final byte version;
    public final byte subVersion;

    public Version(int version, int subVersion) {
        this.version = (byte) version;
        this.subVersion = (byte) subVersion;
    }

    public Version(ByteArrayInputStream in) {
        version = (byte) in.read();
        subVersion = (byte) in.read();
    }

    public String getVersion() {
        return String.format("%d.%d", version, subVersion);
    }

    @Override
    public String toString() {
        return String.format("{\"Version\":{\"version\": %d, \"subVersion\": %d}}", version, subVersion);
    }
}
