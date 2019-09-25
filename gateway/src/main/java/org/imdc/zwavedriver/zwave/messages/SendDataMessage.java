package org.imdc.zwavedriver.zwave.messages;

import org.imdc.zwavedriver.zwave.Hex;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.Command;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandArgument;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.MultiCommandProcessor;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.UndecodedCommand;
import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import org.imdc.zwavedriver.zwave.messages.framework.Message;
import org.imdc.zwavedriver.zwave.messages.framework.MessageAdaptor;
import org.imdc.zwavedriver.zwave.messages.framework.MessageCommandProcessorAdapter;
import org.imdc.zwavedriver.zwave.messages.framework.MessageType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SendDataMessage {

    private static final MessageType REQUEST = new MessageType(Messages.SEND_DATA, Message.Type.REQUEST);
    public static final int TRANSMIT_OPTION_ACK = 0x01;
    public static final int TRANSMIT_OPTION_LOW_POWER = 0x02;
    public static final int TRANSMIT_OPTION_AUTO_ROUTE = 0x04;
    public static final int TRANSMIT_OPTION_NO_ROUTE = 0x10;
    public static final int TRANSMIT_OPTION_EXPLORE = 0x20;
    public static final int TRANSMIT_OPTIONS_ALL = TRANSMIT_OPTION_ACK | TRANSMIT_OPTION_AUTO_ROUTE | TRANSMIT_OPTION_EXPLORE;

    public static class Processor extends MessageCommandProcessorAdapter {
        @Override
        public Message process(MessageType messageType, byte[] message) throws DecoderException {
            MessageAdaptor object;
            if (messageType.getType() == Message.Type.REQUEST) {
                object = new Request(message, getCommandProcessor());
            } else {
                object = new Response(message);
            }
            return object;
        }
    }

    private static int nextCallbackId = 1;

    public static class Request extends MessageAdaptor {
        public final byte nodeId;
        public Command command;
        public final int transmitOptions;
        public final int callbackId;

        public Request(int nodeId, Command command) {
            this(nodeId, command, TRANSMIT_OPTIONS_ALL);
        }

        public Request(int nodeId, Command command, int transmitOptions) {
            this(REQUEST, nodeId, command, transmitOptions);
        }

        public Request(MessageType messageType, int nodeId, Command command, int transmitOptions) {
            super(messageType, nodeId);

            this.nodeId = (byte) nodeId;
            this.command = command;
            this.transmitOptions = transmitOptions;
            callbackId = nextCallbackId;
            if (nextCallbackId == Integer.MAX_VALUE) {
                nextCallbackId = 0;
            } else {
                nextCallbackId = (nextCallbackId + 1);
            }
        }

        public Request(byte[] message, MultiCommandProcessor processor) throws DecoderException {
            super(message);

            nodeId = (byte) in.read();
            setNodeId(nodeId);
            int commandLength = in.read();

            try {
                byte[] commandData = new byte[commandLength];
                in.read(commandData);
                command = processor.process(new CommandArgument(nodeId), commandData);
            } catch (Exception e) {
                command = new UndecodedCommand(new byte[0]);
            }

            setCommand(command);

            transmitOptions = (byte) in.read();
            callbackId = (byte) in.read();
        }

        @Override
        protected void addRequestData(ByteArrayOutputStream result) throws IOException {
            super.addRequestData(result);
            byte[] commandData = command.encode();
            result.write(nodeId);
            result.write(commandData.length);
            result.write(commandData);
            result.write(transmitOptions);
            result.write(callbackId);
        }

        @Override
        public String getMessageId() {
            return String.format("node-%s-msg-cc-%s", Hex.asString(nodeId), command.getCommandCode().getCommandClass().getHex());
        }

        @Override
        public void sent() {
            super.sent();

            if(command != null){
                command.sent(nodeId);
            }
        }

        @Override
        public String toString() {
            return String.format("{\"SendDataMessage.Request\": {\"node\": %s, \"command\":%s, \"transmitOptions\": %d, \"callbackId\": %d}}", Hex.asString(nodeId), command.toString(), transmitOptions, callbackId);
        }
    }

    public static class Response extends MessageAdaptor {

        public final byte callbackId;
        public final Byte status;

        public Response(byte[] message) throws DecoderException {
            super(message);

            callbackId = (byte) in.read();
            if (message.length > 3) {
                status = (byte) in.read();
            } else {
                status = null;
            }
        }

        @Override
        public String toString() {
            return String.format("{\"SendDataMessage.Response\": {\"callbackId\": %d, \"status\": %d}}", callbackId, status == null ? -1 : status);
        }
    }
}
