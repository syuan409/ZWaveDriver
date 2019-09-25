package org.imdc.zwavedriver.zwave.messages.commandclasses.thermostat;

import org.imdc.zwavedriver.gateway.ZWavePath;
import org.imdc.zwavedriver.zwave.ByteUtilities;
import org.imdc.zwavedriver.zwave.Hex;
import org.imdc.zwavedriver.zwave.messages.commandclasses.CommandClasses;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.Command;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandAdapter;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandArgument;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandClass;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandClassVersion;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandCode;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandProcessorAdapter;
import org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SensorType;
import org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SensorUnit;
import org.imdc.zwavedriver.zwave.messages.commandclasses.objects.Size;
import org.imdc.zwavedriver.zwave.messages.commandclasses.objects.ThermostatSetpoint;
import org.imdc.zwavedriver.zwave.messages.commandclasses.objects.ThermostatSetpointType;
import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import com.inductiveautomation.ignition.common.model.values.QualityCode;
import com.inductiveautomation.ignition.common.sqltags.model.types.DataType;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ThermostatSetpointCommandClass implements CommandClass {

    private static final CommandCode SET = new CommandCode(CommandClasses.THERMOSTAT_SETPOINT, 0x01);
    private static final CommandCode GET = new CommandCode(CommandClasses.THERMOSTAT_SETPOINT, 0x02);
    private static final CommandCode REPORT = new CommandCode(CommandClasses.THERMOSTAT_SETPOINT, 0x03);
    private static final CommandCode SUPPORTED_GET = new CommandCode(CommandClasses.THERMOSTAT_SETPOINT, 0x04);
    private static final CommandCode SUPPORTED_REPORT = new CommandCode(CommandClasses.THERMOSTAT_SETPOINT, 0x05);
    private static final CommandCode CAPABILITIES_GET = new CommandCode(CommandClasses.THERMOSTAT_SETPOINT, 0x09);
    private static final CommandCode CAPABILITIES_REPORT = new CommandCode(CommandClasses.THERMOSTAT_SETPOINT, 0x0A);

    private static final SensorType TYPE = SensorType.TEMPERATURE;

    public static class Processor extends CommandProcessorAdapter {
        @Override
        public CommandCode[] getCommandCodes() {
            return new CommandCode[]{GET, SET, REPORT, SUPPORTED_GET, SUPPORTED_REPORT, CAPABILITIES_GET, CAPABILITIES_REPORT};
        }

        @Override
        public Command processCommandData(CommandCode commandCode, CommandArgument argument, byte[] commandData) throws DecoderException {
            Command object = null;
            if (Arrays.asList(GET, SET, REPORT).contains(commandCode)) {
                object = new Report(commandData);
            } else if (Arrays.asList(SUPPORTED_GET, SUPPORTED_REPORT).contains(commandCode)) {
                object = new ReportSupported(commandData);
            } else if (Arrays.asList(CAPABILITIES_GET, CAPABILITIES_REPORT).contains(commandCode)) {
                object = new ReportCapabilities(commandData);
            }
            return object;
        }

        @Override
        public void configureTags(ZWavePath path, int version) {
            configureTagInitValue(path.tag("Lookup", "Types"), DataType.DataSet, ThermostatSetpointType.buildDS());
            configureTagInitValue(path.tag("Lookup", "Units"), DataType.DataSet, SensorUnit.buildDS(TYPE));

            for (ThermostatSetpointType t : ThermostatSetpointType.values()) {
                configureTagInitValue(path.tag("Types", t.name, "Supported"), DataType.Boolean, false);
                configureTag(path.tag("Types", t.name, "Units"), DataType.String, true);
                configureTag(path.tag("Types", t.name, "Value"), DataType.Float8, true);
                configureTagInitValue(path.tag("Types", t.name, "Set"), DataType.Boolean, false, true);
            }
        }

        @Override
        public void queueInitialMessages(ZWavePath path, int version, boolean secure, boolean initial) {
            sendCommand(path, new ThermostatSetpointCommandClass.GetSupported(), secure);
        }

        @Override
        public QualityCode write(ZWavePath path, int version, boolean secure, Object o) {
            try {
                if (path.get(2).equals("Set") && ((Boolean) o)) {
                    updateTag(path, false);
                    String typeStr = path.get(1);
                    ThermostatSetpointType type = ThermostatSetpointType.from(typeStr);
                    SensorUnit unit = SensorUnit.fromMeterScale(TYPE, (String) readTag(path.tag("Types", typeStr, "Unit")));
                    double value = (Double) readTag(path.tag("Types", typeStr, "Value"));
                    ThermostatSetpoint setpoint = new ThermostatSetpoint(type, unit, value);
                    sendCommand(path, new ThermostatSetpointCommandClass.Set(setpoint), secure);
                }
            } catch (Exception ex) {
                logger.error("Error writing to thermostat setpoint tag", ex);
                return QualityCode.Error;
            }
            return QualityCode.Good;
        }
    }

    public static class Get extends CommandAdapter {
        public final ThermostatSetpointType type;

        @CommandClassVersion(CommandClassVersion.Version.V1)
        public Get(ThermostatSetpointType type) {
            this(GET, type);
        }

        @CommandClassVersion(CommandClassVersion.Version.V1)
        protected Get(CommandCode commandCode, ThermostatSetpointType type) {
            super(commandCode);
            this.type = type;
        }

        @Override
        protected void addCommandData(ByteArrayOutputStream result) {
            super.addCommandData(result);
            result.write(type.value);
        }
    }

    public static class Set extends CommandAdapter {
        public final ThermostatSetpoint setpoint;

        @CommandClassVersion(CommandClassVersion.Version.V1)
        public Set(ThermostatSetpoint setpoint) {
            super(SET);
            this.setpoint = setpoint;
        }

        @Override
        protected void addCommandData(ByteArrayOutputStream result) {
            super.addCommandData(result);
            setpoint.write(result);
        }
    }

    public static class Report extends CommandAdapter {
        public final ThermostatSetpoint setpoint;

        public Report(byte[] commandData) throws DecoderException {
            super(commandData);
            setpoint = new ThermostatSetpoint(in);
        }

        @Override
        public void update(ZWavePath path, int version, boolean secure) {
            updateTag(path.tag("Types", setpoint.type.name, "Units"), setpoint.unit.unit);
            updateTag(path.tag("Types", setpoint.type.name, "Value"), setpoint.value);
        }

        @Override
        public String toString() {
            return String.format("{\"ThermostatSetpoint.Report\":{\"setpoint\": %s}}", setpoint.toString());
        }
    }

    public static class GetSupported extends CommandAdapter {
        @CommandClassVersion(CommandClassVersion.Version.V1)
        public GetSupported() {
            super(SUPPORTED_GET);
        }
    }

    private static class ReportSupported extends CommandAdapter {
        public final List<ThermostatSetpointType> supportedTypes;

        public ReportSupported(byte[] commandData) throws DecoderException {
            super(commandData);
            supportedTypes = new ArrayList();
            byte[] masks = getCommandData(commandData);
            for (int i = 0; i < masks.length; i++) {
                for (int b = 0; b < 8; b++) {
                    if (((masks[i] >> b) & 0x01) != 0) {
                        supportedTypes.add(ThermostatSetpointType.from((i * 8) + b));
                    }
                }
            }
        }

        @Override
        public void update(ZWavePath path, int version, boolean secure) {
            for (ThermostatSetpointType t : supportedTypes) {
                updateTag(path.tag("Types", t.name, "Supported"), true);
                sendCommand(path, new ThermostatSetpointCommandClass.Get(t), secure);
            }
        }

        @Override
        public String toString() {
            return String.format("{\"ThermostatSetpoint.Supported.Report\":{\"types\": [%s]}}", Hex.asString(supportedTypes));
        }
    }

    public static class GetCapabilities extends Get {
        @CommandClassVersion(CommandClassVersion.Version.V3)
        public GetCapabilities(ThermostatSetpointType type) {
            super(CAPABILITIES_GET, type);
        }
    }

    private static class ReportCapabilities extends CommandAdapter {
        public final ThermostatSetpointType type;
        public final SensorUnit minValueUnit;
        public final double minValue;
        public final SensorUnit maxValueUnit;
        public final double maxValue;

        public ReportCapabilities(byte[] commandData) throws DecoderException {
            super(commandData);
            type = ThermostatSetpointType.from(in.read() & 0x0F);

            int dimensions = in.read();
            int precision = dimensions >> 5;
            minValueUnit = SensorUnit.fromMeterScale(SensorType.TEMPERATURE, (dimensions >> 3) & 0x03);
            Size size = Size.from(dimensions & 0x07);
            minValue = ByteUtilities.readIntMSBPrecision(in, size, precision);

            dimensions = in.read();
            precision = dimensions >> 5;
            maxValueUnit = SensorUnit.fromMeterScale(SensorType.TEMPERATURE, (dimensions >> 3) & 0x03);
            size = Size.from(dimensions & 0x07);
            maxValue = ByteUtilities.readIntMSBPrecision(in, size, precision);
        }

        @Override
        public String toString() {
            return String.format("{\"ThermostatSetpoint.Capabilities.Report\":{\"type\": %s, \"minValue\": %f, \"maxValue\": %f}}", type.toString(), minValue, maxValue);
        }
    }
}
