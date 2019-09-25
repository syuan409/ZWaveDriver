package org.imdc.zwavedriver.zwave.messages.commandclasses.framework;

/**
 * Arguments to an application Command. Contains the node that issued the command and optionally the instance
 * in the target node that the command is intended for.
 */
public class CommandArgument {
    final private byte sourceNode;
    final private Byte targetInstance;

    public CommandArgument(byte sourceNode, Byte targetInstance) {
        this.sourceNode = sourceNode;
        this.targetInstance = targetInstance;
    }

    public CommandArgument(byte sourceNode) {
        this.sourceNode = sourceNode;
        this.targetInstance = null;
    }

    boolean hasInstance() {
        return targetInstance != null;
    }

    public byte getNode() {
        return sourceNode;
    }
}
