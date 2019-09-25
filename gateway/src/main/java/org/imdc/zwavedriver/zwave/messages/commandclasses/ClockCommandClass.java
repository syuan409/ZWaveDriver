package org.imdc.zwavedriver.zwave.messages.commandclasses;

import org.imdc.zwavedriver.gateway.ZWavePath;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.Command;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandAdapter;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandArgument;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandClass;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandClassVersion;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandCode;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandProcessorAdapter;
import org.imdc.zwavedriver.zwave.messages.commandclasses.objects.Weekday;
import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import com.inductiveautomation.ignition.common.model.values.QualityCode;
import com.inductiveautomation.ignition.common.sqltags.model.types.DataType;

import java.io.ByteArrayOutputStream;

public class ClockCommandClass implements CommandClass {

    private static final CommandCode SET = new CommandCode(CommandClasses.CLOCK, 0x04);
    private static final CommandCode GET = new CommandCode(CommandClasses.CLOCK, 0x05);
    private static final CommandCode REPORT = new CommandCode(CommandClasses.CLOCK, 0x06);

    public static class Processor extends CommandProcessorAdapter {
        @Override
        public CommandCode[] getCommandCodes() {
            return new CommandCode[]{GET, SET, REPORT};
        }

        @Override
        public Command processCommandData(CommandCode commandCode, CommandArgument argument, byte[] commandData) throws DecoderException {
            return new Report(commandData);
        }

        @Override
        public void configureTags(ZWavePath path, int version) {
            configureTagInitValue(path.tag("Lookup", "Days"), DataType.DataSet, Weekday.buildDS());
            configureTag(path.tag("Clock"), DataType.String, true);
        }

        @Override
        public void queueInitialMessages(ZWavePath path, int version, boolean secure, boolean initial) {
            sendCommand(path, new ClockCommandClass.Get(), secure);
        }

        @Override
        public QualityCode write(ZWavePath path, int version, boolean secure, Object o) {
            try {
                Weekday wd;
                String clock = (String) o;
                String[] parts = clock.split(" ");
                wd = Weekday.from(parts[0]);
                String[] time = clock.split(":");
                byte hour = Byte.valueOf(time[0]);
                byte minute = Byte.valueOf(time[1]);
                sendCommand(path, new ClockCommandClass.Set(wd, hour, minute), secure);
            } catch (Exception ex) {
                logger.error("Error writing to clock tag", ex);
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
        private final Weekday weekday;
        private final byte hour, minute;

        @CommandClassVersion(CommandClassVersion.Version.V1)
        public Set(Weekday weekday, int hour, int minute) {
            super(SET);
            this.weekday = weekday;
            this.hour = (byte) hour;
            this.minute = (byte) minute;
        }

        @Override
        protected void addCommandData(ByteArrayOutputStream result) {
            super.addCommandData(result);
            result.write((weekday.value << 5) | (hour & 0x1F));
            result.write(minute);
        }
    }

    public static class Report extends CommandAdapter {
        public final Weekday weekday;
        public final byte hour, minute;

        public Report(byte[] commandData) throws DecoderException {
            super(commandData);
            int b = in.read();
            weekday = Weekday.from(b >> 5);
            hour = (byte) (b & 0x1F);
            minute = (byte) in.read();
        }

        @Override
        public void update(ZWavePath path, int version, boolean secure) {
            updateTag(path.tag("Clock"), String.format("%s %02d:%02d", weekday.name, hour, minute));
        }

        @Override
        public String toString() {
            return String.format("{\"Clock.Report\":{\"day\": %s, \"time\": %02d:%02d}}", weekday.toString(), hour, minute);
        }
    }
}
