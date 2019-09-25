package org.imdc.zwavedriver.zwave.messages.commandclasses;

import org.imdc.zwavedriver.gateway.ZWavePath;
import org.imdc.zwavedriver.zwave.ByteUtilities;
import org.imdc.zwavedriver.zwave.Hex;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.Command;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandAdapter;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandArgument;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandClass;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandClassVersion;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandCode;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandProcessorAdapter;
import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import com.inductiveautomation.ignition.common.sqltags.model.types.DataType;

public class ManufacturerSpecificCommandClass implements CommandClass {

    private static final CommandCode GET = new CommandCode(CommandClasses.MANUFACTURER_SPECIFIC, 0x04);
    private static final CommandCode REPORT = new CommandCode(CommandClasses.MANUFACTURER_SPECIFIC, 0x05);

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
            configureTag(path.tag("Manufacturer"), DataType.String);
            configureTag(path.tag("DeviceType"), DataType.String);
            configureTag(path.tag("DeviceId"), DataType.String);
        }

        @Override
        public void queueInitialMessages(ZWavePath path, int version, boolean secure, boolean initial) {
            sendCommand(path, new ManufacturerSpecificCommandClass.Get(), secure);
        }
    }

    public static class Get extends CommandAdapter {
        @CommandClassVersion(CommandClassVersion.Version.V1)
        public Get() {
            super(GET);
        }
    }

    public static class Report extends CommandAdapter {
        public final int manufacturer;
        public final int deviceType;
        public final int deviceId;

        public Report(byte[] commandData) throws DecoderException {
            super(commandData);

            manufacturer = ByteUtilities.readInt16MSB(in);
            deviceType = ByteUtilities.readInt16MSB(in);
            deviceId = ByteUtilities.readInt16MSB(in);
        }

        @Override
        public void update(ZWavePath path, int version, boolean secure) {
            updateTag(path.tag("Manufacturer"), Hex.asString(manufacturer));
            updateTag(path.tag("DeviceType"), Hex.asString(deviceType));
            updateTag(path.tag("DeviceId"), Hex.asString(deviceId));
        }

        @Override
        public String toString() {
            return String.format("{\"ApplicationSpecific.Report\": {\"manufacturer\": %d, \"deviceType\": %d, \"deviceId\": %d", manufacturer, deviceType, deviceId);
        }
    }
}
