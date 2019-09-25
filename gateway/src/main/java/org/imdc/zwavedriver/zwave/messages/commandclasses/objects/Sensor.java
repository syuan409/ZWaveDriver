package org.imdc.zwavedriver.zwave.messages.commandclasses.objects;

import org.imdc.zwavedriver.zwave.ByteUtilities;
import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;

import java.io.ByteArrayInputStream;

public class Sensor {
    public final SensorType type;
    public final SensorUnit unit;
    public final Size size;
    public final byte precision;
    public final double value;

    public Sensor(ByteArrayInputStream in) throws DecoderException {
        type = SensorType.from(in.read());
        int dimensions = in.read();
        precision = (byte) (dimensions >> 5);
        unit = SensorUnit.fromMeterScale(type, (dimensions >> 3) & 0x03);
        size = Size.from(dimensions & 0x07);
        value = ByteUtilities.readIntMSBPrecision(in, size, precision);
    }

    @Override
    public String toString() {
        return String.format("{\"Sensor\":{\"type\": %s, \"unit\": %s, \"value\": %f}}", type.toString(), unit.toString(), value);
    }
}
