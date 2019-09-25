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
import org.imdc.zwavedriver.zwave.messages.commandclasses.objects.Sensor;
import org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SensorType;
import org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SensorUnit;
import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import com.inductiveautomation.ignition.common.sqltags.model.types.DataType;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MultiLevelSensorCommandClass implements CommandClass {

    private static final CommandCode GET = new CommandCode(CommandClasses.MULTI_LEVEL_SENSOR, 0x04);
    private static final CommandCode REPORT = new CommandCode(CommandClasses.MULTI_LEVEL_SENSOR, 0x05);
    private static final CommandCode SUPPORTED_SENSOR_GET = new CommandCode(CommandClasses.MULTI_LEVEL_SENSOR, 0x01);
    private static final CommandCode SUPPORTED_SENSOR_REPORT = new CommandCode(CommandClasses.MULTI_LEVEL_SENSOR, 0x02);
    private static final CommandCode SUPPORTED_SCALE_GET = new CommandCode(CommandClasses.MULTI_LEVEL_SENSOR, 0x03);
    private static final CommandCode SUPPORTED_SCALE_REPORT = new CommandCode(CommandClasses.MULTI_LEVEL_SENSOR, 0x06);

    public static class Processor extends CommandProcessorAdapter {

        @Override
        public CommandCode[] getCommandCodes() {
            return new CommandCode[]{GET, REPORT, SUPPORTED_SENSOR_GET, SUPPORTED_SENSOR_REPORT, SUPPORTED_SCALE_GET, SUPPORTED_SCALE_REPORT};
        }

        @Override
        public Command processCommandData(CommandCode commandCode, CommandArgument argument, byte[] commandData) throws DecoderException {
            Command object = null;
            if (Arrays.asList(GET, REPORT).contains(commandCode)) {
                object = new Report(commandData);
            } else if (Arrays.asList(SUPPORTED_SENSOR_GET, SUPPORTED_SENSOR_REPORT).contains(commandCode)) {
                object = new ReportSupportedSensor(commandData);
            } else if (Arrays.asList(SUPPORTED_SCALE_GET, SUPPORTED_SCALE_REPORT).contains(commandCode)) {
                object = new ReportSupportedScale(commandData);
            }
            return object;
        }

        @Override
        public void configureTags(ZWavePath path, int version) {
            configureTagInitValue(path.tag("Lookup", "Types"), DataType.DataSet, SensorType.buildDS());
            configureTag(path.tag("Type"), DataType.String);
            configureTag(path.tag("Units"), DataType.String);
            configureTag(path.tag("Value"), DataType.Float8);
        }

        @Override
        public void queueInitialMessages(ZWavePath path, int version, boolean secure, boolean initial) {
            if(version > 1) {
                sendCommand(path, new MultiLevelSensorCommandClass.Get(), secure);
            } else {
                sendCommand(path, new MultiLevelSensorCommandClass.GetSupportedSensor(), secure);
            }
        }
    }

    public static class Get extends CommandAdapter {
        private SensorType type;
        private SensorUnit unit;

        @CommandClassVersion(CommandClassVersion.Version.V1)
        public Get() {
            super(GET);
        }

        @CommandClassVersion(CommandClassVersion.Version.V5)
        public Get(SensorType type, SensorUnit unit) {
            super(GET);
            this.type = type;
            this.unit = unit;
        }

        @Override
        protected void addCommandData(ByteArrayOutputStream result) {
            super.addCommandData(result);

            if (type != null) {
                result.write(type.value);
            }

            if (unit != null) {
                result.write(unit.scale << 3);
            }
        }
    }

    public static class Report extends CommandAdapter {
        public final Sensor sensor;

        public Report(byte[] commandData) throws DecoderException {
            super(commandData);
            sensor = new Sensor(in);
        }

        @Override
        public void update(ZWavePath path, int version, boolean secure) {
            if(version > 1){
                updateTag(path.tag("Types", sensor.type.name(), "Units", sensor.unit.name(), "Value"), sensor.value);
            }

            updateTag(path.tag("Type"), sensor.type.name);
            updateTag(path.tag("Units"), sensor.unit.unit);
            updateTag(path.tag("Value"), sensor.value);
        }

        @Override
        public String toString() {
            return String.format("{\"MultiLevelSensor.Report\":{\"sensor\": [%s]}}", sensor);
        }
    }

    public static class GetSupportedSensor extends CommandAdapter {
        @CommandClassVersion(CommandClassVersion.Version.V5)
        public GetSupportedSensor() {
            super(SUPPORTED_SENSOR_GET);
        }
    }

    public static class ReportSupportedSensor extends CommandAdapter {
        public final List<SensorType> supportedSensorTypes;

        public ReportSupportedSensor(byte[] commandData) throws DecoderException {
            super(commandData);
            byte[] sensors = getCommandData(commandData);
            supportedSensorTypes = new ArrayList();
            for (SensorType t : SensorType.values()) {
                if (sensors != null && sensors.length > t.supportedByte) {
                    if ((sensors[t.supportedByte] & t.supportedBitMask) > 0) {
                        supportedSensorTypes.add(t);
                    }
                }
            }
        }

        @Override
        public void update(ZWavePath path, int version, boolean secure) {
            for(SensorType sensorType : supportedSensorTypes) {
                configureTagInitValue(path.tag("Types", sensorType.name(), "Type"), DataType.String, sensorType.name);
                sendCommand(path, new MultiLevelSensorCommandClass.GetSupportedScale(sensorType), secure);
            }
        }

        @Override
        public String toString() {
            return String.format("{\"MultiLevelSensor.Supported.Report\":{\"sensorTypes\": [%s]}}", Hex.asString(supportedSensorTypes));
        }
    }

    public static class GetSupportedScale extends CommandAdapter {
        private final SensorType type;

        @CommandClassVersion(CommandClassVersion.Version.V5)
        public GetSupportedScale(SensorType type) {
            super(SUPPORTED_SCALE_GET);
            this.type = type;
        }

        @Override
        protected void addCommandData(ByteArrayOutputStream result) {
            super.addCommandData(result);
            result.write(type.value);
        }
    }

    public static class ReportSupportedScale extends CommandAdapter {
        public final SensorType sensorType;
        public final List<SensorUnit> supportedSensorUnits;

        public ReportSupportedScale(byte[] commandData) throws DecoderException {
            super(commandData);

            sensorType = SensorType.from(in.read());
            int scales = in.read() & 0x0F;
            supportedSensorUnits = new ArrayList();
            for (SensorUnit u : SensorUnit.values()) {
                if ((scales & u.supportedBitMask) > 0) {
                    supportedSensorUnits.add(u);
                }
            }
        }

        @Override
        public void update(ZWavePath path, int version, boolean secure) {
            for(SensorUnit unit : supportedSensorUnits){
                configureTagInitValue(path.tag("Types", sensorType.name(), "Units", unit.name(), "Unit"), DataType.String, unit.unit);
                configureTag(path.tag("Types", sensorType.name(), "Units", unit.name(), "Value"), DataType.Float8);
                sendCommand(path, new MultiLevelSensorCommandClass.Get(sensorType, unit), secure);
            }
        }

        @Override
        public String toString() {
            return String.format("{\"MultiLevelSensor.Scale.Supported.Report\":{\"sensorType\": %s, \"sensorUnits\": [%s]}}", sensorType.toString(), Hex.asString(supportedSensorUnits));
        }
    }
}
