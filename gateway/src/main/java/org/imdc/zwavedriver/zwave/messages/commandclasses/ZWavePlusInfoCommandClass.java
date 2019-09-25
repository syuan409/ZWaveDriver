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
import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import com.inductiveautomation.ignition.common.sqltags.model.types.DataType;

public class ZWavePlusInfoCommandClass implements CommandClass {

    private static final CommandCode GET = new CommandCode(CommandClasses.ZWAVE_PLUS_INFO, 0x01);
    private static final CommandCode REPORT = new CommandCode(CommandClasses.ZWAVE_PLUS_INFO, 0x02);

    public static class Processor extends CommandProcessorAdapter {
        @Override
        public CommandCode[] getCommandCodes() {
            return new CommandCode[]{GET, REPORT};
        }

        @Override
        public Command processCommandData(CommandCode commandCode, CommandArgument argument, byte[] commandData) throws DecoderException {
            return new Report(commandData);
        }

        @Override
        public void configureTags(ZWavePath path, int version) {
            configureTag(path.tag("Version"), DataType.Int1);
            configureTag(path.tag("RoleType"), DataType.Int1);
            configureTag(path.tag("NodeType"), DataType.Int1);
            configureTag(path.tag("InstallerIcon"), DataType.Int2);
            configureTag(path.tag("UserIcon"), DataType.Int2);
        }

        @Override
        public void queueInitialMessages(ZWavePath path, int version, boolean secure, boolean initial) {
            sendCommand(path, new ZWavePlusInfoCommandClass.Get(), secure);
        }
    }

    public static class Get extends CommandAdapter {
        @CommandClassVersion(CommandClassVersion.Version.V1)
        public Get() {
            super(GET);
        }
    }

    public static class Report extends CommandAdapter {
        public final byte version;
        public final byte roleType;
        public final byte nodeType;
        public final int installerIcon;
        public final int userIcon;

        public Report(byte[] commandData) throws DecoderException {
            super(commandData);
            version = (byte) in.read();
            roleType = (byte) in.read();
            nodeType = (byte) in.read();
            installerIcon = ByteUtilities.readInt16MSB(in);
            userIcon = ByteUtilities.readInt16MSB(in);
        }

        @Override
        public void update(ZWavePath path, int version, boolean secure) {
            updateTag(path.tag("Version"), version);
            updateTag(path.tag("RoleType"), roleType);
            updateTag(path.tag("NodeType"), nodeType);
            updateTag(path.tag("InstallerIcon"), installerIcon);
            updateTag(path.tag("UserIcon"), userIcon);
        }

        @Override
        public String toString() {
            return String.format("{\"ZWavePlusInfo.Report\":{\"version\": %d, \"roleType\": %d, \"nodeType\": %d}}", version, roleType, nodeType);
        }
    }
}
