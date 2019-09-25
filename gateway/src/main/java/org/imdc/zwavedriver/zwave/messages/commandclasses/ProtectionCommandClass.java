package org.imdc.zwavedriver.zwave.messages.commandclasses;

import org.imdc.zwavedriver.gateway.ZWavePath;
import org.imdc.zwavedriver.zwave.Hex;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.Command;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandAdapter;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandArgument;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandClass;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandClassVersion;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandCode;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandProcessorAdapter;
import org.imdc.zwavedriver.zwave.messages.commandclasses.objects.ProtectionState;
import org.imdc.zwavedriver.zwave.messages.commandclasses.objects.Timeout;
import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import com.inductiveautomation.ignition.common.model.values.QualityCode;
import com.inductiveautomation.ignition.common.sqltags.model.types.DataType;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;

public class ProtectionCommandClass implements CommandClass {

    private static final CommandCode SET = new CommandCode(CommandClasses.PROTECTION, 0x01);
    private static final CommandCode GET = new CommandCode(CommandClasses.PROTECTION, 0x02);
    private static final CommandCode REPORT = new CommandCode(CommandClasses.PROTECTION, 0x03);
    private static final CommandCode SUPPORTED_GET = new CommandCode(CommandClasses.PROTECTION, 0x04);
    private static final CommandCode SUPPORTED_REPORT = new CommandCode(CommandClasses.PROTECTION, 0x05);
    private static final CommandCode EXCLUSIVE_SET = new CommandCode(CommandClasses.PROTECTION, 0x06);
    private static final CommandCode EXCLUSIVE_GET = new CommandCode(CommandClasses.PROTECTION, 0x07);
    private static final CommandCode EXCLUSIVE_REPORT = new CommandCode(CommandClasses.PROTECTION, 0x08);
    private static final CommandCode TIMEOUT_SET = new CommandCode(CommandClasses.PROTECTION, 0x09);
    private static final CommandCode TIMEOUT_GET = new CommandCode(CommandClasses.PROTECTION, 0x0A);
    private static final CommandCode TIMEOUT_REPORT = new CommandCode(CommandClasses.PROTECTION, 0x0B);

    public static class Processor extends CommandProcessorAdapter {

        @Override
        public CommandCode[] getCommandCodes() {
            return new CommandCode[]{GET, SET, REPORT, SUPPORTED_GET, SUPPORTED_REPORT, EXCLUSIVE_GET, EXCLUSIVE_SET, EXCLUSIVE_REPORT, TIMEOUT_GET, TIMEOUT_SET, TIMEOUT_REPORT};
        }

        @Override
        public Command processCommandData(CommandCode commandCode, CommandArgument argument, byte[] commandData) throws DecoderException {
            Command object = null;
            if (Arrays.asList(GET, REPORT).contains(commandCode)) {
                object = new Report(commandData);
            } else if (Arrays.asList(SUPPORTED_GET, SUPPORTED_REPORT).contains(commandCode)) {
                object = new ReportSupported(commandData);
            } else if (Arrays.asList(EXCLUSIVE_GET, EXCLUSIVE_REPORT).contains(commandCode)) {
                object = new ReportExclusive(commandData);
            } else if (Arrays.asList(TIMEOUT_GET, TIMEOUT_REPORT).contains(commandCode)) {
                object = new ReportTimeout(commandData);
            }
            return object;
        }

        @Override
        public void configureTags(ZWavePath path, int version) {
            configureTagInitValue(path.tag("Lookup", "States"), DataType.DataSet, ProtectionState.buildDS());
            configureTagInitValue(path.tag("Lookup", "TimeoutModes"), DataType.DataSet, Timeout.TimeoutMode.buildDS());
            configureTag(path.tag("Local", "State"), DataType.String, true);
        }

        @Override
        public void queueInitialMessages(ZWavePath path, int version, boolean secure, boolean initial) {
            sendCommand(path, new ProtectionCommandClass.Get(), secure);
        }

        @Override
        public QualityCode write(ZWavePath path, int version, boolean secure, Object o) {
            try {
                sendCommand(path, new ProtectionCommandClass.Set(ProtectionState.from((String) o)), secure);
            } catch (Exception ex) {
                logger.error("Error writing to door lock tag", ex);
                return QualityCode.Error;
            }
            return QualityCode.Good;
        }
    }

    public static class Get extends CommandAdapter {
        @CommandClassVersion(CommandClassVersion.Version.V1)
        public Get() {
            super(GET);
        }
    }

    public static class Set extends CommandAdapter {
        private final ProtectionState localState;
        private ProtectionState rfState;

        @CommandClassVersion(CommandClassVersion.Version.V1)
        public Set(ProtectionState localState) {
            this(localState, null);
        }

        @CommandClassVersion(CommandClassVersion.Version.V2)
        public Set(ProtectionState localState, ProtectionState rfState) {
            super(SET);
            this.localState = localState;
            this.rfState = rfState;
        }

        @Override
        protected void addCommandData(ByteArrayOutputStream result) {
            super.addCommandData(result);
            result.write(localState.value);
            if (rfState != null) {
                result.write(rfState.value);
            }
        }
    }

    public static class Report extends CommandAdapter {
        public final ProtectionState localState;
        public ProtectionState rfState;

        public Report(byte[] commandData) throws DecoderException {
            super(commandData);
            localState = ProtectionState.from(in.read());

            if (commandLength > 1) {
                rfState = ProtectionState.from(in.read());
            }
        }

        @Override
        public void update(ZWavePath path, int version, boolean secure) {
            updateTag(path.tag("Local", "State"), localState.name);
        }

        @Override
        public String toString() {
            return String.format("{\"Protection.Report\":{\"state\": %s}}", localState.toString());
        }
    }

    public static class GetSupported extends CommandAdapter {
        @CommandClassVersion(CommandClassVersion.Version.V2)
        public GetSupported() {
            super(SUPPORTED_GET);
        }
    }

    public static class ReportSupported extends CommandAdapter {
        public final boolean exclusiveControl, timeout;
        public final List<ProtectionState> localStates, rfStates;

        public ReportSupported(byte[] commandData) throws DecoderException {
            super(commandData);
            int firstByte = in.read();
            exclusiveControl = ((firstByte >> 1) & 0x01) != 0;
            timeout = (firstByte & 0x01) != 0;
            localStates = ProtectionState.getStates(Hex.getMaskInts(in, 2));
            rfStates = ProtectionState.getStates(Hex.getMaskInts(in, 2));
        }

        @Override
        public String toString() {
            return String.format("{\"Protection.Supported.Report\":{\"states\": [%s]}}", Hex.asString(localStates));
        }
    }

    public static class GetExclusive extends CommandAdapter {
        @CommandClassVersion(CommandClassVersion.Version.V2)
        public GetExclusive() {
            super(EXCLUSIVE_GET);
        }
    }

    public static class SetExclusive extends CommandAdapter {
        private final byte nodeId;

        @CommandClassVersion(CommandClassVersion.Version.V2)
        public SetExclusive(int nodeId) {
            super(EXCLUSIVE_SET);
            this.nodeId = (byte) nodeId;
        }

        @Override
        protected void addCommandData(ByteArrayOutputStream result) {
            super.addCommandData(result);
            result.write(nodeId);
        }
    }

    public static class ReportExclusive extends CommandAdapter {
        public final byte nodeId;

        public ReportExclusive(byte[] commandData) throws DecoderException {
            super(commandData);
            nodeId = (byte) in.read();
        }

        @Override
        public String toString() {
            return String.format("{\"Protection.Exclusive.Report\":{\"nodeId\": %s}}", Hex.asString(nodeId));
        }
    }

    public static class GetTimeout extends CommandAdapter {
        @CommandClassVersion(CommandClassVersion.Version.V2)
        public GetTimeout() {
            super(TIMEOUT_GET);
        }
    }

    public static class SetTimeout extends CommandAdapter {
        private final Timeout timeout;

        @CommandClassVersion(CommandClassVersion.Version.V2)
        public SetTimeout(Timeout timeout) {
            super(TIMEOUT_SET);
            this.timeout = timeout;
        }

        @Override
        protected void addCommandData(ByteArrayOutputStream result) {
            super.addCommandData(result);
            result.write(timeout.getValue());
        }
    }

    public static class ReportTimeout extends CommandAdapter {
        public final Timeout timeout;

        public ReportTimeout(byte[] commandData) throws DecoderException {
            super(commandData);
            timeout = new Timeout(in.read());
        }

        @Override
        public String toString() {
            return String.format("{\"Protection.Timeout.Report\":{\"timeout\": %s}}", timeout.toString());
        }
    }
}
