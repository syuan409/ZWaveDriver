package org.imdc.zwavedriver.zwave.messages.commandclasses;

import org.imdc.zwavedriver.gateway.ZWavePath;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.*;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.*;
import com.inductiveautomation.ignition.common.model.values.QualityCode;
import com.inductiveautomation.ignition.common.sqltags.model.types.DataType;

public class DeviceResetCommandClass implements CommandClass {

    private static final CommandCode RESET = new CommandCode(CommandClasses.DEVICE_RESET, 0x01);

    public static class Processor extends CommandProcessorAdapter {
        @Override
        public CommandCode[] getCommandCodes() {
            return new CommandCode[]{RESET};
        }

        @Override
        public Command processCommandData(CommandCode commandCode, CommandArgument argument, byte[] commandData) {
            return null;
        }

        @Override
        public void configureTags(ZWavePath path, int version) {
            configureTagInitValue(path.tag("Reset"), DataType.Boolean, false, true);
        }

        @Override
        public QualityCode write(ZWavePath path, int version, boolean secure, Object o) {
            updateTag(path, false);
            sendCommand(path, new DeviceResetCommandClass.Reset(), secure);
            return QualityCode.Good;
        }
    }

    public static class Reset extends CommandAdapter {
        @CommandClassVersion(CommandClassVersion.Version.V1)
        public Reset() {
            super(RESET);
        }
    }
}
