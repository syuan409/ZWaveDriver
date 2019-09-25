package org.imdc.zwavedriver.zwave.messages.framework;

import org.imdc.zwavedriver.zwave.messages.Messages;

import java.util.Objects;

public class MessageType {
    private final Messages message;
    private final Message.Type type;

    public MessageType(Messages message, Message.Type type) {
        this.message = message;
        this.type = type;
    }

    public Messages getMessage() {
        return message;
    }

    public Message.Type getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageType messageType = (MessageType) o;
        return message == messageType.message &&
                type == messageType.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, type);
    }

    @Override
    public String toString() {
        return String.format("{\"Message.Type\":{\"message\": %s, \"type\": %s}}", message.toString(), type.toString());
    }
}
