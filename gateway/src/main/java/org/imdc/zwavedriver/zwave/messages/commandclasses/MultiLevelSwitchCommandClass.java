package org.imdc.zwavedriver.zwave.messages.commandclasses;

import org.imdc.zwavedriver.gateway.ZWavePath;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.Command;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandAdapter;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandArgument;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandClass;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandClassVersion;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandCode;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandProcessorAdapter;
import org.imdc.zwavedriver.zwave.messages.commandclasses.objects.Direction;
import org.imdc.zwavedriver.zwave.messages.commandclasses.objects.Duration;
import org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SwitchType;
import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import com.inductiveautomation.ignition.common.model.values.QualityCode;
import com.inductiveautomation.ignition.common.sqltags.model.types.DataType;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

public class MultiLevelSwitchCommandClass implements CommandClass {

    private static final CommandCode SET = new CommandCode(CommandClasses.MULTI_LEVEL_SWITCH, 0x01);
    private static final CommandCode GET = new CommandCode(CommandClasses.MULTI_LEVEL_SWITCH, 0x02);
    private static final CommandCode REPORT = new CommandCode(CommandClasses.MULTI_LEVEL_SWITCH, 0x03);
    private static final CommandCode START_LEVEL_CHANGE = new CommandCode(CommandClasses.MULTI_LEVEL_SWITCH, 0x04);
    private static final CommandCode STOP_LEVEL_CHANGE = new CommandCode(CommandClasses.MULTI_LEVEL_SWITCH, 0x05);
    private static final CommandCode SUPPORTED_GET = new CommandCode(CommandClasses.MULTI_LEVEL_SWITCH, 0x06);
    private static final CommandCode SUPPORTED_REPORT = new CommandCode(CommandClasses.MULTI_LEVEL_SWITCH, 0x07);

    private static final int IGNORE_START_POSITION_BIT = 1 << 5;

    public static class Processor extends CommandProcessorAdapter {

        @Override
        public CommandCode[] getCommandCodes() {
            return new CommandCode[]{GET, SET, REPORT, START_LEVEL_CHANGE, STOP_LEVEL_CHANGE, SUPPORTED_GET, SUPPORTED_REPORT};
        }

        @Override
        public Command processCommandData(CommandCode commandCode, CommandArgument argument, byte[] commandData) throws DecoderException {
            Command object = null;
            if (Arrays.asList(GET, REPORT).contains(commandCode)) {
                object = new Report(commandData);
            } else if (Arrays.asList(SUPPORTED_GET, SUPPORTED_REPORT).contains(commandCode)) {
                object = new ReportSupported(commandData);
            }
            return object;
        }

        @Override
        public void configureTags(ZWavePath path, int version) {
            configureTagInitValue(path.tag("Lookup", "Directions"), DataType.DataSet, Direction.buildDS());
            configureTagInitValue(path.tag("Lookup", "DurationModes"), DataType.DataSet, Duration.DurationMode.buildDS());
            configureTagInitValue(path.tag("Lookup", "SwitchTypes"), DataType.DataSet, SwitchType.buildDS());

            configureTagInitValue(path.tag("Start Level", "Direction"), DataType.String, Direction.UP.name, true);
            configureTag(path.tag("Start Level", "Value"), DataType.Int1, true);
            configureTagInitValue(path.tag("Start Level", "Initiate"), DataType.Boolean, false, true);

            configureTagInitValue(path.tag("Stop Level", "Initiate"), DataType.Boolean, false, true);

            configureTag(path.tag("On"), DataType.Boolean, true);
            configureTag(path.tag("Value"), DataType.Int1, true);
        }

        @Override
        public void queueInitialMessages(ZWavePath path, int version, boolean secure, boolean initial) {
            sendCommand(path, new MultiLevelSwitchCommandClass.Get(), secure);
        }

        @Override
        public QualityCode write(ZWavePath path, int version, boolean secure, Object o) {
            try {
                if (path.get(0).equals("Start Level")) {
                    if (path.get(1).equals("Initiate") && ((Boolean) o)) {
                        updateTag(path, false);
                        Direction direction = Direction.from((String) readTag(path.tag("Start Level", "Direction")));
                        int value = (Byte) readTag(path.tag("Start Level", "Value"));
                        sendCommand(path, new MultiLevelSwitchCommandClass.StartLevelChange(value, direction), secure);
                    }
                } else if (path.get(0).equals("Stop Level")) {
                    if (path.get(1).equals("Initiate") && ((Boolean) o)) {
                        updateTag(path, false);
                        sendCommand(path, new MultiLevelSwitchCommandClass.StopLevelChange(), secure);
                    }
                } else {
                    if (path.get(0).equals("On")) {
                        sendCommand(path, new MultiLevelSwitchCommandClass.Set((Boolean) o), secure);
                    } else {
                        sendCommand(path, new MultiLevelSwitchCommandClass.Set((Byte) o), secure);
                    }
                }
            } catch (Exception ex) {
                logger.error("Error writing to multi-level switch tag", ex);
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
        private final byte level;
        private Duration duration;

        @CommandClassVersion(CommandClassVersion.Version.V1)
        public Set(boolean on) {
            this(on ? 0xFF : 0);
        }

        @CommandClassVersion(CommandClassVersion.Version.V1)
        public Set(int level) {
            super(SET);
            this.level = (byte) level;
        }

        @CommandClassVersion(CommandClassVersion.Version.V2)
        public Set(boolean on, Duration duration) {
            this(on ? 0xFF : 0, duration);
        }

        @CommandClassVersion(CommandClassVersion.Version.V2)
        public Set(int level, Duration duration) {
            super(SET);
            this.level = (byte) level;
            this.duration = duration;
        }

        @Override
        protected void addCommandData(ByteArrayOutputStream result) {
            super.addCommandData(result);
            result.write(level);
            if (duration != null) {
                result.write(duration.getValue());
            }
        }
    }

    public static class Report extends CommandAdapter {
        public final byte level;
        public Byte target;
        public Duration duration;

        public Report(byte[] commandData) throws DecoderException {
            super(commandData);
            level = (byte) in.read();

            if (commandLength > 1) {
                // Version 2
                target = (byte) in.read();
                duration = new Duration(in.read());
            }
        }

        public boolean isOn() {
            return level != 0;
        }

        @Override
        public void update(ZWavePath path, int version, boolean secure) {
            updateTag(path.tag("On"), isOn());
            updateTag(path.tag("Value"), level);
        }

        @Override
        public String toString() {
            return String.format("{\"MultiLevelSwitch.Report\":{\"level\": %d}}", level);
        }
    }

    public static class StartLevelChange extends CommandAdapter {
        private final Integer startLevel;
        private final Direction direction;
        private Duration duration;
        private Direction secondary;
        private Integer secondaryStepSize;

        @CommandClassVersion(CommandClassVersion.Version.V1)
        public StartLevelChange(Direction direction) {
            this(null, direction);
        }

        @CommandClassVersion(CommandClassVersion.Version.V1)
        public StartLevelChange(Integer startLevel, Direction direction) {
            super(START_LEVEL_CHANGE);
            this.startLevel = startLevel;
            this.direction = direction;
        }

        @CommandClassVersion(CommandClassVersion.Version.V2)
        public StartLevelChange(Direction direction, Duration duration) {
            this(null, direction, duration);
        }

        @CommandClassVersion(CommandClassVersion.Version.V2)
        public StartLevelChange(Integer startLevel, Direction direction, Duration duration) {
            super(START_LEVEL_CHANGE);
            this.startLevel = startLevel;
            this.direction = direction;
            this.duration = duration;
        }

        @CommandClassVersion(CommandClassVersion.Version.V3)
        public StartLevelChange(Direction primary, Duration duration, Direction secondary, int secondaryStepSize) {
            this(null, primary, duration, secondary, secondaryStepSize);
        }

        @CommandClassVersion(CommandClassVersion.Version.V3)
        public StartLevelChange(Integer startLevel, Direction primary, Duration duration, Direction secondary, int secondaryStepSize) {
            super(START_LEVEL_CHANGE);
            this.startLevel = startLevel;
            this.direction = primary;
            this.duration = duration;
            this.secondary = secondary;
            this.secondaryStepSize = secondaryStepSize;
        }

        @Override
        protected void addCommandData(ByteArrayOutputStream result) {
            super.addCommandData(result);

            int mode = direction.value << 6;

            int tempStartLevel = 0;
            if (startLevel == null) {
                mode |= IGNORE_START_POSITION_BIT;
            } else {
                tempStartLevel = startLevel;
            }

            if (secondary != null) {
                mode |= secondary.value << 3;
            }

            result.write(mode);
            result.write(tempStartLevel);

            // Version 2
            if (duration != null) {
                result.write(duration.getValue());
            }

            // Version 3
            if (secondaryStepSize != null) {
                result.write(secondaryStepSize);
            }
        }
    }

    public static class StopLevelChange extends CommandAdapter {
        @CommandClassVersion(CommandClassVersion.Version.V1)
        public StopLevelChange() {
            super(STOP_LEVEL_CHANGE);
        }
    }

    public static class GetSupported extends CommandAdapter {
        @CommandClassVersion(CommandClassVersion.Version.V3)
        public GetSupported() {
            super(SUPPORTED_GET);
        }
    }

    public static class ReportSupported extends CommandAdapter {
        public final SwitchType primarySwitchType;
        public final SwitchType secondarySwitchType;

        public ReportSupported(byte[] commandData) throws DecoderException {
            super(commandData);
            primarySwitchType = SwitchType.from(in.read());
            secondarySwitchType = SwitchType.from(in.read());
        }

        @Override
        public String toString() {
            return String.format("{\"MultiLevel.Switch.Supported.Report\":{\"primary\": %s, \"secondary\": %s}}", primarySwitchType.toString(), secondarySwitchType.toString());
        }
    }
}
