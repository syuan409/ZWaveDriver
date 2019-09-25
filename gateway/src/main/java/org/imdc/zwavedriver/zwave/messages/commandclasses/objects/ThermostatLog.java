package org.imdc.zwavedriver.zwave.messages.commandclasses.objects;

import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;

import java.io.ByteArrayInputStream;

public class ThermostatLog {
    public final ThermostatOperatingState state;
    public final byte todayUsageHours;
    public final byte todayUsageMinutes;
    public final byte yesterdayUsageHours;
    public final byte yesterdayUsageMinutes;

    public ThermostatLog(ByteArrayInputStream in) throws DecoderException {
        state = ThermostatOperatingState.from(in.read());
        todayUsageHours = (byte) in.read();
        todayUsageMinutes = (byte) in.read();
        yesterdayUsageHours = (byte) in.read();
        yesterdayUsageMinutes = (byte) in.read();
    }

    @Override
    public String toString() {
        return String.format("{\"Thermostat.Log\":{\"state\": %s, \"today\": %02d:%02d, \"yesterday\": %02d:%02d}}", state.toString(), todayUsageHours, todayUsageMinutes, yesterdayUsageHours, yesterdayUsageMinutes);
    }
}
