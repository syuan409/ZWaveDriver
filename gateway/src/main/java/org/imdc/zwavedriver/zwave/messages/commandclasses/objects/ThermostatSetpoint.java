package org.imdc.zwavedriver.zwave.messages.commandclasses.objects;

import org.imdc.zwavedriver.zwave.ByteUtilities;
import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class ThermostatSetpoint {
    public final ThermostatSetpointType type;
    public final SensorUnit unit;
    public final Size size;
    public final byte precision;
    public final double value;

    public ThermostatSetpoint(ThermostatSetpointType type, SensorUnit unit, double value) {
        this.type = type;
        this.unit = unit;
        this.value = value;
        this.precision = 0x02;
        this.size = Size.BIT16;
    }

    public ThermostatSetpoint(ByteArrayInputStream in) throws DecoderException {
        int b = in.read();
        type = ThermostatSetpointType.from(b & 0x0F);
        int dimensions = in.read();
        precision = (byte) (dimensions >> 5);
        unit = SensorUnit.fromMeterScale(SensorType.TEMPERATURE, (byte) ((dimensions >> 3) & 0x03));
        size = Size.from(dimensions & 0x07);
        value = ByteUtilities.readIntMSBPrecision(in, size, precision);
    }

    public void write(ByteArrayOutputStream result) {
        result.write(type.value);
        result.write((precision << 5) | ((unit.scale << 3) & 0x03) | (size.value & 0x07));
        ByteUtilities.writeDouble(result, size, precision, value);
    }

    @Override
    public String toString() {
        return String.format("{\"Setpoint\":{\"type\": %s, \"unit\": %s, \"value\": %f}}", type.toString(), unit.toString(), value);
    }
}
