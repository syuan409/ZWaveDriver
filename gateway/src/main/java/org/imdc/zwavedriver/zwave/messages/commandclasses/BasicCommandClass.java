package org.imdc.zwavedriver.zwave.messages.commandclasses;

import org.imdc.zwavedriver.gateway.ZWavePath;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.Command;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandAdapter;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandArgument;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandClass;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandClassVersion;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandCode;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandProcessorAdapter;
import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import com.inductiveautomation.ignition.common.model.values.QualityCode;
import com.inductiveautomation.ignition.common.sqltags.model.types.DataType;

import java.io.ByteArrayOutputStream;

public class BasicCommandClass implements CommandClass {

    private static final CommandCode SET = new CommandCode(CommandClasses.BASIC, 0x01);
    private static final CommandCode GET = new CommandCode(CommandClasses.BASIC, 0x02);
    private static final CommandCode REPORT = new CommandCode(CommandClasses.BASIC, 0x03);

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
            configureTag(path.tag("Value"), DataType.Int1, true);
        }

        @Override
        public void queueInitialMessages(ZWavePath path, int version, boolean secure, boolean initial) {
            sendCommand(path, new BasicCommandClass.Get(), secure);
        }

        @Override
        public QualityCode write(ZWavePath path, int version, boolean secure, Object o) {
            sendCommand(path, new BasicCommandClass.Set((Byte) o), secure);
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
        private final byte value;

        @CommandClassVersion(CommandClassVersion.Version.V1)
        public Set(boolean on) {
            super(SET);
            this.value = (byte) (on ? 0xFF : 0);
        }

        @CommandClassVersion(CommandClassVersion.Version.V1)
        public Set(int value) {
            super(SET);
            this.value = (byte) value;
        }

        @Override
        protected void addCommandData(ByteArrayOutputStream result) {
            super.addCommandData(result);
            result.write(value);
        }
    }

    public static class Report extends CommandAdapter {
        public final byte value;
        public Byte targetValue;
        public Byte duration;

        public Report(byte[] commandData) throws DecoderException {
            super(commandData);
            value = (byte) in.read();
            if (commandLength == 2) {
                // Version 2
                targetValue = (byte) in.read();
                duration = (byte) in.read();
            }
        }

        public boolean isOn() {
            return value != 0;
        }

        @Override
        public void update(ZWavePath path, int version, boolean secure) {
            updateTag(path.tag("Value"), value);
        }

        @Override
        public String toString() {
            return String.format("{\"Basic.Report\":{\"value\": %d}}", value);
        }
    }
}
