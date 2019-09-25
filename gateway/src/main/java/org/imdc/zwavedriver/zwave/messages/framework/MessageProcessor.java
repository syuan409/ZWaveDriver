package org.imdc.zwavedriver.zwave.messages.framework;

import org.imdc.zwavedriver.zwave.messages.Messages;

public interface MessageProcessor {
    Message getNextMessage();

    boolean allowProcess();

    boolean allowProcessMessage(Messages message);

    void process(Message message);
}
