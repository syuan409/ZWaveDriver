package org.imdc.zwavedriver.zwave.messages;

import org.imdc.zwavedriver.gateway.ZWavePath;
import org.imdc.zwavedriver.zwave.ByteUtilities;
import org.imdc.zwavedriver.zwave.messages.framework.*;
import com.inductiveautomation.ignition.common.sqltags.model.types.DataType;
import org.imdc.zwavedriver.zwave.messages.framework.*;

/*
 * event,ZWave_Message,Direction,Out,Value,0020
 */
public class GetVersionMessage {

    private static final MessageType REQUEST = new MessageType(Messages.GET_VERSION, Message.Type.REQUEST);

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
        public final String version;
        public final LibraryType libraryType;

        public Response(byte[] message) throws DecoderException {
            super(message);

            version = ByteUtilities.readString(in,  messageLength - 1);
            libraryType = LibraryType.from((byte) in.read());
        }

        @Override
        public void update() {
           ZWavePath path = getPath();

           configureTagInitValue(path.tag("Version"), DataType.String, version);
           configureTagInitValue(path.tag("LibraryType"), DataType.String, libraryType.name);
        }

        @Override
        public String toString() {
            return String.format("{\"GetVersionMessage.Response\": {\"version\": %s, \"libraryType\": %s}}", version, libraryType.toString());
        }
    }
}
