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
import org.imdc.zwavedriver.zwave.messages.commandclasses.objects.Color;
import org.imdc.zwavedriver.zwave.messages.commandclasses.objects.Direction;
import org.imdc.zwavedriver.zwave.messages.commandclasses.objects.Duration;
import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import com.inductiveautomation.ignition.common.model.values.QualityCode;
import com.inductiveautomation.ignition.common.sqltags.model.types.DataType;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;

public class ColorSwitchCommandClass implements CommandClass {

    private static final CommandCode SET = new CommandCode(CommandClasses.COLOR_SWITCH, 0x05);
    private static final CommandCode GET = new CommandCode(CommandClasses.COLOR_SWITCH, 0x03);
    private static final CommandCode REPORT = new CommandCode(CommandClasses.COLOR_SWITCH, 0x04);
    private static final CommandCode START_LEVEL_CHANGE = new CommandCode(CommandClasses.COLOR_SWITCH, 0x06);
    private static final CommandCode STOP_LEVEL_CHANGE = new CommandCode(CommandClasses.COLOR_SWITCH, 0x07);
    private static final CommandCode SUPPORTED_GET = new CommandCode(CommandClasses.COLOR_SWITCH, 0x01);
    private static final CommandCode SUPPORTED_REPORT = new CommandCode(CommandClasses.COLOR_SWITCH, 0x02);

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
            configureTagInitValue(path.tag("Lookup", "Colors"), DataType.DataSet, Color.buildDS());
            configureTagInitValue(path.tag("Lookup", "Directions"), DataType.DataSet, Direction.buildDS());
            configureTagInitValue(path.tag("Lookup", "DurationModes"), DataType.DataSet, Duration.DurationMode.buildDS());

            configureTagInitValue(path.tag("Start Level", "Direction"), DataType.String, Direction.UP.name, true);
            configureTag(path.tag("Start Level", "Color"), DataType.String, true);
            configureTag(path.tag("Start Level", "Value"), DataType.Int1, true);
            configureTagInitValue(path.tag("Start Level", "Initiate"), DataType.Boolean, false, true);

            configureTag(path.tag("Stop Level", "Color"), DataType.String, true);
            configureTagInitValue(path.tag("Stop Level", "Initiate"), DataType.Boolean, false, true);

            configureTagInitValue(path.tag("Color"), DataType.String, "#FFFFFF", true);

            for (Color c : Color.values()) {
                configureTagInitValue(path.tag("Colors", c.name, "Supported"), DataType.Boolean, false);
                configureTag(path.tag("Colors", c.name, "Value"), DataType.Int1, true);
                configureTagInitValue(path.tag("Colors", c.name, "Refresh"), DataType.Boolean, false, true);
            }
        }

        @Override
        public void queueInitialMessages(ZWavePath path, int version, boolean secure, boolean initial) {
            sendCommand(path, new ColorSwitchCommandClass.GetSupported(), secure);
        }

        @Override
        public QualityCode write(ZWavePath path, int version, boolean secure, Object o) {
            try {
                if (path.get(0).equals("Start Level")) {
                    if (path.get(1).equals("Initiate") && ((Boolean) o)) {
                        updateTag(path, false);
                        Color color = Color.from((String) readTag(path.tag("Start Level", "Color")));
                        Direction direction = Direction.from((String) readTag(path.tag("Start Level", "Direction")));
                        int value = (Byte) readTag(path.tag("Start Level", "Value"));
                        sendCommand(path, new ColorSwitchCommandClass.StartLevelChange(color, value, direction), secure);
                    }
                } else if (path.get(0).equals("Stop Level")) {
                    if (path.get(1).equals("Initiate") && ((Boolean) o)) {
                        updateTag(path, false);
                        Color color = Color.from((String) readTag(path.tag("Stop Level", "Color")));
                        sendCommand(path, new ColorSwitchCommandClass.StopLevelChange(color), secure);
                    }
                } else if (path.get(0).equals("Color")) {
                    sendCommand(path, new ColorSwitchCommandClass.Set((String) o), secure);
                } else {
                    Color color = Color.from(path.get(1));

                    if (path.get(2).equals("Refresh") && ((Boolean) o)) {
                        updateTag(path, false);
                        sendCommand(path, new ColorSwitchCommandClass.Get(color), secure);
                    } else {
                        sendCommand(path, new ColorSwitchCommandClass.Set(color, (Byte) o), secure);
                    }
                }
            } catch (Exception ex) {
                logger.error("Error writing to color tag", ex);
                return QualityCode.Error;
            }
            return QualityCode.Good;
        }
    }

    public static class Get extends CommandAdapter {
        private final Color color;

        @CommandClassVersion(CommandClassVersion.Version.V1)
        public Get(Color color) {
            super(GET);
            this.color = color;
        }

        @Override
        protected void addCommandData(ByteArrayOutputStream result) {
            super.addCommandData(result);
            result.write(color.value);
        }
    }

    public static class Set extends CommandAdapter {
        private final Color color;
        private final Byte value;
        private String hexColor;
        private Duration duration;

        @CommandClassVersion(CommandClassVersion.Version.V1)
        public Set(Color color, int value) {
            this(color, value, null);
        }

        @CommandClassVersion(CommandClassVersion.Version.V2)
        public Set(Color color, int value, Duration duration) {
            super(SET);
            this.hexColor = null;
            this.color = color;
            this.value = (byte) value;
            this.duration = duration;
        }

        @CommandClassVersion(CommandClassVersion.Version.V1)
        public Set(String color){
            this(color, null);
        }

        @CommandClassVersion(CommandClassVersion.Version.V2)
        public Set(String color, Duration duration) {
            super(SET);
            this.color = null;
            this.value = null;
            this.hexColor = color;
            this.duration = duration;
        }

        @Override
        protected void addCommandData(ByteArrayOutputStream result) {
            super.addCommandData(result);

            if(hexColor != null){
                result.write(0x03);
                java.awt.Color javaColor = java.awt.Color.decode(hexColor);
                if(javaColor == null){
                    javaColor = java.awt.Color.getColor(hexColor);
                }

                if (javaColor != null) {
                    result.write(Color.RED.value);
                    result.write(javaColor.getRed());
                    result.write(Color.GREEN.value);
                    result.write(javaColor.getGreen());
                    result.write(Color.BLUE.value);
                    result.write(javaColor.getBlue());
                }
            } else {
                result.write(0x01);
                result.write(color.value);
                result.write(value);
            }

            // Version 2
            if (duration != null) {
                result.write(duration.getValue());
            }
        }
    }

    public static class Report extends CommandAdapter {
        public final Color color;
        public final byte value;
        public Byte target;
        public Duration duration;

        public Report(byte[] commandData) throws DecoderException {
            super(commandData);
            color = Color.from(in.read());
            value = (byte) in.read();

            if (commandLength > 2) {
                // Version 3
                target = (byte) in.read();
                duration = new Duration(in.read());
            }
        }

        @Override
        public void update(ZWavePath path, int version, boolean secure) {
            updateTag(path.tag("Colors", color.name, "Value"), value);

            //#123456
            String hexColor = (String) readTag(path.tag("Color"));
            String indHexColor = Hex.asString(value);
            if(color == Color.RED){
                hexColor = "#" + indHexColor + hexColor.substring(3);
                updateTag(path.tag("Color"), hexColor);
            } else if(color == Color.GREEN){
                hexColor = "#" + hexColor.substring(1, 3) + indHexColor + hexColor.substring(5);
                updateTag(path.tag("Color"), hexColor);
            } else if(color == Color.BLUE){
                hexColor = "#" + hexColor.substring(1, 5) + indHexColor;
                updateTag(path.tag("Color"), hexColor);
            }
        }

        @Override
        public String toString() {
            return String.format("{\"ColorSwitch.Report\":{\"color\": %s, \"value\": %d}}", color.toString(), value);
        }
    }

    public static class StartLevelChange extends CommandAdapter {
        private final Color color;
        private final Integer startLevel;
        private final Direction direction;
        private final Duration duration;

        @CommandClassVersion(CommandClassVersion.Version.V1)
        public StartLevelChange(Color color, Direction direction) {
            this(color, null, direction);
        }

        @CommandClassVersion(CommandClassVersion.Version.V1)
        public StartLevelChange(Color color, Integer startLevel, Direction direction) {
            this(color, startLevel, direction, null);
        }

        @CommandClassVersion(CommandClassVersion.Version.V3)
        public StartLevelChange(Color color, Direction direction, Duration duration) {
            this(color, null, direction, duration);
        }

        @CommandClassVersion(CommandClassVersion.Version.V3)
        public StartLevelChange(Color color, Integer startLevel, Direction direction, Duration duration) {
            super(START_LEVEL_CHANGE);
            this.color = color;
            this.startLevel = startLevel;
            this.direction = direction;
            this.duration = duration;
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

            result.write(mode);
            result.write(color.value);
            result.write(tempStartLevel);

            // Version 3
            if (duration != null) {
                result.write(duration.getValue());
            }
        }
    }

    public static class StopLevelChange extends CommandAdapter {
        private final Color color;

        @CommandClassVersion(CommandClassVersion.Version.V1)
        public StopLevelChange(Color color) {
            super(STOP_LEVEL_CHANGE);
            this.color = color;
        }

        @Override
        protected void addCommandData(ByteArrayOutputStream result) {
            super.addCommandData(result);
            result.write(color.value);
        }
    }

    public static class GetSupported extends CommandAdapter {
        @CommandClassVersion(CommandClassVersion.Version.V1)
        public GetSupported() {
            super(SUPPORTED_GET);
        }
    }

    public static class ReportSupported extends CommandAdapter {
        public final List<Color> supportedColors;

        public ReportSupported(byte[] commandData) throws DecoderException {
            super(commandData);
            supportedColors = Color.getColors(Hex.getMaskInts(in, commandLength));
        }

        @Override
        public void update(ZWavePath path, int version, boolean secure) {
            for (Color c : supportedColors) {
                updateTag(path.tag("Colors", c.name, "Supported"), true);
                sendCommand(path, new ColorSwitchCommandClass.Get(c), secure);
            }
        }

        @Override
        public String toString() {
            return String.format("{\"Color.Switch.Supported.Report\":{\"colors\": [%s]}}", Hex.asString(supportedColors));
        }
    }
}
