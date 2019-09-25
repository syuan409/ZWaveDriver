package org.imdc.zwavedriver.zwave.messages.framework;

import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.Command;

import java.util.Date;

public interface Message {
    enum Type {REQUEST, RESPONSE}
    enum Status {UNKNOWN, FAILED, SUCCESS}

    MessageType getMessageType();

    Command getCommand();

    void setCommand(Command command);

    byte getNodeId();

    void setNodeId(int nodeId);

    String getMessageId();

    byte[] encode();

    void setPriority(boolean priority);

    boolean hasPriority();

    void sent();

    Date getSentDate();

    Status getStatus();

    void failed();

    void success();

    void update();
}
