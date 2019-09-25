package org.imdc.zwavedriver.zwave.messages.commandclasses.objects;

import org.imdc.zwavedriver.zwave.ByteUtilities;
import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;

import java.io.ByteArrayInputStream;

public class ParameterProperty {
    public final int parameterNumber;
    public final int minValue, maxValue, defaultValue;
    public final int nextParameterNumber;
    public final Format format;
    public final Size size;

    public ParameterProperty(ByteArrayInputStream in) throws DecoderException {
        parameterNumber = ByteUtilities.readInt16MSB(in);
        byte firstByte = (byte) in.read();
        size = Size.from(firstByte & 0x07);
        format = Format.from((firstByte >> 3) & 0x07);
        minValue = ByteUtilities.readIntMSB(in, size);
        maxValue = ByteUtilities.readIntMSB(in, size);
        defaultValue = ByteUtilities.readIntMSB(in, size);
        nextParameterNumber = ByteUtilities.readInt16MSB(in);
    }

    @Override
    public String toString() {
        return String.format("{\"Parameter.Property\": {\"parameter\": %d, \"format\": %s, \"minValue\": %d, \"maxValue\": %d, \"defaultValue\": %d)", parameterNumber, format, format.intToString(minValue), format.intToString(maxValue), format.intToString(defaultValue));
    }
}
