package org.imdc.zwavedriver.zwave.messages;

import org.imdc.zwavedriver.gateway.ZWavePath;
import org.imdc.zwavedriver.zwave.ByteUtilities;
import org.imdc.zwavedriver.zwave.Hex;
import org.imdc.zwavedriver.zwave.messages.commandclasses.objects.Size;
import org.imdc.zwavedriver.zwave.messages.framework.ControllerMode;
import org.imdc.zwavedriver.zwave.messages.framework.ControllerType;
import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import org.imdc.zwavedriver.zwave.messages.framework.Message;
import org.imdc.zwavedriver.zwave.messages.framework.MessageAdaptor;
import org.imdc.zwavedriver.zwave.messages.framework.MessageCommandProcessorAdapter;
import org.imdc.zwavedriver.zwave.messages.framework.MessageType;
import com.inductiveautomation.ignition.common.sqltags.model.types.DataType;

/*
 * event,ZWave_Message,Direction,Out,Value,0020
 */
public class MemoryGetIdMessage {

    private static final MessageType REQUEST = new MessageType(Messages.MEMORY_GET_ID, Message.Type.REQUEST);

    public static class Processor extends MessageCommandProcessorAdapter {
        @Override
        public Message process(MessageType messageType, byte[] message) throws DecoderException {
            return new Response(message);
        }
    }

    public static class Request extends MessageAdaptor {
        public Request() {
            super(REQUEST);
        }
    }

    public static class Response extends MessageAdaptor {
        public final int homeId;
        public final byte nodeId;

        public Response(byte[] message) throws DecoderException {
            super(message);

            homeId = ByteUtilities.readIntMSB(in, Size.BIT32);
            nodeId = (byte) in.read();
        }

        @Override
        public void update() {
            if(!addHome(homeId, nodeId)){
                ZWavePath path = getPath();

                configureTagInitValue(path.tag("Lookup", "ControllerModes"), DataType.DataSet, ControllerMode.buildDS());
                configureTagInitValue(path.tag("Lookup", "ControllerTypes"), DataType.DataSet, ControllerType.buildDS());

                configureTagInitValue(path.tag("ControllerId"), DataType.String, Hex.asString(nodeId));
                configureTag(path.tag("ControllerMode"), DataType.String);
                configureTag(path.tag("ControllerType"), DataType.String);
                configureTagInitValue(path.tag("AddNode"), DataType.Boolean, false, true);
                configureTagInitValue(path.tag("AddSecureNode"), DataType.Boolean, false, true);
                configureTagInitValue(path.tag("RemoveNode"), DataType.Boolean, false, true);
                configureTagInitValue(path.tag("QueueSize"), DataType.Int4, 0);
                configureTagInitValue(path.tag("CurrentMessage"), DataType.String, getMessageId());
                configureTagInitValue(path.tag("HealNetwork"), DataType.Boolean, false, true);
                configureTagInitValue(path.tag("Reinitialize"), DataType.Boolean, false, true);

                sendMessage(new GetInitDataMessage.Request());
            }
        }

        @Override
        public String toString() {
            return String.format("{\"MemoryGetIdMessage.Response\": {\"homeId\": %s, \"nodeId\": %s}}", Hex.asString(homeId), Hex.asString(nodeId));
        }
    }
}
