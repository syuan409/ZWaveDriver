package org.imdc.zwavedriver.zwave.messages.commandclasses;

import org.imdc.zwavedriver.gateway.ZWavePath;
import org.imdc.zwavedriver.zwave.ByteUtilities;
import org.imdc.zwavedriver.zwave.Hex;
import org.imdc.zwavedriver.zwave.messages.ApplicationUpdateMessage;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.Command;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandAdapter;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandArgument;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandClass;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandClassVersion;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandCode;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandProcessorAdapter;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.MultiCommandProcessor;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.MultipleReportsCommandAdapter;
import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Security0CommandClass implements CommandClass {

    private static final CommandCode COMMANDS_GET = new CommandCode(CommandClasses.SECURITY0, 0x02);
    private static final CommandCode COMMANDS_REPORT = new CommandCode(CommandClasses.SECURITY0, 0x03);
    private static final CommandCode SCHEME_GET = new CommandCode(CommandClasses.SECURITY0, 0x04);
    private static final CommandCode SCHEME_REPORT = new CommandCode(CommandClasses.SECURITY0, 0x05);
    private static final CommandCode NETWORK_KEY_SET = new CommandCode(CommandClasses.SECURITY0, 0x06);
    private static final CommandCode NETWORK_KEY_VERIFY = new CommandCode(CommandClasses.SECURITY0, 0x07);
    private static final CommandCode SCHEME_INHERIT = new CommandCode(CommandClasses.SECURITY0, 0x08);
    private static final CommandCode NONCE_GET = new CommandCode(CommandClasses.SECURITY0, 0x40);
    public static final CommandCode NONCE_REPORT = new CommandCode(CommandClasses.SECURITY0, 0x80);
    public static final CommandCode ENCAPSULATION = new CommandCode(CommandClasses.SECURITY0, 0x81);
    private static final CommandCode ENCAPSULATION_NONCE_GET = new CommandCode(CommandClasses.SECURITY0, 0xC1);

    public static class Processor extends CommandProcessorAdapter {
        @Override
        public CommandCode[] getCommandCodes() {
            return new CommandCode[]{COMMANDS_GET, COMMANDS_REPORT, SCHEME_GET, SCHEME_REPORT, NETWORK_KEY_SET, NETWORK_KEY_VERIFY, SCHEME_INHERIT, NONCE_GET, NONCE_REPORT, ENCAPSULATION, ENCAPSULATION_NONCE_GET};
        }

        @Override
        public Command processCommandData(CommandCode commandCode, CommandArgument argument, byte[] commandData) throws DecoderException {
            CommandAdapter object = null;

            if (commandCode.equals(NONCE_GET)) {
                object = new GetNonce();
                ReportNonce externalNonce = new ReportNonce();
                storeCommand(argument.getNode(), externalNonce);
                sendCommand(argument.getNode(), externalNonce, false, true);
            } else if (commandCode.equals(NONCE_REPORT)) {
                object = new ReportNonce(commandData);
                storeCommand(argument.getNode(), object);
                CommandAdapter command = getStoredCommand(argument.getNode(), ENCAPSULATION);
                if (command != null) {
                    sendCommand(argument.getNode(), new Encapsulation(((ReportNonce) object).nonce, argument.getNode(), command), false, true);
                    clearStoredObject(argument.getNode(), ENCAPSULATION);
                }
            } else if (Arrays.asList(ENCAPSULATION, ENCAPSULATION_NONCE_GET).contains(commandCode)) {
                ReportNonce externalNonce = (ReportNonce) getStoredCommand(argument.getNode(), NONCE_REPORT);
                object = new Encapsulation(commandData, externalNonce.nonce, argument, getMultiCommandProcessor());

                if (((Encapsulation) object).hasReportsToFollow()) {
                    externalNonce = new ReportNonce();
                    storeCommand(argument.getNode(), externalNonce);
                    sendCommand(argument.getNode(), externalNonce, false, true);
                }
            } else if (Arrays.asList(COMMANDS_GET, COMMANDS_REPORT).contains(commandCode)) {
                object = new ReportCommands(commandData);
            } else if (Arrays.asList(SCHEME_GET, SCHEME_REPORT).contains(commandCode)) {
                object = new ReportScheme(commandData);
                sendCommand(argument.getNode(), new SetNetworkKey(getNetworkKey()), true, true);
            } else if (commandCode.equals(NETWORK_KEY_VERIFY)) {
                object = new VerifyNetworkKey(commandData);
                addNode(argument.getNode());
            }

            return object;
        }

        @Override
        public void queueInitialMessages(ZWavePath path, int version, boolean secure, boolean initial) {
            sendCommand(path, new GetCommands(), true);
        }
    }

    public static class GetNonce extends CommandAdapter {
        @CommandClassVersion(CommandClassVersion.Version.V1)
        public GetNonce() {
            super(NONCE_GET);
        }

        @CommandClassVersion(CommandClassVersion.Version.V1)
        public GetNonce(int nodeId, CommandAdapter command) {
            super(NONCE_GET);
            setEncapsulatedCommand(command);
        }

        @Override
        public void sent(byte nodeId) {
            if (getEncapsulatedCommand() != null) {
                CommandClasses.SECURITY0.getProcessor().storeCommand(nodeId, Security0CommandClass.ENCAPSULATION, (CommandAdapter) getEncapsulatedCommand());
            }
        }

        @Override
        public String toString() {
            return String.format("{\"Security0.Nonce.Get\":{\"command\":%s}}", getEncapsulatedCommand() == null ? "" : getEncapsulatedCommand().toString());
        }
    }

    private static byte[] generateNonce() {
        byte[] nonce = new byte[8];
        new Random().nextBytes(nonce);
        return nonce;
    }

    public static class ReportNonce extends CommandAdapter {
        public final byte[] nonce;

        public ReportNonce() {
            this(true, generateNonce());
        }

        private ReportNonce(boolean internal, byte[] nonce) {
            super(NONCE_REPORT);
            this.nonce = nonce;
        }

        public ReportNonce(byte[] commandData) throws DecoderException {
            super(commandData);
            nonce = new byte[8];
            in.read(nonce, 0, 8);
        }

        @Override
        protected void addCommandData(ByteArrayOutputStream result) {
            super.addCommandData(result);
            result.write(nonce, 0, 8);
        }

        @Override
        public String toString() {
            return String.format("{\"Security0.Nonce.Report\":{\"nonce\": %s}}", Hex.asString(nonce));
        }
    }

    public static class Encapsulation extends MultipleReportsCommandAdapter {
        private final Logger logger = LoggerFactory.getLogger(getClass());

        private CommandArgument argument;
        private MultiCommandProcessor processor;
        private byte[] internalNonce;
        private byte[] externalNonce;
        private byte[] iv;
        private boolean sequenced, secondFrame;
        private byte sequenceCounter;
        public Command command;
        private byte nonceId;
        private byte[] messageAuthCode;
        private byte[] bufferedData = new byte[0];

        public Encapsulation(byte[] externalNonce, byte node, Command command) {
            super(ENCAPSULATION);
            this.externalNonce = externalNonce;
            this.internalNonce = generateNonce();
            iv = ByteUtilities.combine(internalNonce, externalNonce);

            sequenced = false;
            secondFrame = false;
            sequenceCounter = 0;
            this.command = command;
            setEncapsulatedCommand(this.command);
            nonceId = this.externalNonce[0];
            this.argument = new CommandArgument(node);
        }

        public Encapsulation(byte[] commandData, byte[] externalNonce, CommandArgument argument, MultiCommandProcessor processor) throws DecoderException {
            super(commandData);
            this.argument = argument;
            this.processor = processor;
            this.externalNonce = externalNonce;
            processNextReport(commandData);
        }

        @Override
        protected void addCommandData(ByteArrayOutputStream result) {
            super.addCommandData(result);
            result.write(internalNonce, 0, 8);
            int sequence = ((secondFrame ? 0x01 : 0x00) << 5) | ((sequenced ? 0x01 : 0x00) << 4) | (sequenceCounter & 0x0F);
            boolean temp = command.getCommandCode().getCommand() == NETWORK_KEY_SET.getCommand();
            byte[] commandData = ByteUtilities.encryptAES128OFB(getNetworkKeyE(temp), iv, ByteUtilities.combine(new byte[]{(byte) sequence}, command.encode()));
            result.write(commandData, 0, commandData.length);
            result.write(nonceId);
            messageAuthCode = ByteUtilities.generateAuthData(getNetworkKeyA(temp), iv, getCommandCode().getCommand(), getControllerNodeId(), argument.getNode(), commandData);
            result.write(messageAuthCode, 0, 8);
        }

        @Override
        public void processNextReport(byte[] commandData) throws DecoderException {
            internalNonce = new byte[8];
            in.read(internalNonce, 0, 8);

            iv = ByteUtilities.combine(internalNonce, externalNonce);

            byte[] encryptedCommandData = getCommandData(commandData, 8, 9);
            byte[] decryptedCommandData = ByteUtilities.decryptAES128OFB(getNetworkKeyE(false), iv, encryptedCommandData);

            int b = decryptedCommandData[0];
            secondFrame = (b & 0x20) > 0;
            sequenced = (b & 0x10) > 0;
            sequenceCounter = (byte) (b & 0x0F);
            reportsToFollow = sequenced && !secondFrame ? 1 : 0;

            bufferedData = ByteUtilities.combine(bufferedData, ByteUtilities.copy(decryptedCommandData, 1, decryptedCommandData.length));

            nonceId = (byte) in.read();
            messageAuthCode = new byte[8];
            in.read(messageAuthCode, 0, 8);

            byte[] generatedMessageAuthCode = ByteUtilities.copy(ByteUtilities.generateAuthData(getNetworkKeyA(false), iv, getCommandCode().getCommand(), argument.getNode(), getControllerNodeId(), encryptedCommandData), 0, 8);

            if (reportsToFollow == 0 && Arrays.equals(messageAuthCode, generatedMessageAuthCode)) {
                command = processor.process(argument, bufferedData);
                setEncapsulatedCommand(command);
            }
        }

        @Override
        public CommandClasses getCommandClass() {
            if (command != null) {
                return command.getCommandClass();
            }

            return super.getCommandClass();
        }

        @Override
        public void sent(byte nodeId) {
            if (command != null) {
                command.sent(nodeId);
            }
        }

        @Override
        public String toString() {
            return String.format("{\"Security0.Encapsultation\":{\"command\": %s}}", command == null ? "pending: " + Hex.asString(bufferedData) : command.toString());
        }
    }

    public static class GetScheme extends CommandAdapter {
        @CommandClassVersion(CommandClassVersion.Version.V1)
        public GetScheme() {
            super(SCHEME_GET);
        }

        @Override
        protected void addCommandData(ByteArrayOutputStream result) {
            super.addCommandData(result);
            result.write(0x00);
        }
    }

    public static class ReportScheme extends CommandAdapter {
        public final byte schemes;

        public ReportScheme(byte[] commandData) throws DecoderException {
            super(commandData);
            schemes = (byte) in.read();
        }

        @Override
        public String toString() {
            return String.format("{\"Security0.Scheme.Report\":{\"schems\": %02X}}", schemes);
        }
    }

    public static class SetNetworkKey extends CommandAdapter {
        private final byte[] networkKey;

        @CommandClassVersion(CommandClassVersion.Version.V1)
        public SetNetworkKey(byte[] networkKey) {
            super(NETWORK_KEY_SET);
            this.networkKey = networkKey;
        }

        @Override
        protected void addCommandData(ByteArrayOutputStream result) {
            super.addCommandData(result);
            result.write(networkKey, 0, networkKey.length);
        }
    }

    public static class VerifyNetworkKey extends CommandAdapter {
        public VerifyNetworkKey(byte[] commandData) throws DecoderException {
            super(commandData);
        }

        @Override
        public String toString() {
            return String.format("{\"Security0.NetworkKey.Verify\":{}}");
        }
    }

    public static class SchemeInherit extends CommandAdapter {
        public final byte schemes;

        public SchemeInherit(byte[] commandData) throws DecoderException {
            super(commandData);
            schemes = (byte) in.read();
        }

        @Override
        public String toString() {
            return String.format("{\"Security0.Scheme.Inherit\":{\"schemes\": %02X}}", schemes);
        }
    }

    public static class GetCommands extends CommandAdapter {
        @CommandClassVersion(CommandClassVersion.Version.V1)
        public GetCommands() {
            super(COMMANDS_GET);
        }
    }

    public static class ReportCommands extends MultipleReportsCommandAdapter {
        public List<Byte> supportedCommandClasses;
        public List<Byte> controlledCommandClasses;

        public ReportCommands(byte[] commandData) throws DecoderException {
            super(commandData);
            supportedCommandClasses = new ArrayList();
            controlledCommandClasses = new ArrayList();
            processNextReport(commandData);
        }

        @Override
        public void processNextReport(byte[] commandData) throws DecoderException {
            reportsToFollow = in.read();
            byte[] allCommandClasses = getCommandData(commandData, 1);
            int numberOfCommandClasses = allCommandClasses.length;
            int separatorPosition = ByteUtilities.find(allCommandClasses, ApplicationUpdateMessage.COMMAND_CLASS_MARK);

            for (int commandClass : Arrays.copyOfRange(allCommandClasses, 0, separatorPosition)) {
                supportedCommandClasses.add((byte) commandClass);
            }

            for (int commandClass : Arrays.copyOfRange(allCommandClasses, separatorPosition + (separatorPosition == numberOfCommandClasses ? 0 : 1), numberOfCommandClasses)) {
                controlledCommandClasses.add((byte) commandClass);
            }
        }

        @Override
        public void update(ZWavePath path, int version, boolean secure) {
            addCommandClasses(path.getNodeId(), ByteUtilities.toByteArray(supportedCommandClasses), true);
        }

        @Override
        public String toString() {
            return String.format("{\"Security0.Commands.Report\":{\"supportedClasses\": [%s], \"controlledClasses\": [%s]}}", Hex.asString(supportedCommandClasses), Hex.asString(controlledCommandClasses));
        }
    }
}