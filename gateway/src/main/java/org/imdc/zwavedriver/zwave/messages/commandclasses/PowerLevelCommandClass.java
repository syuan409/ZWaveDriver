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
import org.imdc.zwavedriver.zwave.messages.commandclasses.objects.PowerLevel;
import org.imdc.zwavedriver.zwave.messages.commandclasses.objects.PowerLevelStatus;
import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import com.inductiveautomation.ignition.common.model.values.QualityCode;
import com.inductiveautomation.ignition.common.sqltags.model.types.DataType;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

public class PowerLevelCommandClass implements CommandClass {

    private static final CommandCode SET = new CommandCode(CommandClasses.POWER_LEVEL, 0x01);
    private static final CommandCode GET = new CommandCode(CommandClasses.POWER_LEVEL, 0x02);
    private static final CommandCode REPORT = new CommandCode(CommandClasses.POWER_LEVEL, 0x03);
    private static final CommandCode TEST_NODE_SET = new CommandCode(CommandClasses.POWER_LEVEL, 0x04);
    private static final CommandCode TEST_NODE_GET = new CommandCode(CommandClasses.POWER_LEVEL, 0x05);
    private static final CommandCode TEST_NODE_REPORT = new CommandCode(CommandClasses.POWER_LEVEL, 0x06);

    public static class Processor extends CommandProcessorAdapter {
        @Override
        public CommandCode[] getCommandCodes() {
            return new CommandCode[]{GET, SET, REPORT, TEST_NODE_GET, TEST_NODE_SET, TEST_NODE_REPORT};
        }

        @Override
        public Command processCommandData(CommandCode commandCode, CommandArgument argument, byte[] commandData) throws DecoderException {
            Command object = null;
            if (Arrays.asList(GET, REPORT).contains(commandCode)) {
                object = new Report(commandData);
            } else if (Arrays.asList(TEST_NODE_GET, TEST_NODE_REPORT).contains(commandCode)) {
                object = new TestNodeReport(commandData);
            }
            return object;
        }

        @Override
        public void configureTags(ZWavePath path, int version) {
            configureTagInitValue(path.tag("Lookup", "Levels"), DataType.DataSet, PowerLevel.buildDS());
            configureTagInitValue(path.tag("Lookup", "LevelStatus"), DataType.DataSet, PowerLevelStatus.buildDS());
            configureTag(path.tag("Level"), DataType.String, true);
        }

        @Override
        public void queueInitialMessages(ZWavePath path, int version, boolean secure, boolean initial) {
            sendCommand(path, new PowerLevelCommandClass.Get(), secure);
        }

        @Override
        public QualityCode write(ZWavePath path, int version, boolean secure, Object o) {
            try {
                sendCommand(path, new PowerLevelCommandClass.Set(PowerLevel.from((String) o), 0), secure);
            } catch (Exception ex) {
                logger.error("Error writing to power level tag", ex);
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
        private final PowerLevel powerLevel;
        private final byte timeout;

        @CommandClassVersion(CommandClassVersion.Version.V1)
        public Set(PowerLevel powerLevel, int timeout) {
            super(SET);
            this.powerLevel = powerLevel;
            this.timeout = (byte) timeout;
        }

        @Override
        protected void addCommandData(ByteArrayOutputStream result) {
            super.addCommandData(result);
            result.write(powerLevel.value);
            result.write(timeout);
        }
    }

    public static class Report extends CommandAdapter {
        public final PowerLevel powerLevel;
        public final byte timeout;

        public Report(byte[] commandData) throws DecoderException {
            super(commandData);
            powerLevel = PowerLevel.from(in.read());
            timeout = (byte) in.read();
        }

        @Override
        public void update(ZWavePath path, int version, boolean secure) {
            updateTag(path.tag("Level"), powerLevel.name);
        }

        @Override
        public String toString() {
            return String.format("{\"PowerLevel.Report\":{\"level\": %s, \"timeout\": %d}}", powerLevel.toString(), timeout);
        }
    }

    public static class TestNodeGet extends CommandAdapter {
        @CommandClassVersion(CommandClassVersion.Version.V1)
        public TestNodeGet() {
            super(TEST_NODE_GET);
        }
    }

    public static class TestNodeSet extends CommandAdapter {
        private final byte nodeId;
        private final PowerLevel powerLevel;
        private final int frameCount;

        @CommandClassVersion(CommandClassVersion.Version.V1)
        public TestNodeSet(int nodeId, PowerLevel powerLevel, int frameCount) {
            super(TEST_NODE_SET);
            this.nodeId = (byte) nodeId;
            this.powerLevel = powerLevel;
            this.frameCount = (byte) frameCount;
        }

        @Override
        protected void addCommandData(ByteArrayOutputStream result) {
            super.addCommandData(result);
            result.write(nodeId);
            result.write(powerLevel.value);
            ByteUtilities.writeInt16MSB(result, frameCount);
        }
    }

    public static class TestNodeReport extends CommandAdapter {
        private final byte nodeId;
        public final PowerLevelStatus status;
        private final int frameCount;

        public TestNodeReport(byte[] commandData) throws DecoderException {
            super(commandData);
            nodeId = (byte) in.read();
            status = PowerLevelStatus.from(in.read());
            frameCount = ByteUtilities.readInt16MSB(in);
        }

        @Override
        public String toString() {
            return String.format("{\"PowerLevel.TestNode.Report\":{\"nodeId\": %s, \"status\": %s, \"frameCount\": %d}}", Hex.asString(nodeId), status.toString(), frameCount);
        }
    }
}
