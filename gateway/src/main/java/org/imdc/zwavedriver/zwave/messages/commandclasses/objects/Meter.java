package org.imdc.zwavedriver.zwave.messages.commandclasses.objects;

import org.imdc.zwavedriver.zwave.ByteUtilities;
import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;

import java.io.ByteArrayInputStream;

public class Meter {
    public final MeterType meterType;
    public final RateType rateType;
    public final MeterUnit meterUnit;
    public final Size size;
    public final byte precision;
    public final double value;
    public Integer deltaTime;
    public Double previousValue;

    public Meter(ByteArrayInputStream in) throws DecoderException {
        int b = in.read();
        rateType = RateType.from((b >> 5) & 0x03);
        meterType = MeterType.from(b & 0x1F);
        int dimensions = in.read();
        precision = (byte) (dimensions >> 5);
        meterUnit = MeterUnit.fromMeterScale(meterType, ((dimensions >> 3) & 0x03) | ((b >> 7) << 2));
        size = Size.from(dimensions & 0x07);
        value = ByteUtilities.readIntMSBPrecision(in, size, precision);
        deltaTime = ByteUtilities.readInt16MSB(in);
        if (deltaTime > 0) {
            previousValue = ByteUtilities.readIntMSBPrecision(in, size, precision);
        }

        if (deltaTime == 0xFFFF || deltaTime == 0) {
            deltaTime = null;
        }
    }

    @Override
    public String toString() {
        return String.format("{\"Meter\":{\"type\": %s, \"unit\": %s, \"value\": %f}}", meterType.toString(), meterUnit.toString(), value);
    }
}
