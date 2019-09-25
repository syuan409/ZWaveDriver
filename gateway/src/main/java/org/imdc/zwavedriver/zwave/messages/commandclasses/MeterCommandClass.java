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
import org.imdc.zwavedriver.zwave.messages.commandclasses.objects.Meter;
import org.imdc.zwavedriver.zwave.messages.commandclasses.objects.MeterType;
import org.imdc.zwavedriver.zwave.messages.commandclasses.objects.MeterUnit;
import org.imdc.zwavedriver.zwave.messages.commandclasses.objects.RateType;
import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import com.inductiveautomation.ignition.common.sqltags.model.types.DataType;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MeterCommandClass implements CommandClass {

    private static final CommandCode GET = new CommandCode(CommandClasses.METER, 0x01);
    private static final CommandCode REPORT = new CommandCode(CommandClasses.METER, 0x02);
    private static final CommandCode SUPPORTED_GET = new CommandCode(CommandClasses.METER, 0x03);
    private static final CommandCode SUPPORTED_REPORT = new CommandCode(CommandClasses.METER, 0x04);
    private static final CommandCode RESET = new CommandCode(CommandClasses.METER, 0x05);

    public static class Processor extends CommandProcessorAdapter {
        @Override
        public CommandCode[] getCommandCodes() {
            return new CommandCode[]{GET, REPORT, SUPPORTED_GET, SUPPORTED_REPORT, RESET};
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
            configureTagInitValue(path.tag("Lookup", "Types"), DataType.DataSet, MeterType.buildDS());
            configureTagInitValue(path.tag("Lookup", "Units"), DataType.DataSet, MeterUnit.buildDS());
            configureTagInitValue(path.tag("Lookup", "RateTypes"), DataType.DataSet, RateType.buildDS());

            configureTag(path.tag("Type"), DataType.String);
            configureTag(path.tag("Units"), DataType.String);
            configureTag(path.tag("Value"), DataType.Float8);
        }

        @Override
        public void queueInitialMessages(ZWavePath path, int version, boolean secure, boolean initial) {
            sendCommand(path, new MeterCommandClass.Get(), secure);
        }
    }

    public static class Get extends CommandAdapter {
        private MeterUnit meterUnit;
        private RateType rateType;

        @CommandClassVersion(CommandClassVersion.Version.V1)
        public Get() {
            super(GET);
        }

        @CommandClassVersion(CommandClassVersion.Version.V2)
        public Get(MeterUnit meterUnit) {
            super(GET);
            this.meterUnit = meterUnit;
        }

        @CommandClassVersion(CommandClassVersion.Version.V4)
        public Get(MeterUnit meterUnit, RateType rateType) {
            super(GET);
            this.meterUnit = meterUnit;
            this.rateType = rateType;
        }

        @Override
        protected void addCommandData(ByteArrayOutputStream result) {
            super.addCommandData(result);

            if (meterUnit != null) {
                result.write(((rateType != null ? rateType.value : 0) << 6) | (meterUnit.scale << 3));
            }
        }
    }

    public static class Report extends CommandAdapter {
        public final Meter meter;

        public Report(byte[] commandData) throws DecoderException {
            super(commandData);
            meter = new Meter(in);
        }

        @Override
        public void update(ZWavePath path, int version, boolean secure) {
            updateTag(path.tag("Type"), meter.meterType.name);
            updateTag(path.tag("Units"), meter.meterUnit.unit);
            updateTag(path.tag("Value"), meter.value);
        }

        @Override
        public String toString() {
            return String.format("{\"Meter.Report\":{\"meter\": %s}}", meter);
        }
    }

    public static class GetSupported extends CommandAdapter {
        @CommandClassVersion(CommandClassVersion.Version.V2)
        public GetSupported() {
            super(SUPPORTED_GET);
        }
    }

    public static class ReportSupported extends CommandAdapter {
        public final boolean meterResetSupported;
        public final MeterType meterType;
        public final List<MeterUnit> supportedMeterUnits;
        public final RateType rateType;

        public ReportSupported(byte[] commandData) throws DecoderException {
            super(commandData);
            int b = in.read();
            meterResetSupported = (b >> 7) > 0;
            meterType = MeterType.from(b & 0x1F);
            rateType = RateType.from((b >> 5) & 0x03);
            int scales = in.read();
            supportedMeterUnits = new ArrayList();
            for (MeterUnit u : MeterUnit.values()) {
                if ((scales & u.supportedBitMask) > 0) {
                    supportedMeterUnits.add(u);
                }
            }
        }

        @Override
        public String toString() {
            return String.format("{\"Meter.Supported.Report\":{\"meterType\": %s, \"meterUnits\": [%s], \"supportsReset\": %d}}", meterType.toString(), Hex.asString(supportedMeterUnits), meterResetSupported ? 1 : 0);
        }
    }

    public static class Reset extends CommandAdapter {
        @CommandClassVersion(CommandClassVersion.Version.V2)
        public Reset() {
            super(RESET);
        }
    }
}
