package org.imdc.zwavedriver.zwave.messages;

import org.imdc.zwavedriver.gateway.ZWavePath;
import org.imdc.zwavedriver.zwave.Hex;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.Command;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandArgument;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.MultiCommandProcessor;
import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import org.imdc.zwavedriver.zwave.messages.framework.Message;
import org.imdc.zwavedriver.zwave.messages.framework.MessageAdaptor;
import org.imdc.zwavedriver.zwave.messages.framework.MessageCommandProcessorAdapter;
import org.imdc.zwavedriver.zwave.messages.framework.MessageType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;

public class ApplicationCommandMessage {

    private static final MessageType REQUEST = new MessageType(Messages.APPLICATION_COMMAND, Message.Type.REQUEST);

    public static class Processor extends MessageCommandProcessorAdapter {
        @Override
        public Message process(MessageType messageType, byte[] message) throws DecoderException {
            return new Request(message, getCommandProcessor());
        }
    }

    public static class Request extends MessageAdaptor {
        public final byte nodeId;
        public final Command command;

        public Request(byte[] data, MultiCommandProcessor processor) throws DecoderException {
            super(data);

            try {
                in.read(); // ?? Seems to be zero
                nodeId = (byte) in.read();
                setNodeId(nodeId);
                int commandLength = in.read();
                byte[] commandData = new byte[commandLength];
                in.read(commandData);
                command = processor.process(new CommandArgument(nodeId), commandData);
                setCommand(command);
            } catch (IOException e) {
                throw new DecoderException(e.getMessage());
            }
        }

        @Override
        protected void addRequestData(ByteArrayOutputStream result) throws IOException {
            byte[] commandData = command.encode();
            super.addRequestData(result);
            result.write(0);
            result.write(nodeId);
            result.write(commandData.length);
            result.write(commandData);
        }

        @Override
        public String getMessageId() {
            return String.format("node-%s-msg-cc-%s", Hex.asString(nodeId), command.getCommandCode().getCommandClass().getHex());
        }

        @Override
        public void update() {
            if (command == null) {
                return;
            }

            if (command.process()) {
                ZWavePath path = getPath(nodeId);
                updateStatusTag(path, "Connected");

                path.setCommandClass(command.getCommandClass().getValue());
                boolean secure = isSecure(path);
                int version = getVersion(path);

                if(command.getEncapsulatedCommand() != null) {
                    command.getEncapsulatedCommand().update(path, version, secure);
                } else {
                    command.update(path, version, secure);
                }

                updateTag(path.tag("LastUpdate"), new Date());
            }
        }

        @Override
        public String toString() {
            return String.format("{\"ApplicationCommandMessage.Request\":{\"node\": %s, \"command\": %s}}", Hex.asString(nodeId), command.toString());
        }
    }
}
