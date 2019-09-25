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
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.MultiCommandProcessor;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.MultipleReportsCommandAdapter;
import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import com.inductiveautomation.ignition.common.sqltags.model.types.DataType;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MultiInstanceCommandClass implements CommandClass {

    private static final CommandCode ENDPOINT_GET = new CommandCode(CommandClasses.MULTI_INSTANCE, 0x07);
    private static final CommandCode ENDPOINT_REPORT = new CommandCode(CommandClasses.MULTI_INSTANCE, 0x08);
    private static final CommandCode CAPABILITY_GET = new CommandCode(CommandClasses.MULTI_INSTANCE, 0x09);
    private static final CommandCode CAPABILITY_REPORT = new CommandCode(CommandClasses.MULTI_INSTANCE, 0x0A);
    private static final CommandCode ENDPOINTS_GET = new CommandCode(CommandClasses.MULTI_INSTANCE, 0x0B);
    private static final CommandCode ENDPOINTS_REPORT = new CommandCode(CommandClasses.MULTI_INSTANCE, 0x0C);
    private static final CommandCode ENCAPSULATION = new CommandCode(CommandClasses.MULTI_INSTANCE, 0x0D);
    private static final CommandCode AGGREGATED_MEMBER_GET = new CommandCode(CommandClasses.MULTI_INSTANCE, 0x0E);
    private static final CommandCode AGGREGATED_MEMBER_REPORT = new CommandCode(CommandClasses.MULTI_INSTANCE, 0x0F);

    private static final int DYNAMIC_ENDPOINTS = 0x80;
    private static final int IDENTICAL_ENDPOINTS = 0x40;

    public static class Processor extends CommandProcessorAdapter {
        @Override
        public CommandCode[] getCommandCodes() {
            return new CommandCode[]{ENDPOINT_GET, ENDPOINT_REPORT, CAPABILITY_GET, CAPABILITY_REPORT, ENDPOINTS_GET, ENDPOINTS_REPORT, ENCAPSULATION, AGGREGATED_MEMBER_GET, AGGREGATED_MEMBER_REPORT};
        }

        @Override
        public Command processCommandData(CommandCode commandCode, CommandArgument argument, byte[] commandData) throws DecoderException {
            Command object = null;
            if (Arrays.asList(ENDPOINT_GET, ENDPOINT_REPORT).contains(commandCode)) {
                object = new Report(commandData);
            } else if (Arrays.asList(CAPABILITY_GET, CAPABILITY_REPORT).contains(commandCode)) {
                object = new ReportCapability(commandData);
            } else if (ENCAPSULATION.equals(commandCode)) {
                object = new Encapsulation(commandData, getMultiCommandProcessor());
            } else if (Arrays.asList(AGGREGATED_MEMBER_GET, AGGREGATED_MEMBER_REPORT).contains(commandCode)) {
                object = new ReportAggregatedMembers(commandData);
            }
            return object;
        }

        @Override
        public void configureTags(ZWavePath path, int version) {
            configureTag(path.tag("DynamicEndpoints"), DataType.Boolean);
            configureTag(path.tag("OnlyIdenticalEndpoints"), DataType.Boolean);
            configureTag(path.tag("NumberEndpoints"), DataType.Int2);
        }

        @Override
        public void queueInitialMessages(ZWavePath path, int version, boolean secure, boolean initial) {
            sendCommand(path, new MultiInstanceCommandClass.Get(), secure);
        }
    }

    public static class Get extends CommandAdapter {
        @CommandClassVersion(CommandClassVersion.Version.V3)
        public Get() {
            super(ENDPOINT_GET);
        }
    }

    public static class Report extends CommandAdapter {
        public final boolean hasDynamicEndpoints;
        public final boolean hasOnlyIdenticalEndpoints;
        public final int numberOfEndpoints;
        public Integer numberOfAggregatedEndpoints;

        public Report(byte[] commandData) throws DecoderException {
            super(commandData);
            int flags = in.read();
            hasDynamicEndpoints = (flags & DYNAMIC_ENDPOINTS) != 0;
            hasOnlyIdenticalEndpoints = (flags & IDENTICAL_ENDPOINTS) != 0;
            numberOfEndpoints = in.read() & 0x7F;

            if (commandLength >= 3) {
                // Version 4
                numberOfAggregatedEndpoints = in.read();
            }
        }

        @Override
        public void update(ZWavePath path, int version, boolean secure) {
            updateTag(path.tag("DynamicEndpoints"), hasDynamicEndpoints);
            updateTag(path.tag("OnlyIdenticalEndpoints"), hasOnlyIdenticalEndpoints);
            updateTag(path.tag("NumberEndpoints"), numberOfEndpoints);
        }

        @Override
        public String toString() {
            return String.format("{\"MultiInstance.Report\":{\"hasDynamic\": %b, \"identical\": %b, \"numberOfEndpoints\": %d}}", hasDynamicEndpoints, hasOnlyIdenticalEndpoints, numberOfEndpoints);
        }
    }

    public static class GetCapability extends CommandAdapter {
        private final int endpoint;

        @CommandClassVersion(CommandClassVersion.Version.V3)
        public GetCapability(int endpoint) {
            super(CAPABILITY_GET);
            this.endpoint = endpoint;
        }

        @Override
        protected void addCommandData(ByteArrayOutputStream result) {
            super.addCommandData(result);
            result.write(endpoint);
        }
    }

    public static class ReportCapability extends CommandAdapter {
        public final byte endpoint;
        public final boolean isDynamic;
        public final byte genericDeviceClass;
        public final byte specificDeviceClass;
        public final List<CommandClasses> commandClasses;

        public ReportCapability(byte[] commandData) throws DecoderException {
            super(commandData);
            int flags = in.read();
            isDynamic = (flags & DYNAMIC_ENDPOINTS) != 0;
            endpoint = (byte) (flags & 0x7F);
            genericDeviceClass = (byte) in.read();
            specificDeviceClass = (byte) in.read();
            byte[] ccs = getCommandData(commandData, 3);
            commandClasses = new ArrayList();
            for (byte cc : ccs) {
                commandClasses.add(CommandClasses.from(cc));
            }
        }

        @Override
        public String toString() {
            return String.format("{\"MultiInstance.Capability.Report\":{\"endpoint\": %d, \"isDynamic\": %b, \"genericDeviceClass\": %d, \"specificDeviceClass\": %d, \"commandClasses\": [%s]}}", endpoint, isDynamic, genericDeviceClass, specificDeviceClass, Hex.asString(commandClasses));
        }
    }

    public static class GetEndpoints extends CommandAdapter {
        private final byte genericDeviceClass;
        private final byte specificDeviceClass;

        @CommandClassVersion(CommandClassVersion.Version.V3)
        public GetEndpoints(int genericDeviceClass, int specificDeviceClass) {
            super(ENDPOINTS_GET);
            this.genericDeviceClass = (byte) genericDeviceClass;
            this.specificDeviceClass = (byte) specificDeviceClass;
        }

        @Override
        protected void addCommandData(ByteArrayOutputStream result) {
            super.addCommandData(result);
            result.write(genericDeviceClass);
            result.write(specificDeviceClass);
        }
    }

    public static class ReportEndpoints extends MultipleReportsCommandAdapter {
        public byte genericDeviceClass;
        public byte specificDeviceClass;
        public List<Byte> endpoints;

        public ReportEndpoints(byte[] commandData) throws DecoderException {
            super(commandData);
            endpoints = new ArrayList();
            processNextReport(commandData);
        }

        @Override
        public void processNextReport(byte[] commandData) {
            reportsToFollow = in.read();
            genericDeviceClass = (byte) in.read();
            specificDeviceClass = (byte) in.read();
            int numberOfEndpoints = commandLength - 3;

            for (int i = 0; i < numberOfEndpoints; i++) {
                endpoints.add((byte) in.read());
            }
        }

        @Override
        public String toString() {
            return String.format("{\"MultiInstance.Endpoints.Report\":{\"genericDeviceClass\": %d, \"specificDeviceClass\": %d, \"reportsToFollow\": %d, \"endpoints\": [%s]}}", genericDeviceClass, specificDeviceClass, reportsToFollow, Hex.asString(endpoints));
        }
    }

    public static class Encapsulation extends CommandAdapter {
        public final byte sourceEndpoint, destinationEndpoint;
        public final boolean destinationBitMask;
        public final Command command;

        @CommandClassVersion(CommandClassVersion.Version.V3)
        public Encapsulation(int sourceEndpoint, int destinationEndpoint, Command command) {
            super(ENCAPSULATION);
            this.sourceEndpoint = (byte) sourceEndpoint;
            this.destinationEndpoint = (byte) destinationEndpoint;
            this.destinationBitMask = false;
            this.command = command;
            setEncapsulatedCommand(this.command);
        }

        public Encapsulation(byte[] commandData, MultiCommandProcessor processor) throws DecoderException {
            super(commandData);
            sourceEndpoint = (byte) in.read();
            byte dest = (byte) in.read();
            destinationEndpoint = (byte) (dest & 0x7F);
            destinationBitMask = (dest & 0x80) != 0;

            byte[] inCommandData = getCommandData(commandData, 2);
            command = processor.process(new CommandArgument(sourceEndpoint, destinationEndpoint), inCommandData);
            setEncapsulatedCommand(command);
        }

        @Override
        protected void addCommandData(ByteArrayOutputStream result) {
            super.addCommandData(result);
            result.write(sourceEndpoint);
            result.write(destinationEndpoint | (destinationBitMask ? 0x80 : 0x00));
            byte[] commandData = command.encode();
            result.write(commandData, 0, commandData.length);
        }

        @Override
        public CommandClasses getCommandClass() {
            if (command != null) {
                return command.getCommandClass();
            }

            return super.getCommandClass();
        }

        @Override
        public void sent(byte nodeId) {
            if (command != null) {
                command.sent(nodeId);
            }
        }

        @Override
        public String toString() {
            return String.format("{\"MultiInstance.Encapsulation\": {\"source\": \"%d\", \"destination\": \"%d\", \"command\": %s}}", sourceEndpoint, destinationEndpoint, command.toString());
        }
    }

    public static class GetAggregatedMembers extends CommandAdapter {
        private final byte endpoint;

        @CommandClassVersion(CommandClassVersion.Version.V4)
        public GetAggregatedMembers(int endpoint) {
            super(ENDPOINTS_GET);
            this.endpoint = (byte) (endpoint & 0x7F);
        }

        @Override
        protected void addCommandData(ByteArrayOutputStream result) {
            super.addCommandData(result);
            result.write(endpoint);
        }
    }

    public static class ReportAggregatedMembers extends CommandAdapter {
        public final byte endpoint;
        public final List<Byte> members;

        public ReportAggregatedMembers(byte[] commandData) throws DecoderException {
            super(commandData);
            endpoint = (byte) (in.read() & 0x7F);
            int numBitMasks = in.read();
            members = new ArrayList();
            for (int i = 0; i < numBitMasks; i++) {
                int next = in.read();
                for (int b = 0; b < 8; b++) {
                    if (((next >> b) & 0x01) != 0) {
                        members.add((byte) ((i * 8) + b + 1));
                    }
                }
            }
        }

        @Override
        public String toString() {
            return String.format("{\"MultiInstance.AggregatedMembers.Report\":{\"endpoint\": %d, \"members\": [%s]}}", endpoint, Hex.asString(members));
        }
    }
}
