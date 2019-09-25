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
import org.imdc.zwavedriver.zwave.messages.commandclasses.objects.Size;
import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import com.inductiveautomation.ignition.common.model.values.QualityCode;
import com.inductiveautomation.ignition.common.sqltags.model.types.DataType;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

public class WakeUpCommandClass implements CommandClass {

    private static final CommandCode SET = new CommandCode(CommandClasses.WAKEUP, 0x04);
    private static final CommandCode GET = new CommandCode(CommandClasses.WAKEUP, 0x05);
    private static final CommandCode REPORT = new CommandCode(CommandClasses.WAKEUP, 0x06);
    private static final CommandCode NOTIFICATION = new CommandCode(CommandClasses.WAKEUP, 0x07);
    private static final CommandCode NO_MORE_INFORMATION = new CommandCode(CommandClasses.WAKEUP, 0x08);
    private static final CommandCode CAPABILITIES_GET = new CommandCode(CommandClasses.WAKEUP, 0x09);
    private static final CommandCode CAPABILITIES_REPORT = new CommandCode(CommandClasses.WAKEUP, 0x0A);

    public static class Processor extends CommandProcessorAdapter {
        @Override
        public CommandCode[] getCommandCodes() {
            return new CommandCode[]{GET, SET, REPORT, NOTIFICATION, NO_MORE_INFORMATION, CAPABILITIES_GET, CAPABILITIES_REPORT};
        }

        @Override
        public Command processCommandData(CommandCode commandCode, CommandArgument argument, byte[] commandData) throws DecoderException {
            Command object = null;
            if (Arrays.asList(GET, REPORT).contains(commandCode)) {
                object = new Report(commandData);
            } else if (Arrays.asList(NOTIFICATION).contains(commandCode)) {
                object = new Notification(commandData);
            } else if (Arrays.asList(NO_MORE_INFORMATION).contains(commandCode)) {
                object = new NoMoreInformation(commandData);
            } else if (Arrays.asList(CAPABILITIES_GET, CAPABILITIES_REPORT).contains(commandCode)) {
                object = new ReportCapabilities(commandData);
            }
            return object;
        }

        @Override
        public void configureTags(ZWavePath path, int version) {
            configureTag(path.tag("NodeId"), DataType.Int1, true);
            configureTag(path.tag("Seconds"), DataType.Int4, true);
            configureTagInitValue(path.tag("Set"), DataType.Boolean, false, true);
        }

        @Override
        public void queueInitialMessages(ZWavePath path, int version, boolean secure, boolean initial) {
            sendCommand(path, new WakeUpCommandClass.Get(), secure);
        }

        @Override
        public QualityCode write(ZWavePath path, int version, boolean secure, Object o) {
            try {
                if (path.get(0).equals("Set") && ((Boolean) o)) {
                    updateTag(path, false);
                    byte nodeId = (Byte) readTag(path.tag("NodeId"));
                    int seconds = (Short) readTag(path.tag("Seconds"));
                    sendCommand(path, new WakeUpCommandClass.Set(nodeId, seconds), secure);
                }
            } catch (Exception ex) {
                logger.error("Error writing to wakeup tag", ex);
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
        private final byte nodeId;
        private final int seconds;

        @CommandClassVersion(CommandClassVersion.Version.V1)
        public Set(int nodeId, int seconds) {
            super(SET);
            this.nodeId = (byte) nodeId;
            this.seconds = seconds;
        }

        @Override
        protected void addCommandData(ByteArrayOutputStream result) {
            super.addCommandData(result);
            result.write(nodeId);
            ByteUtilities.writeIntMSB(result, Size.BIT24, seconds);
        }
    }

    public static class Report extends CommandAdapter {
        public final byte nodeId;
        public final int seconds;

        public Report(byte[] commandData) throws DecoderException {
            super(commandData);
            nodeId = (byte) in.read();
            seconds = ByteUtilities.readIntMSB(in, Size.BIT24);
        }

        @Override
        public void update(ZWavePath path, int version, boolean secure) {
            updateTag(path.tag("NodeId"), nodeId);
            updateTag(path.tag("Seconds"), seconds);
        }

        @Override
        public String toString() {
            return String.format("{\"WakeUp.Report\":{\"nodeId\": %s, \"seconds\": %d}}", Hex.asString(nodeId), seconds);
        }
    }

    public static class Notification extends CommandAdapter {
        public Notification(byte[] commandData) throws DecoderException {
            super(commandData);
        }

        @Override
        public String toString() {
            return String.format("{\"WakeUp.Notification\"}");
        }
    }

    public static class NoMoreInformation extends CommandAdapter {
        public NoMoreInformation(byte[] commandData) throws DecoderException {
            super(commandData);
        }

        @Override
        public String toString() {
            return String.format("{\"WakeUp.NoMoreInformation\"}");
        }
    }

    public static class GetCapabilities extends CommandAdapter {
        @CommandClassVersion(CommandClassVersion.Version.V2)
        public GetCapabilities() {
            super(CAPABILITIES_GET);
        }
    }

    public static class ReportCapabilities extends CommandAdapter {
        public final int minWakeUpSeconds, maxWakeUpSeconds, defaultWakeUpSeconds, wakeUpSeconds;

        public ReportCapabilities(byte[] commandData) throws DecoderException {
            super(commandData);
            minWakeUpSeconds = ByteUtilities.readIntMSB(in, Size.BIT24);
            maxWakeUpSeconds = ByteUtilities.readIntMSB(in, Size.BIT24);
            defaultWakeUpSeconds = ByteUtilities.readIntMSB(in, Size.BIT24);
            wakeUpSeconds = ByteUtilities.readIntMSB(in, Size.BIT24);
        }

        @Override
        public String toString() {
            return String.format("{\"WakeUp.Capabilities.Report\":{\"minWakeUpSeconds\": %d, \"maxWakeUpSeconds\": %d, \"defaultWakeUpSeconds\": %d, \"wakeUpSeconds\": %d,}}", minWakeUpSeconds, maxWakeUpSeconds, defaultWakeUpSeconds, wakeUpSeconds);
        }
    }
}
