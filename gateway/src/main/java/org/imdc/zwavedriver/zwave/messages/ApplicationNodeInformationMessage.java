package org.imdc.zwavedriver.zwave.messages;

import org.imdc.zwavedriver.zwave.messages.framework.*;
import org.imdc.zwavedriver.zwave.messages.framework.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ApplicationNodeInformationMessage {

    private static final MessageType REQUEST = new MessageType(Messages.APPLICATION_NODE_INFORMATION, Message.Type.REQUEST);

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

        @Override
        protected void addRequestData(ByteArrayOutputStream result) throws IOException {
            super.addRequestData(result);
            result.write(0x01);
            result.write(0x02);
            result.write(0x01);
            result.write(0x00);
        }
    }

    public static class Response extends MessageAdaptor {
        public Response(byte[] message) throws DecoderException {
            super(message);
        }

        @Override
        public String toString() {
            return String.format("{\"ApplicationNodeInformationMessage.Response\":{}");
        }
    }

}
