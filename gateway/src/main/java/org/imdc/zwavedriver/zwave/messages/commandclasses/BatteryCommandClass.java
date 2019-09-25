package org.imdc.zwavedriver.zwave.messages.commandclasses;

import org.imdc.zwavedriver.gateway.ZWavePath;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.*;
import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import com.inductiveautomation.ignition.common.sqltags.model.types.DataType;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.*;

public class BatteryCommandClass implements CommandClass {

    private static final CommandCode GET = new CommandCode(CommandClasses.BATTERY, 0x02);
    private static final CommandCode REPORT = new CommandCode(CommandClasses.BATTERY, 0x03);

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
            configureTag(path.tag("Level"), DataType.Int1);
            configureTag(path.tag("LowBatterWarning"), DataType.Boolean);
        }

        @Override
        public void queueInitialMessages(ZWavePath path,  int version, boolean secure, boolean initial) {
            sendCommand(path, new BatteryCommandClass.Get(), secure);
        }
    }

    public static class Get extends CommandAdapter {
        @CommandClassVersion(CommandClassVersion.Version.V1)
        public Get() {
            super(GET);
        }
    }

    public static class Report extends CommandAdapter {
        private final byte level;
        private final boolean lowBatteryWarning;

        public Report(byte[] commandData) throws DecoderException {
            super(commandData);
            byte b = (byte) in.read();
            if (b == 0xFF) {
                lowBatteryWarning = true;
                level = 0;
            } else {
                lowBatteryWarning = false;
                level = b;
            }
        }

        public boolean isLowBatteryWarning() {
            return lowBatteryWarning;
        }

        @Override
        public void update(ZWavePath path, int version, boolean secure) {
            updateTag(path.tag("Level"), level);
            updateTag(path.tag("LowBatterWarning"), lowBatteryWarning);
        }

        @Override
        public String toString() {
            return String.format("{\"Battery.Report\":{\"level\": %d, \"lowBatteryWarning\": %b}}", level, lowBatteryWarning);
        }
    }
}
