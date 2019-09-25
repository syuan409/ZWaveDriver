package org.imdc.zwavedriver.zwave.messages.commandclasses;

import org.imdc.zwavedriver.gateway.ZWavePath;
import org.imdc.zwavedriver.zwave.ByteUtilities;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.Command;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandAdapter;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandArgument;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandClass;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandClassVersion;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandCode;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandProcessorAdapter;
import org.imdc.zwavedriver.zwave.messages.commandclasses.objects.LibraryType;
import org.imdc.zwavedriver.zwave.messages.commandclasses.objects.Size;
import org.imdc.zwavedriver.zwave.messages.commandclasses.objects.Version;
import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import com.inductiveautomation.ignition.common.sqltags.model.types.DataType;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VersionCommandClass implements CommandClass {

    private static final CommandCode GET = new CommandCode(CommandClasses.VERSION, 0x11);
    private static final CommandCode REPORT = new CommandCode(CommandClasses.VERSION, 0x12);
    private static final CommandCode COMMAND_CLASS_GET = new CommandCode(CommandClasses.VERSION, 0x13);
    private static final CommandCode COMMAND_CLASS_REPORT = new CommandCode(CommandClasses.VERSION, 0x14);
    private static final CommandCode CAPABILITIES_GET = new CommandCode(CommandClasses.VERSION, 0x15);
    private static final CommandCode CAPABILITIES_REPORT = new CommandCode(CommandClasses.VERSION, 0x16);
    private static final CommandCode SOFTWARE_GET = new CommandCode(CommandClasses.VERSION, 0x17);
    private static final CommandCode SOFTWARE_REPORT = new CommandCode(CommandClasses.VERSION, 0x18);

    public static class Processor extends CommandProcessorAdapter {

        @Override
        public CommandCode[] getCommandCodes() {
            return new CommandCode[]{GET, REPORT, COMMAND_CLASS_GET, COMMAND_CLASS_REPORT, CAPABILITIES_GET, CAPABILITIES_REPORT, SOFTWARE_GET, SOFTWARE_REPORT};
        }

        @Override
        public Command processCommandData(CommandCode commandCode, CommandArgument argument, byte[] commandData) throws DecoderException {
            Command object = null;
            if (Arrays.asList(GET, REPORT).contains(commandCode)) {
                object = new Report(commandData);
            } else if (Arrays.asList(COMMAND_CLASS_GET, COMMAND_CLASS_REPORT).contains(commandCode)) {
                object = new ReportCommandClass(commandData);
            } else if (Arrays.asList(CAPABILITIES_GET, CAPABILITIES_REPORT).contains(commandCode)) {
                object = new ReportCapabilities(commandData);
            } else if (Arrays.asList(SOFTWARE_GET, SOFTWARE_REPORT).contains(commandCode)) {
                object = new ReportSoftware(commandData);
            }
            return object;
        }

        @Override
        public void queueInitialMessages(ZWavePath path, int version, boolean secure, boolean initial) {
            sendCommand(path, new Get(), secure);
        }

        @Override
        public void configureTags(ZWavePath path, int version) {
            configureTag(path.tag("LibraryType"), DataType.String);
            configureTag(path.tag("Protocol"), DataType.String);
            configureTag(path.tag("Application"), DataType.String);
        }
    }

    public static class Get extends CommandAdapter {
        @CommandClassVersion(CommandClassVersion.Version.V1)
        public Get() {
            super(GET);
        }
    }

    public static class Report extends CommandAdapter {
        public final LibraryType type;
        public final Version protocol;
        public final Version application;
        public Byte hardwareVersion;
        public Byte numberFirmwareTargets;
        public List<Version> firmwareVersions;

        public Report(byte[] commandData) throws DecoderException {
            super(commandData);
            type = LibraryType.from(in.read());
            protocol = new Version(in);
            application = new Version(in);

            if (commandLength >= 7) {
                hardwareVersion = (byte) in.read();
                numberFirmwareTargets = (byte) in.read();
                firmwareVersions = new ArrayList();
                for (int i = 0; i < numberFirmwareTargets; i++) {
                    firmwareVersions.add(new Version(in));
                }
            }
        }

        @Override
        public void update(ZWavePath path, int version, boolean secure) {
            updateTag(path.tag("LibraryType"), type.name);
            updateTag(path.tag("Protocol"), protocol.getVersion());
            updateTag(path.tag("Application"), application.getVersion());
        }

        @Override
        public String toString() {
            return String.format("{\"Version.Report\":{\"type\": %s, \"protocolVersion\": %s, \"applicationVersion\": %s}}", type.toString(), protocol, application);
        }
    }

    public static class GetCommandClass extends CommandAdapter {
        private final CommandClasses commandClass;

        @CommandClassVersion(CommandClassVersion.Version.V1)
        public GetCommandClass(CommandClasses commandClass) {
            super(COMMAND_CLASS_GET);
            this.commandClass = commandClass;
        }

        @Override
        protected void addCommandData(ByteArrayOutputStream result) {
            super.addCommandData(result);
            result.write(commandClass.getValue());
        }
    }

    public static class ReportCommandClass extends CommandAdapter {
        public final CommandClasses commandClass;
        public final int version;

        public ReportCommandClass(byte[] commandData) throws DecoderException {
            super(commandData);
            commandClass = CommandClasses.from(in.read());
            version = in.read();
        }

        @Override
        public CommandClasses getCommandClass() {
            return commandClass;
        }

        @Override
        public void update(ZWavePath path, int version, boolean secure) {
            if (this.version > 1) {
                try {
                    commandClass.getProcessor().configureTags(path, this.version);
                } catch (Exception ignored) {
                }

                updateVersion(path, this.version);
                updateTag(path.tag("Version"), this.version);

                if (commandClass.getProcessor() != null) {
                    commandClass.getProcessor().queueInitialMessages(path, this.version, secure, true);
                }
            }
        }

        @Override
        public String toString() {
            return String.format("{\"Version.CommandClass.Report\":{\"commandClass\": %s, \"version\": %d}}", commandClass.toString(), version);
        }
    }

    public static class GetCapabilities extends CommandAdapter {
        @CommandClassVersion(CommandClassVersion.Version.V3)
        public GetCapabilities() {
            super(CAPABILITIES_GET);
        }
    }

    public static class ReportCapabilities extends CommandAdapter {
        public final boolean version, commandClass, software;

        public ReportCapabilities(byte[] commandData) throws DecoderException {
            super(commandData);
            int b = in.read();
            version = (b & 0x01) != 0;
            commandClass = (b & 0x02) != 0;
            software = (b & 0x04) != 0;
        }

        @Override
        public String toString() {
            return String.format("{\"Version.Capabilities.Report\":{\"version\": %b, \"commandClass\": %b, \"software\": %b}}", version, commandClass, software);
        }
    }

    public static class GetSoftware extends CommandAdapter {
        @CommandClassVersion(CommandClassVersion.Version.V3)
        public GetSoftware() {
            super(SOFTWARE_GET);
        }
    }

    public static class ReportSoftware extends CommandAdapter {
        public final int sdk, applicationFramework, applicationFrameworkBuild, hostInterface, hostInterfaceBuild, protocol, protocolBuild, application, applicationBuild;

        public ReportSoftware(byte[] commandData) throws DecoderException {
            super(commandData);
            sdk = ByteUtilities.readIntMSB(in, Size.BIT24);
            applicationFramework = ByteUtilities.readIntMSB(in, Size.BIT24);
            applicationFrameworkBuild = ByteUtilities.readInt16MSB(in);
            hostInterface = ByteUtilities.readIntMSB(in, Size.BIT24);
            hostInterfaceBuild = ByteUtilities.readInt16MSB(in);
            protocol = ByteUtilities.readIntMSB(in, Size.BIT24);
            protocolBuild = ByteUtilities.readInt16MSB(in);
            application = ByteUtilities.readIntMSB(in, Size.BIT24);
            applicationBuild = ByteUtilities.readInt16MSB(in);
        }

        @Override
        public String toString() {
            return String.format("{\"Version.Software.Report\":{\"sdk\": %d, \"applicationFramework\": %d.%d, \"hostInterface\": %d.%d, \"protocol\": %d.%d, \"application\": %d.%d}}", sdk, applicationFramework, applicationFrameworkBuild, hostInterface, hostInterfaceBuild, protocol, protocolBuild, application, applicationBuild);
        }
    }
}
