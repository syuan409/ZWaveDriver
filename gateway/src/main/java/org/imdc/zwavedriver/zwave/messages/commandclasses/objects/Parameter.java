package org.imdc.zwavedriver.zwave.messages.commandclasses.objects;

import org.imdc.zwavedriver.zwave.ByteUtilities;
import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Configuration parameters are numeric values used to configure the behaviour of nodes. They are read and set
 * with the Configuration command class.
 */
public class Parameter {
    public final int parameterNumber;
    public final int value;
    public final Size size;

    public Parameter(int parameterNumber, int value, Size size) {
        this.parameterNumber = parameterNumber;
        this.value = value;
        this.size = size;
    }

    public Parameter(ByteArrayInputStream in) throws DecoderException {
        parameterNumber = in.read();
        size = Size.from(in.read());
        value = ByteUtilities.readIntMSB(in, size);
    }

    public void write(ByteArrayOutputStream result) {
        result.write(parameterNumber);
        result.write(size.value);
        ByteUtilities.writeIntMSB(result, size, value);
    }

    @Override
    public String toString() {
        return String.format("{\"Parameter\": {\"number\": %d, \"value\": %d, \"size\": %s}}", parameterNumber, value, size.name);
    }
}
