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
import org.imdc.zwavedriver.zwave.messages.commandclasses.objects.Duration;
import org.imdc.zwavedriver.zwave.messages.commandclasses.objects.Handle;
import org.imdc.zwavedriver.zwave.messages.commandclasses.objects.LockMode;
import org.imdc.zwavedriver.zwave.messages.commandclasses.objects.LockOperationType;
import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import com.inductiveautomation.ignition.common.model.values.QualityCode;
import com.inductiveautomation.ignition.common.sqltags.model.types.DataType;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;

public class DoorLockCommandClass implements CommandClass {

    private static final CommandCode SET = new CommandCode(CommandClasses.DOOR_LOCK, 0x01);
    private static final CommandCode GET = new CommandCode(CommandClasses.DOOR_LOCK, 0x02);
    private static final CommandCode REPORT = new CommandCode(CommandClasses.DOOR_LOCK, 0x03);
    private static final CommandCode CONFIGURATION_SET = new CommandCode(CommandClasses.DOOR_LOCK, 0x04);
    private static final CommandCode CONFIGURATION_GET = new CommandCode(CommandClasses.DOOR_LOCK, 0x05);
    private static final CommandCode CONFIGURATION_REPORT = new CommandCode(CommandClasses.DOOR_LOCK, 0x06);
    private static final CommandCode CAPABILITIES_GET = new CommandCode(CommandClasses.DOOR_LOCK, 0x07);
    private static final CommandCode CAPABILITIES_REPORT = new CommandCode(CommandClasses.DOOR_LOCK, 0x08);

    private static final int CONSTANT = 0xFE;

    public static class Processor extends CommandProcessorAdapter {

        @Override
        public CommandCode[] getCommandCodes() {
            return new CommandCode[]{SET, GET, REPORT, CONFIGURATION_SET, CONFIGURATION_GET, CONFIGURATION_REPORT, CAPABILITIES_GET, CAPABILITIES_REPORT};
        }

        @Override
        public Command processCommandData(CommandCode commandCode, CommandArgument argument, byte[] commandData) throws DecoderException {
            Command object = null;
            if (Arrays.asList(GET, REPORT).contains(commandCode)) {
                object = new Report(commandData);
            } else if (Arrays.asList(CONFIGURATION_GET, CONFIGURATION_REPORT).contains(commandCode)) {
                object = new ReportConfiguration(commandData);
            } else if (Arrays.asList(CAPABILITIES_GET, CAPABILITIES_REPORT).contains(commandCode)) {
                object = new ReportCapabilities(commandData);
            }
            return object;
        }

        @Override
        public void configureTags(ZWavePath path, int version) {
            configureTagInitValue(path.tag("Lookup", "DurationModes"), DataType.DataSet, Duration.DurationMode.buildDS());
            configureTagInitValue(path.tag("Lookup", "Handles"), DataType.DataSet, Handle.buildDS());
            configureTagInitValue(path.tag("Lookup", "LockModes"), DataType.DataSet, LockMode.buildDS());
            configureTagInitValue(path.tag("Lookup", "LockOperationTypes"), DataType.DataSet, LockOperationType.buildDS());

            configureTag(path.tag("LockMode"), DataType.String, true);
            configureTag(path.tag("DoorClosed"), DataType.Boolean);
            configureTag(path.tag("BoltUnlocked"), DataType.Boolean);
            configureTag(path.tag("LatchClosed"), DataType.Boolean);
            configureTag(path.tag("LockOperationType"), DataType.String);
        }

        @Override
        public void queueInitialMessages(ZWavePath path, int version, boolean secure, boolean initial) {
            sendCommand(path, new DoorLockCommandClass.Get(), secure);
        }

        @Override
        public QualityCode write(ZWavePath path, int version, boolean secure, Object o) {
            try {
                sendCommand(path, new DoorLockCommandClass.Set(LockMode.from((String) o)), secure);
            } catch (Exception ex) {
                logger.error("Error writing to door lock tag", ex);
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
        private final LockMode mode;

        @CommandClassVersion(CommandClassVersion.Version.V1)
        public Set(LockMode mode) {
            super(SET);
            this.mode = mode;
        }

        @Override
        protected void addCommandData(ByteArrayOutputStream result) {
            super.addCommandData(result);
            result.write(mode.value);
        }
    }

    public static class Report extends CommandAdapter {
        public final LockMode mode;
        public final List<Handle> handles;
        public final boolean doorClosed, boltUnlocked, latchClosed;
        public final LockOperationType operationType;
        public Integer timeoutMinutes, timeoutSeconds;
        public LockMode targetLockMode;
        public Duration duration;

        public Report(byte[] commandData) throws DecoderException {
            super(commandData);

            mode = LockMode.from(in.read());
            handles = Handle.getHandles(in.read());
            int b = in.read();
            doorClosed = (b & 0x01) != 0;
            boltUnlocked = (b & 0x02) != 0;
            latchClosed = (b & 0x04) != 0;
            b = in.read();
            timeoutMinutes = b == CONSTANT ? null : b;
            operationType = b == CONSTANT ? LockOperationType.CONSTANT : LockOperationType.TIMED;
            b = in.read();
            timeoutSeconds = b == CONSTANT ? null : b;

            if (commandLength >= 6) {
                // Version 3
                targetLockMode = LockMode.from(in.read());
                duration = new Duration(in.read());
            }
        }

        @Override
        public void update(ZWavePath path, int version, boolean secure) {
            updateTag(path.tag("LockMode"), mode.name);
            updateTag(path.tag("DoorClosed"), doorClosed);
            updateTag(path.tag("BoltUnlocked"), boltUnlocked);
            updateTag(path.tag("LatchClosed"), latchClosed);
            updateTag(path.tag("LockOperationType"), operationType.name);
        }

        @Override
        public String toString() {
            return String.format("{\"DoorLock.Report\":{\"mode\": %s, \"doorClosed\": %b, \"boltUnlocked\": %b, \"latchClosed\": %b}}", mode.toString(), doorClosed, boltUnlocked, latchClosed);
        }
    }

    public static class GetConfiguration extends CommandAdapter {
        @CommandClassVersion(CommandClassVersion.Version.V1)
        public GetConfiguration() {
            super(GET);
        }
    }

    public static class SetConfiguration extends CommandAdapter {
        private final LockOperationType operationType;
        private final Handle handle;
        private Integer timeoutMinutes, timeoutSeconds;
        private Integer autoRelockTime;
        private Integer holdReleaseTime;
        private boolean twistAssist, blockToBlock;

        @CommandClassVersion(CommandClassVersion.Version.V1)
        public SetConfiguration(LockOperationType operationType, Handle handle) {
            this(operationType, handle, CONSTANT, CONSTANT);
        }

        @CommandClassVersion(CommandClassVersion.Version.V1)
        public SetConfiguration(LockOperationType operationType, Handle handle, int timeoutMinutes, int timeoutSeconds) {
            super(CONFIGURATION_SET);
            this.operationType = operationType;
            this.handle = handle;
            if (operationType == LockOperationType.CONSTANT) {
                this.timeoutMinutes = CONSTANT;
                this.timeoutSeconds = CONSTANT;
            } else {
                this.timeoutMinutes = timeoutMinutes;
                this.timeoutSeconds = timeoutSeconds;
            }
        }

        @CommandClassVersion(CommandClassVersion.Version.V4)
        public SetConfiguration(LockOperationType operationType, Handle handle, int timeoutMinutes, int timeoutSeconds, int autoRelockTime, int holdReleaseTime, boolean twistAssist, boolean blockToBlock) {
            this(operationType, handle, timeoutMinutes, timeoutSeconds);
            this.autoRelockTime = autoRelockTime;
            this.holdReleaseTime = holdReleaseTime;
            this.twistAssist = twistAssist;
            this.blockToBlock = blockToBlock;
        }

        @Override
        protected void addCommandData(ByteArrayOutputStream result) {
            super.addCommandData(result);
            result.write(operationType.value);
            result.write(handle.value);
            result.write(timeoutMinutes);
            result.write(timeoutSeconds);

            if (autoRelockTime != null) {
                ByteUtilities.writeInt16MSB(result, autoRelockTime);
                ByteUtilities.writeInt16MSB(result, holdReleaseTime);
                result.write((blockToBlock ? 0x02 : 0x00) | (twistAssist ? 0x01 : 0x00));
            }
        }
    }

    public static class ReportConfiguration extends CommandAdapter {
        public final LockOperationType operationType;
        public final List<Handle> handles;
        public Integer timeoutMinutes, timeoutSeconds;
        private Integer autoRelockTime;
        private Integer holdReleaseTime;
        private boolean twistAssist, blockToBlock;

        public ReportConfiguration(byte[] commandData) throws DecoderException {
            super(commandData);
            operationType = LockOperationType.from(in.read());
            handles = Handle.getHandles(in.read());
            int b = in.read();
            timeoutMinutes = b == CONSTANT ? null : b;
            b = in.read();
            timeoutSeconds = b == CONSTANT ? null : b;

            if (commandLength >= 5) {
                // Version 4
                autoRelockTime = ByteUtilities.readInt16MSB(in);
                holdReleaseTime = ByteUtilities.readInt16MSB(in);
                b = in.read();
                twistAssist = (b & 0x01) != 0;
                blockToBlock = (b & 0x02) != 0;
            }
        }

        @Override
        public String toString() {
            return String.format("{\"DoorLock.Configuration.Report\":{\"operationType\": %s, \"handles\": [%s]}}", operationType.toString(), Hex.asString(handles));
        }
    }

    public static class GetCapabilities extends CommandAdapter {
        @CommandClassVersion(CommandClassVersion.Version.V4)
        public GetCapabilities() {
            super(CAPABILITIES_GET);
        }
    }

    public static class ReportCapabilities extends CommandAdapter {
        public final List<LockOperationType> supportedOperationTypes;
        public final List<LockMode> supportedModes;
        public final List<Handle> supportedHandles;
        public final boolean supportsDoor, supportsBolt, supportsLatch;
        public final boolean ars, hrs, tas, btbs;

        public ReportCapabilities(byte[] commandData) throws DecoderException {
            super(commandData);
            int length = in.read();
            supportedOperationTypes = LockOperationType.getTypes(Hex.getMaskInts(in, length));
            supportedModes = LockMode.getModes(Hex.getMaskInts(in, length));
            supportedHandles = Handle.getHandles(in.read());
            int b = in.read();
            supportsDoor = (b & 0x01) != 0;
            supportsBolt = (b & 0x02) != 0;
            supportsLatch = (b & 0x04) != 0;
            b = in.read();
            ars = (b & 0x01) != 0;
            hrs = (b & 0x02) != 0;
            tas = (b & 0x04) != 0;
            btbs = (b & 0x08) != 0;
        }

        @Override
        public String toString() {
            return String.format("{\"Version.Capabilities.Report\":{\"operationTypes\": [%s], \"modes\": [%s], \"handles\": [%s], \"door\": %b, \"bolt\": %b, \"latch\": %b}}", Hex.asString(supportedOperationTypes), Hex.asString(supportedModes), Hex.asString(supportedHandles), supportsDoor, supportsBolt, supportsLatch);
        }
    }
}
