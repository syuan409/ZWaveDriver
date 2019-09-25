package org.imdc.zwavedriver.zwave.messages.commandclasses.thermostat;

import org.imdc.zwavedriver.gateway.ZWavePath;
import org.imdc.zwavedriver.zwave.Hex;
import org.imdc.zwavedriver.zwave.messages.commandclasses.CommandClasses;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.Command;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandAdapter;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandArgument;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandClass;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandClassVersion;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandCode;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandProcessorAdapter;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.MultipleReportsCommandAdapter;
import org.imdc.zwavedriver.zwave.messages.commandclasses.objects.ThermostatLog;
import org.imdc.zwavedriver.zwave.messages.commandclasses.objects.ThermostatOperatingState;
import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import com.inductiveautomation.ignition.common.sqltags.model.types.DataType;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ThermostatOperatingStateCommandClass implements CommandClass {

    private static final CommandCode GET = new CommandCode(CommandClasses.THERMOSTAT_OPERATING_STATE, 0x02);
    private static final CommandCode REPORT = new CommandCode(CommandClasses.THERMOSTAT_OPERATING_STATE, 0x03);
    private static final CommandCode LOGGING_GET = new CommandCode(CommandClasses.THERMOSTAT_OPERATING_STATE, 0x05);
    private static final CommandCode LOGGING_REPORT = new CommandCode(CommandClasses.THERMOSTAT_OPERATING_STATE, 0x06);
    private static final CommandCode SUPPORTED_LOGGING_GET = new CommandCode(CommandClasses.THERMOSTAT_OPERATING_STATE, 0x01);
    private static final CommandCode SUPPORTED_LOGGING_REPORT = new CommandCode(CommandClasses.THERMOSTAT_OPERATING_STATE, 0x04);

    public static class Processor extends CommandProcessorAdapter {
        @Override
        public CommandCode[] getCommandCodes() {
            return new CommandCode[]{GET, REPORT, LOGGING_GET, LOGGING_REPORT, SUPPORTED_LOGGING_GET, SUPPORTED_LOGGING_REPORT};
        }

        @Override
        public Command processCommandData(CommandCode commandCode, CommandArgument argument, byte[] commandData) throws DecoderException {
            Command object = null;
            if (Arrays.asList(GET, REPORT).contains(commandCode)) {
                object = new Report(commandData);
            } else if (Arrays.asList(LOGGING_GET, LOGGING_REPORT).contains(commandCode)) {
                object = new ReportLogging(commandData);
            } else if (Arrays.asList(SUPPORTED_LOGGING_GET, SUPPORTED_LOGGING_REPORT).contains(commandCode)) {
                object = new ReportLoggingSupported(commandData);
            }
            return object;
        }

        @Override
        public void configureTags(ZWavePath path, int version) {
            configureTagInitValue(path.tag("Lookup", "States"), DataType.DataSet, ThermostatOperatingState.buildDS());
            configureTag(path.tag("State"), DataType.String);
        }

        @Override
        public void queueInitialMessages(ZWavePath path, int version, boolean secure, boolean initial) {
            sendCommand(path, new ThermostatOperatingStateCommandClass.Get(), secure);
        }
    }

    public static class Get extends CommandAdapter {
        @CommandClassVersion(CommandClassVersion.Version.V1)
        public Get() {
            super(GET);
        }
    }

    public static class Report extends CommandAdapter {
        public final ThermostatOperatingState state;

        public Report(byte[] commandData) throws DecoderException {
            super(commandData);
            state = ThermostatOperatingState.from(in.read());
        }

        @Override
        public void update(ZWavePath path, int version, boolean secure) {
            updateTag(path.tag("State"), state.name);
        }

        @Override
        public String toString() {
            return String.format("{\"ThermostatOperatingState.Report\":{\"state\": %s}}", state.toString());
        }
    }

    public static class GetLogging extends CommandAdapter {
        private final List<ThermostatOperatingState> states;

        @CommandClassVersion(CommandClassVersion.Version.V2)
        public GetLogging(List<ThermostatOperatingState> states) {
            super(GET);
            this.states = states;
        }

        @Override
        protected void addCommandData(ByteArrayOutputStream result) {
            super.addCommandData(result);
            int numBytes = 1;
            for (ThermostatOperatingState state : states) {
                if ((state.bitMaskByte + 1) > numBytes) {
                    numBytes = state.bitMaskByte + 1;
                }
            }

            byte[] writeBytes = new byte[numBytes];
            for (ThermostatOperatingState state : states) {
                writeBytes[state.bitMaskByte] |= state.bitMask;
            }
            result.write(writeBytes, 0, writeBytes.length);
        }
    }

    public static class ReportLogging extends MultipleReportsCommandAdapter {
        public List<ThermostatLog> logs;

        public ReportLogging(byte[] commandData) throws DecoderException {
            super(commandData);
            logs = new ArrayList();
            processNextReport(commandData);
        }

        @Override
        public void processNextReport(byte[] commandData) throws DecoderException {
            int numberOfLogs = (commandLength - 1) / 5;
            for (int i = 0; i < numberOfLogs; i++) {
                logs.add(new ThermostatLog(in));
            }
        }

        @Override
        public String toString() {
            return String.format("{\"ThermostatOperatingState.Logging.Report\":{\"logs\": [%s]}}", Hex.asString(logs));
        }
    }

    public static class GetSupportedLogging extends CommandAdapter {
        @CommandClassVersion(CommandClassVersion.Version.V2)
        public GetSupportedLogging() {
            super(GET);
        }
    }

    public static class ReportLoggingSupported extends CommandAdapter {
        public final List<ThermostatOperatingState> supportedStates;

        public ReportLoggingSupported(byte[] commandData) throws DecoderException {
            super(commandData);
            supportedStates = new ArrayList();
            byte[] masks = getCommandData(commandData);
            for (int i = 0; i < masks.length; i++) {
                for (int b = 0; b < 8; b++) {
                    if (((masks[i] >> b) & 0x01) != 0) {
                        supportedStates.add(ThermostatOperatingState.from((i * 8) + b));
                    }
                }
            }
        }

        @Override
        public String toString() {
            return String.format("{\"ThermostatOperatingState.Logging.Supported.Report\":{\"modes\": [%s]}}", Hex.asString(supportedStates));
        }
    }
}
