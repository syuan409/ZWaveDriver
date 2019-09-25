package org.imdc.zwavedriver.zwave.messages.commandclasses.thermostat;

import org.imdc.zwavedriver.gateway.ZWavePath;
import org.imdc.zwavedriver.zwave.messages.commandclasses.CommandClasses;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.Command;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandAdapter;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandArgument;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandClass;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandClassVersion;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandCode;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandProcessorAdapter;
import org.imdc.zwavedriver.zwave.messages.commandclasses.objects.ThermostatFanState;
import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import com.inductiveautomation.ignition.common.sqltags.model.types.DataType;

public class ThermostatFanStateCommandClass implements CommandClass {

    private static final CommandCode GET = new CommandCode(CommandClasses.THERMOSTAT_FAN_STATE, 0x02);
    private static final CommandCode REPORT = new CommandCode(CommandClasses.THERMOSTAT_FAN_STATE, 0x03);

    public static class Processor extends CommandProcessorAdapter {
        @Override
        public CommandCode[] getCommandCodes() {
            return new CommandCode[]{GET, REPORT};
        }

        @Override
        public Command processCommandData(CommandCode commandCode, CommandArgument argument, byte[] commandData) throws DecoderException {
            return new Report(commandData);
        }

        @Override
        public void configureTags(ZWavePath path, int version) {
            configureTagInitValue(path.tag("Lookup", "FanStates"), DataType.DataSet, ThermostatFanState.buildDS());
            configureTag(path.tag("FanState"), DataType.String);
        }

        @Override
        public void queueInitialMessages(ZWavePath path, int version, boolean secure, boolean initial) {
            sendCommand(path, new ThermostatFanStateCommandClass.Get(), secure);
        }
    }

    public static class Get extends CommandAdapter {
        @CommandClassVersion(CommandClassVersion.Version.V1)
        public Get() {
            super(GET);
        }
    }

    public static class Report extends CommandAdapter {
        public final ThermostatFanState state;

        public Report(byte[] commandData) throws DecoderException {
            super(commandData);
            state = ThermostatFanState.from(in.read() & 0x0F);
        }

        @Override
        public void update(ZWavePath path, int version, boolean secure) {
            updateTag(path.tag("FanState"), state.name);
        }

        @Override
        public String toString() {
            return String.format("{\"ThermostatFanState.Report\":{\"state\": %s}}", state.toString());
        }
    }
}
