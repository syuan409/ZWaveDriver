package org.imdc.zwavedriver.zwave;

import org.imdc.zwavedriver.zwave.messages.framework.Message;
import org.imdc.zwavedriver.zwave.messages.framework.MessageProcessor;

public interface ZWavePort {
    void setReceiver(MessageProcessor receiver);

    void sendMessage(Message message);

    boolean isOpen();

    void shutdown();
}
