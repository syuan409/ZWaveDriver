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
import org.imdc.zwavedriver.zwave.messages.commandclasses.objects.ThermostatFanMode;
import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import com.inductiveautomation.ignition.common.model.values.QualityCode;
import com.inductiveautomation.ignition.common.sqltags.model.types.DataType;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ThermostatFanModeCommandClass implements CommandClass {

    private static final CommandCode SET = new CommandCode(CommandClasses.THERMOSTAT_FAN_MODE, 0x01);
    private static final CommandCode GET = new CommandCode(CommandClasses.THERMOSTAT_FAN_MODE, 0x02);
    private static final CommandCode REPORT = new CommandCode(CommandClasses.THERMOSTAT_FAN_MODE, 0x03);
    private static final CommandCode SUPPORTED_GET = new CommandCode(CommandClasses.THERMOSTAT_FAN_MODE, 0x04);
    private static final CommandCode SUPPORTED_REPORT = new CommandCode(CommandClasses.THERMOSTAT_FAN_MODE, 0x05);

    public static class Processor extends CommandProcessorAdapter {
        @Override
        public CommandCode[] getCommandCodes() {
            return new CommandCode[]{GET, SET, REPORT, SUPPORTED_GET, SUPPORTED_REPORT};
        }

        @Override
        public Command processCommandData(CommandCode commandCode, CommandArgument argument, byte[] commandData) throws DecoderException {
            Command object = null;
            if (Arrays.asList(GET, SET, REPORT).contains(commandCode)) {
                object = new Report(commandData);
            } else if (Arrays.asList(SUPPORTED_GET, SUPPORTED_REPORT).contains(commandCode)) {
                object = new ReportSupported(commandData);
            }
            return object;
        }

        @Override
        public void configureTags(ZWavePath path, int version) {
            configureTagInitValue(path.tag("Lookup", "FanModes"), DataType.DataSet, ThermostatFanMode.buildDS());
            configureTag(path.tag("FanMode"), DataType.String, true);
            configureTag(path.tag("Off"), DataType.Boolean, true);
            configureTagInitValue(path.tag("Set"), DataType.Boolean, false, true);
        }

        @Override
        public void queueInitialMessages(ZWavePath path, int version, boolean secure, boolean initial) {
            sendCommand(path, new ThermostatFanModeCommandClass.Get(), secure);
        }

        @Override
        public QualityCode write(ZWavePath path, int version, boolean secure, Object o) {
            try {
                if (path.get(0).equals("Set") && ((Boolean) o)) {
                    updateTag(path, false);
                    ThermostatFanMode mode = ThermostatFanMode.from((String) readTag(path.tag("FanMode")));
                    boolean off = (Boolean) readTag(path.tag("Off"));
                    sendCommand(path, new ThermostatFanModeCommandClass.Set(mode, off), secure);
                }
            } catch (Exception ex) {
                logger.error("Error writing to thermostat fan mode tag", ex);
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
        private final ThermostatFanMode mode;
        private Boolean off;

        @CommandClassVersion(CommandClassVersion.Version.V1)
        public Set(ThermostatFanMode mode) {
            super(SET);
            this.mode = mode;
        }

        @CommandClassVersion(CommandClassVersion.Version.V2)
        public Set(ThermostatFanMode mode, boolean off) {
            super(SET);
            this.mode = mode;
            this.off = off;
        }

        @Override
        protected void addCommandData(ByteArrayOutputStream result) {
            super.addCommandData(result);
            result.write((off != null ? 0x80 : 0x00) | mode.value);
        }
    }

    private static class Report extends CommandAdapter {
        public final ThermostatFanMode mode;
        public final boolean off;

        public Report(byte[] commandData) throws DecoderException {
            super(commandData);
            int b = in.read();
            mode = ThermostatFanMode.from(b & 0x0F);
            off = (b & 0x80) != 0;
        }

        @Override
        public void update(ZWavePath path, int version, boolean secure) {
            updateTag(path.tag("FanMode"), mode.name);
            updateTag(path.tag("Off"), off);
        }

        @Override
        public String toString() {
            return String.format("{\"ThermostatFanMode.Report\":{\"mode\": %s}}", mode.toString());
        }
    }

    public static class GetSupported extends CommandAdapter {
        @CommandClassVersion(CommandClassVersion.Version.V1)
        public GetSupported() {
            super(SUPPORTED_GET);
        }
    }

    private static class ReportSupported extends CommandAdapter {
        public final List<ThermostatFanMode> supportedModes;

        public ReportSupported(byte[] commandData) throws DecoderException {
            super(commandData);
            supportedModes = new ArrayList();
            byte[] masks = getCommandData(commandData);
            for (int i = 0; i < masks.length; i++) {
                for (int b = 0; b < 8; b++) {
                    if (((masks[i] >> b) & 0x01) != 0) {
                        supportedModes.add(ThermostatFanMode.from((i * 8) + b));
                    }
                }
            }
        }

        @Override
        public String toString() {
            return String.format("{\"ThermostatFanMode.Supported.Report\":{\"modes\": [%s]}}", Hex.asString(supportedModes));
        }
    }
}
