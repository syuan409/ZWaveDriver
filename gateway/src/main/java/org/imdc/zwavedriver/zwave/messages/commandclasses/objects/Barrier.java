package org.imdc.zwavedriver.zwave.messages.commandclasses.objects;

import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;

public class Barrier {

    public byte value;
    public BarrierState state;

    public Barrier(BarrierState state) {
        this.state = state;
        this.value = state.value;
    }

    public Barrier(int value) throws DecoderException {
        this.value = (byte) value;
        this.state = BarrierState.from(value);
    }

    @Override
    public String toString() {
        return String.format("{\"Barrier\":{\"state\": %s, \"value\": %d}}", state.toString(), value);
    }
}
