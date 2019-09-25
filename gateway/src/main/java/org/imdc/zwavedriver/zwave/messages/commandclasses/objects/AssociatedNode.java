package org.imdc.zwavedriver.zwave.messages.commandclasses.objects;

import org.imdc.zwavedriver.zwave.Hex;

/**
 * An AssociatedNode is a reference to another node used in association references, that is when one node
 * holds an association to another node. The association could either be a simple node number, referring to the
 * node id of the associated node, but it can also contain a reference to an specific endpoint (or instance)
 * within that node. Associations without support for instances are handled by the AssociationCommandClass and
 * associations with instances are handled by the MultiInstanceAssociationCommandClass.
 */
public class AssociatedNode {
    public final byte nodeId;
    public final Byte instance;

    public boolean isMultiInstance() {
        return instance != null;
    }

    public AssociatedNode(int nodeId, int instance) {
        this.nodeId = (byte) nodeId;
        this.instance = (byte) instance;
    }

    public AssociatedNode(byte nodeId) {
        this.nodeId = nodeId;
        this.instance = null;
    }

    @Override
    public String toString() {
        return "" + Hex.asString(nodeId) + (isMultiInstance() ? ("." + Hex.asString(instance)) : "");
    }
}
