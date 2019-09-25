package org.imdc.zwavedriver.zwave.messages;

import org.imdc.zwavedriver.zwave.Hex;
import org.imdc.zwavedriver.zwave.messages.commandclasses.CommandClasses;
import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import org.imdc.zwavedriver.zwave.messages.framework.Message;
import org.imdc.zwavedriver.zwave.messages.framework.MessageCommandProcessorAdapter;

import java.util.Comparator;

public enum Messages {
    MEMORY_GET_ID(0x20, new MemoryGetIdMessage.Processor(), 1),
    GET_VERSION(0x15, new GetVersionMessage.Processor(), 2),
    GET_INIT_DATA(0x02, new GetInitDataMessage.Processor(), 3),
    APPLICATION_NODE_INFORMATION(0x03, new ApplicationNodeInformationMessage.Processor(), 4),
    IDENTIFY_NODE(0x41, new IdentifyNodeMessage.Processor(), 5, true, 1),
    GET_ROUTING_INFO(0x80, new GetRoutingInfoMessage.Processor(), 5, true, 2),
    NO_OPERATION(0x13, new SendDataMessage.Processor(), 5, true, 3),
    REQUEST_NODE_INFO(0x60, new RequestNodeInfoMessage.Processor(), 5, true, 4),
    IS_FAILED_NODE(0x62, new IsFailedNodeMessage.Processor(), 6),
    APPLICATION_UPDATE(0x49, new ApplicationUpdateMessage.Processor(), 7),
    APPLICATION_COMMAND(0x04, new ApplicationCommandMessage.Processor(), 8),
    ADD_NODE(0x4A, new AddNodeMessage.Processor(), 9),
    REMOVE_NODE(0x4B, new RemoveNodeMessage.Processor(), 10),
    REQUEST_NODE_NEIGHBOR_UPDATE(0x48, new RequestNodeNeighborUpdate.Processor(), 11),
    DELETE_RETURN_ROUTE(0x47, new DeleteAllReturnRoutes.Processor(), 12),
    ASSIGN_RETURN_ROUTE(0x46, new AssignReturnRoute.Processor(), 13),
    REMOVE_FAILED_NODE(0x61, new RemoveFailedNodeMessage.Processor(), 14),
    REPLACE_FAILED_NODE(0x63, new ReplaceFailedNodeMessage.Processor(), 15),
    SEND_DATA(0x13, new SendDataMessage.Processor(), 16);

    private final byte messageId;
    private final MessageCommandProcessorAdapter processor;
    private final int priority, subPriority;
    private final boolean sortNode;

    Messages(int messageId, MessageCommandProcessorAdapter processor, int priority) {
        this(messageId, processor, priority, false, 1);
    }

    Messages(int messageId, MessageCommandProcessorAdapter processor, int priority, boolean sortNode, int subPriority) {
        this.messageId = (byte) messageId;
        this.processor = processor;
        this.priority = priority;
        this.subPriority = subPriority;
        this.sortNode = sortNode;
    }

    public byte getValue() {
        return messageId;
    }

    public String getHex() {
        return Hex.asString(messageId);
    }

    public MessageCommandProcessorAdapter getProcessor() {
        return processor;
    }

    public int getPriority() {
        return priority;
    }

    public int getSubPriority() {
        return subPriority;
    }

    public boolean isSortNode() {
        return sortNode;
    }

    public static Messages from(int messageId) throws DecoderException {
        for (Messages m : Messages.values()) {
            if (m.getValue() == (byte) messageId) {
                return m;
            }
        }
        throw new DecoderException("Unknown Message");
    }

    public static class MessageComparator implements Comparator<Message> {
        public int compare(Message m1, Message m2) {
            if (m1.hasPriority() && !m2.hasPriority()) {
                return -1;
            } else if (m2.hasPriority() && !m1.hasPriority()) {
                return 1;
            } else {
                if (m1.getMessageType().getMessage().getPriority() == m2.getMessageType().getMessage().getPriority() && m1.getMessageType().getMessage().isSortNode() && m1.getMessageType().getMessage().isSortNode() && m1.getNodeId() == m2.getNodeId()) {
                    return Integer.compare(m1.getMessageType().getMessage().getSubPriority(), m2.getMessageType().getMessage().getSubPriority());
                } else if (m1.getMessageType().getMessage().getPriority() == m2.getMessageType().getMessage().getPriority() && m1.getMessageType().getMessage().isSortNode() && m1.getMessageType().getMessage().isSortNode()) {
                    return Byte.compare(m1.getNodeId(), m2.getNodeId());
                } else if (m1.getMessageType().getMessage() == SEND_DATA && m2.getMessageType().getMessage() == SEND_DATA) {
                    CommandClasses.CommandClassComparator ccc = new CommandClasses.CommandClassComparator();
                    return ccc.compare(((SendDataMessage.Request) m1).command.getCommandCode().getCommandClass(), ((SendDataMessage.Request) m2).command.getCommandCode().getCommandClass());
                }
                return Integer.compare(m1.getMessageType().getMessage().getPriority(), m2.getMessageType().getMessage().getPriority());
            }
        }
    }

    @Override
    public String toString() {
        return String.format("{\"Message\":{\"name\": %s, \"id\": %s}}", name(), Hex.asString(messageId));
    }
}
