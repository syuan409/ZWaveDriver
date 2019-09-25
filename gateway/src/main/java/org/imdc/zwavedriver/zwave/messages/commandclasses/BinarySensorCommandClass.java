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
import org.imdc.zwavedriver.zwave.messages.commandclasses.objects.BinarySensorType;
import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import com.inductiveautomation.ignition.common.sqltags.model.types.DataType;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class BinarySensorCommandClass implements CommandClass {

    private static final CommandCode GET = new CommandCode(CommandClasses.SENSOR_BINARY, 0x02);
    private static final CommandCode REPORT = new CommandCode(CommandClasses.SENSOR_BINARY, 0x03);
    private static final CommandCode SUPPORTED_GET = new CommandCode(CommandClasses.SENSOR_BINARY, 0x01);
    private static final CommandCode SUPPORTED_REPORT = new CommandCode(CommandClasses.SENSOR_BINARY, 0x04);

    public static class Processor extends CommandProcessorAdapter {
        @Override
        public CommandCode[] getCommandCodes() {
            return new CommandCode[]{GET, REPORT, SUPPORTED_GET, SUPPORTED_REPORT};
        }

        @Override
        public Command processCommandData(CommandCode commandCode, CommandArgument argument, byte[] commandData) throws DecoderException {
            return new Report(commandData);
        }

        @Override
        public void configureTags(ZWavePath path, int version) {
            if (version == 1) {
                configureTag(path.tag("Value"), DataType.Boolean);
            } else {
                removeTag(path.tag("Value"));
            }
        }

        @Override
        public void queueInitialMessages(ZWavePath path, int version, boolean secure, boolean initial) {
            if (version == 1) {
                sendCommand(path, new BinarySensorCommandClass.Get(), secure);
            } else if (version == 2) {
                sendCommand(path, new BinarySensorCommandClass.GetSupported(), secure);
            }
        }
    }

    public static class Get extends CommandAdapter {
        private BinarySensorType type;

        @CommandClassVersion(CommandClassVersion.Version.V1)
        public Get() {
            this(null);
        }

        @CommandClassVersion(CommandClassVersion.Version.V2)
        public Get(BinarySensorType type) {
            super(GET);
        }

        @Override
        protected void addCommandData(ByteArrayOutputStream result) {
            super.addCommandData(result);
            if (type != null) {
                // Version 2
                result.write(type.value);
            }
        }
    }

    public static class Report extends CommandAdapter {
        public final boolean value;
        public BinarySensorType type;

        public Report(byte[] commandData) throws DecoderException {
            super(commandData);
            value = in.read() != 0;
            if (commandLength == 2) {
                type = BinarySensorType.from(in.read());
            }
        }

        @Override
        public void update(ZWavePath path, int version, boolean secure) {
            if (type != null) {
                updateTag(path.tag("Types", type.name, "Value"), value);
            } else {
                updateTag(path.tag("Value"), value);
            }
        }

        @Override
        public String toString() {
            return String.format("{\"Sensor.Binary.Report\":{\"value\": %b, \"type\": %s}}", value, type == null ? "Main" : type.name);
        }
    }

    public static class GetSupported extends CommandAdapter {
        @CommandClassVersion(CommandClassVersion.Version.V2)
        public GetSupported() {
            super(SUPPORTED_GET);
        }
    }

    public static class ReportSupported extends CommandAdapter {
        public final List<BinarySensorType> supportedTypes;

        public ReportSupported(byte[] commandData) throws DecoderException {
            super(commandData);
            supportedTypes = BinarySensorType.getTypes(Hex.getMaskInts(in, commandLength));
        }

        @Override
        public void update(ZWavePath path, int version, boolean secure) {
            for (BinarySensorType type : supportedTypes) {
                configureTag(path.tag("Types", type.name, "Value"), DataType.Boolean);
            }
        }

        @Override
        public String toString() {
            return String.format("{\"Sensor.Binary.Supported.Report\":{\"types\": [%s]}}", Hex.asString(supportedTypes));
        }
    }
}
