package org.imdc.zwavedriver.zwave.messages;

import org.imdc.zwavedriver.gateway.ZWavePath;
import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import org.imdc.zwavedriver.zwave.messages.framework.Message;
import org.imdc.zwavedriver.zwave.messages.framework.MessageAdaptor;
import org.imdc.zwavedriver.zwave.messages.framework.MessageCommandProcessorAdapter;
import org.imdc.zwavedriver.zwave.messages.framework.MessageType;
import com.inductiveautomation.ignition.common.sqltags.model.types.DataType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class IsFailedNodeMessage {

    private static final MessageType REQUEST = new MessageType(Messages.IS_FAILED_NODE, Message.Type.REQUEST);

    public static class Processor extends MessageCommandProcessorAdapter {
        @Override
        public Message process(MessageType messageType, byte[] message) throws DecoderException {
            return new Response(message);
        }
    }

    public static class Request extends MessageAdaptor {
        public Request(int nodeId) {
            super(REQUEST, nodeId);
        }

        @Override
        protected void addRequestData(ByteArrayOutputStream result) throws IOException {
            super.addRequestData(result);
            result.write(getNodeId());
        }
    }

    public static class Response extends MessageAdaptor {
        public final boolean isFailed;

        public Response(byte[] message) throws DecoderException {
            super(message);
            isFailed = in.read() != 0;
        }

        @Override
        public void update() {
            ZWavePath path = getPath(getNodeId());
            if (isFailed) {
                configureTagInitValue(path.tag("RemoveFailedNode"), DataType.Boolean, false, true);
                configureTagInitValue(path.tag("ReplaceFailedNode"), DataType.Boolean, false, true);
                updateStatusTag(path, "Failed");
            } else {
                removeTag(path.tag("RemoveFailedNode"));
                removeTag(path.tag("ReplaceFailedNode"));
            }
        }

        @Override
        public String toString() {
            return String.format("{\"IsFailedNodeMessage.Response\": {\"isFailed\": %b}}", isFailed);
        }
    }
}
