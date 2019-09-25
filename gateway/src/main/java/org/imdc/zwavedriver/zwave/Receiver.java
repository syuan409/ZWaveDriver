package org.imdc.zwavedriver.zwave;

public interface Receiver {
    void receiveMessage(byte[] message);

    void receiveFrameByte(byte frameByte);
}
