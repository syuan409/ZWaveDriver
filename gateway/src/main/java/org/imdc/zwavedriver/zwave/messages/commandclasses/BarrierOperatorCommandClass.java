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
import org.imdc.zwavedriver.zwave.messages.commandclasses.objects.Barrier;
import org.imdc.zwavedriver.zwave.messages.commandclasses.objects.BarrierState;
import org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SubsystemType;
import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import com.inductiveautomation.ignition.common.model.values.QualityCode;
import com.inductiveautomation.ignition.common.sqltags.model.types.DataType;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;

public class BarrierOperatorCommandClass implements CommandClass {

    private static final CommandCode SET = new CommandCode(CommandClasses.BARRIER_OPERATOR, 0x01);
    private static final CommandCode GET = new CommandCode(CommandClasses.BARRIER_OPERATOR, 0x02);
    private static final CommandCode REPORT = new CommandCode(CommandClasses.BARRIER_OPERATOR, 0x03);
    private static final CommandCode SIGNALING_CAPABILITIES_GET = new CommandCode(CommandClasses.BARRIER_OPERATOR, 0x04);
    private static final CommandCode SIGNALING_CAPABILITIES_REPORT = new CommandCode(CommandClasses.BARRIER_OPERATOR, 0x05);
    private static final CommandCode SIGNAL_SET = new CommandCode(CommandClasses.BARRIER_OPERATOR, 0x06);
    private static final CommandCode SIGNAL_GET = new CommandCode(CommandClasses.BARRIER_OPERATOR, 0x07);
    private static final CommandCode SIGNAL_REPORT = new CommandCode(CommandClasses.BARRIER_OPERATOR, 0x08);

    public static class Processor extends CommandProcessorAdapter {
        @Override
        public CommandCode[] getCommandCodes() {
            return new CommandCode[]{GET, SET, REPORT, SIGNALING_CAPABILITIES_GET, SIGNALING_CAPABILITIES_REPORT, SIGNAL_SET, SIGNAL_GET, SIGNAL_REPORT};
        }

        @Override
        public Command processCommandData(CommandCode commandCode, CommandArgument argument, byte[] commandData) throws DecoderException {
            Command object = null;
            if (Arrays.asList(GET, REPORT).contains(commandCode)) {
                object = new Report(commandData);
            } else if (Arrays.asList(SIGNALING_CAPABILITIES_GET, SIGNALING_CAPABILITIES_REPORT).contains(commandCode)) {
                object = new ReportSignalingCapabilities(commandData);
            } else if (Arrays.asList(SIGNAL_GET, SIGNAL_REPORT).contains(commandCode)) {
                object = new ReportSignal(commandData);
            }
            return object;
        }

        @Override
        public void configureTags(ZWavePath path, int version) {
            configureTagInitValue(path.tag("Lookup", "States"), DataType.DataSet, BarrierState.buildDS());
            configureTagInitValue(path.tag("Lookup", "SubsystemTypes"), DataType.DataSet, SubsystemType.buildDS());

            configureTag(path.tag("Value"), DataType.Int1, true);
            configureTag(path.tag("State"), DataType.String, true);
            configureTag(path.tag("Signals", SubsystemType.AUDIBLE.name), DataType.Boolean);
            configureTag(path.tag("Signals", SubsystemType.VISUAL.name), DataType.Boolean);
        }

        @Override
        public void queueInitialMessages(ZWavePath path, int version, boolean secure, boolean initial) {
            sendCommand(path, new BarrierOperatorCommandClass.Get(), secure);
            if(initial) {
                sendCommand(path, new BarrierOperatorCommandClass.GetSignalingCapabilities(), secure);
            }
        }

        @Override
        public QualityCode write(ZWavePath path, int version, boolean secure, Object o) {
            try {
                Barrier b;
                if (o instanceof String) {
                    b = new Barrier(BarrierState.from((String) o));
                } else {
                    b = new Barrier(BarrierState.from((Byte) o));
                }
                sendCommand(path, new BarrierOperatorCommandClass.Set(b), secure);
            } catch (Exception ex) {
                logger.error("Error writing to barrier tag", ex);
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
        private final Barrier barrier;

        @CommandClassVersion(CommandClassVersion.Version.V1)
        public Set(Barrier barrier) {
            super(SET);
            this.barrier = barrier;
        }

        @Override
        protected void addCommandData(ByteArrayOutputStream result) {
            super.addCommandData(result);
            result.write(barrier.value);
        }
    }

    public static class Report extends CommandAdapter {
        public final Barrier barrier;

        public Report(byte[] commandData) throws DecoderException {
            super(commandData);
            barrier = new Barrier(in.read());
        }

        @Override
        public void update(ZWavePath path, int version, boolean secure) {
            updateTag(path.tag("Value"), barrier.value);
            updateTag(path.tag("State"), barrier.state.name);
        }

        @Override
        public String toString() {
            return String.format("{\"BarrierOperator.Report\":{\"barrier\": %s}}", barrier);
        }
    }

    public static class GetSignalingCapabilities extends CommandAdapter {
        @CommandClassVersion(CommandClassVersion.Version.V1)
        public GetSignalingCapabilities() {
            super(SIGNALING_CAPABILITIES_GET);
        }
    }

    public static class ReportSignalingCapabilities extends CommandAdapter {
        public final List<SubsystemType> subsystemTypes;

        public ReportSignalingCapabilities(byte[] commandData) throws DecoderException {
            super(commandData);
            subsystemTypes = SubsystemType.getTypes(Hex.getMaskInts(in, commandLength));
        }

        @Override
        public void update(ZWavePath path, int version, boolean secure) {
            for (SubsystemType subsystemType : subsystemTypes) {
                sendCommand(path, new BarrierOperatorCommandClass.GetSignal(subsystemType), secure);
            }
        }

        @Override
        public String toString() {
            return String.format("{\"BarrierOperator.Capabilities.Report\":{\"subsystemTypes\": %s}}", Hex.asString(subsystemTypes));
        }
    }

    public static class GetSignal extends CommandAdapter {
        private final SubsystemType type;

        @CommandClassVersion(CommandClassVersion.Version.V1)
        public GetSignal(SubsystemType type) {
            super(SIGNAL_GET);
            this.type = type;
        }

        @Override
        protected void addCommandData(ByteArrayOutputStream result) {
            super.addCommandData(result);
            result.write(type.value);
        }
    }

    public static class SetSignal extends CommandAdapter {
        private final SubsystemType type;
        private final boolean value;

        @CommandClassVersion(CommandClassVersion.Version.V1)
        public SetSignal(SubsystemType type, boolean value) {
            super(SET);
            this.type = type;
            this.value = value;
        }

        @Override
        protected void addCommandData(ByteArrayOutputStream result) {
            super.addCommandData(result);
            result.write(type.value);
            result.write(value ? 0xFF : 0x00);
        }
    }

    public static class ReportSignal extends CommandAdapter {
        public final SubsystemType type;
        public final boolean value;

        public ReportSignal(byte[] commandData) throws DecoderException {
            super(commandData);
            type = SubsystemType.from(in.read());
            value = (in.read() & 0xFF) == 0xFF;
        }

        @Override
        public void update(ZWavePath path, int version, boolean secure) {
            if (type != SubsystemType.UNDEFINED) {
                updateTag(path.tag("Signals", type.name), value);
            }
        }

        @Override
        public String toString() {
            return String.format("{\"BarrierOperator.Signal.Report\":{\"subsystemType\": %s, \"value\": %b}}", type, value);
        }
    }
}
