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
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.MultipleReportsCommandAdapter;
import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import com.inductiveautomation.ignition.common.model.values.QualityCode;
import com.inductiveautomation.ignition.common.sqltags.model.types.DataType;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AssociationCommandClass implements CommandClass {

    private static final CommandCode SET = new CommandCode(CommandClasses.ASSOCIATION, 0x01);
    private static final CommandCode GET = new CommandCode(CommandClasses.ASSOCIATION, 0x02);
    private static final CommandCode REPORT = new CommandCode(CommandClasses.ASSOCIATION, 0x03);
    private static final CommandCode REMOVE = new CommandCode(CommandClasses.ASSOCIATION, 0x04);
    private static final CommandCode GROUPINGS_GET = new CommandCode(CommandClasses.ASSOCIATION, 0x05);
    private static final CommandCode GROUPINGS_REPORT = new CommandCode(CommandClasses.ASSOCIATION, 0x06);
    private static final CommandCode GROUP_GET = new CommandCode(CommandClasses.ASSOCIATION, 0x0B);
    private static final CommandCode GROUP_REPORT = new CommandCode(CommandClasses.ASSOCIATION, 0x0C);

    public static class Processor extends CommandProcessorAdapter {
        @Override
        public CommandCode[] getCommandCodes() {
            return new CommandCode[]{GET, SET, REPORT, REMOVE, GROUPINGS_GET, GROUPINGS_REPORT, GROUP_GET, GROUP_REPORT};
        }

        @Override
        public Command processCommandData(CommandCode commandCode, CommandArgument argument, byte[] commandData) throws DecoderException {
            Command object = null;
            if (Arrays.asList(GET, REPORT).contains(commandCode)) {
                object = new Report(commandData);
            } else if (Arrays.asList(GROUP_GET, GROUP_REPORT).contains(commandCode)) {
                object = new ReportGroup(commandData);
            } else if (Arrays.asList(GROUPINGS_GET, GROUPINGS_REPORT).contains(commandCode)) {
                object = new ReportGroupings(commandData);
            }
            return object;
        }

        @Override
        public void queueInitialMessages(ZWavePath path, int version, boolean secure, boolean initial) {
            configureTagInitValue(path.tag("NumGroupings"), DataType.Int1, 0);
            sendCommand(path, new AssociationCommandClass.GetGroupings(), secure);
        }

        @Override
        public QualityCode write(ZWavePath path, int version, boolean secure, Object o) {
            try {
                int associationId = Integer.valueOf(path.get(1));
                if (path.get(2).equals("Refresh") && ((Boolean) o)) {
                    updateTag(path, false);
                    sendCommand(path, new AssociationCommandClass.Get(associationId), secure);
                } else if (path.get(2).equals("Remove") && ((Boolean) o)) {
                    updateTag(path, false);
                    byte[] nodes = Hex.hexToByteArray((String) readTag(path.tag("Associations", String.format("%d", associationId), "Associations")));
                    sendCommand(path, new AssociationCommandClass.Remove(associationId, nodes), secure);
                } else {
                    byte[] nodes = Hex.hexToByteArray((String) o);
                    sendCommand(path, new AssociationCommandClass.Set(associationId, nodes), secure);
                }
                return QualityCode.Good;
            } catch(Exception ex){
                logger.error("Error writing to association tag", ex);
                return QualityCode.Error;
            }
        }
    }

    public static class Get extends CommandAdapter {
        private final byte associationId;

        @CommandClassVersion(CommandClassVersion.Version.V1)
        public Get(int associationId) {
            super(GET);
            this.associationId = (byte) associationId;
        }

        @Override
        protected void addCommandData(ByteArrayOutputStream result) {
            super.addCommandData(result);
            result.write(associationId);
        }
    }

    public static class Set extends CommandAdapter {
        private final byte associationId;
        private final byte[] nodes;

        @CommandClassVersion(CommandClassVersion.Version.V1)
        public Set(int associationId, byte[] nodes) {
            this(SET, associationId, nodes);
        }

        @CommandClassVersion(CommandClassVersion.Version.V1)
        public Set(CommandCode commandCode, int associationId, byte[] nodes) {
            super(commandCode);

            this.associationId = (byte) associationId;
            this.nodes = nodes;
        }

        @Override
        protected void addCommandData(ByteArrayOutputStream result) {
            super.addCommandData(result);

            result.write(associationId);
            for (byte node : nodes) {
                result.write(node);
            }
        }
    }

    public static class Remove extends Set {
        @CommandClassVersion(CommandClassVersion.Version.V1)
        public Remove(int associationId, byte[] nodes) {
            super(REMOVE, associationId, nodes);
        }
    }

    public static class Report extends MultipleReportsCommandAdapter {
        public byte associationId;
        public byte maxAssociations;
        public List<Byte> nodes;

        public Report(byte[] commandData) throws DecoderException {
            super(commandData);
            nodes = new ArrayList();
            processNextReport(commandData);
        }

        @Override
        public void processNextReport(byte[] commandData) {
            associationId = (byte) in.read();
            maxAssociations = (byte) in.read();
            reportsToFollow = (byte) in.read();
            int numberOfNodes = commandLength - 3;

            for (int i = 0; i < numberOfNodes; i++) {
                nodes.add((byte) in.read());
            }
        }

        @Override
        public void update(ZWavePath path, int version, boolean secure) {
            updateTag(path.tag("Associations", String.format("%d", associationId), "MaxAssociations"), maxAssociations);
            updateTag(path.tag("Associations", String.format("%d", associationId), "Associations"), Hex.asString(nodes));
        }

        @Override
        public String toString() {
            return String.format("{\"Association.Report\":{\"associationId\": %d, \"maxAssociations\": %d, \"reportsToFollow\": %d, \"nodes\": [%s]}}", associationId, maxAssociations, reportsToFollow, Hex.asString(nodes));
        }
    }

    public static class GetGroupings extends CommandAdapter {
        @CommandClassVersion(CommandClassVersion.Version.V1)
        public GetGroupings() {
            super(GROUPINGS_GET);
        }
    }

    public static class ReportGroupings extends CommandAdapter {
        public final int supportedGroupings;

        public ReportGroupings(byte[] commandData) throws DecoderException {
            super(commandData);
            supportedGroupings = in.read();
        }

        @Override
        public void update(ZWavePath path, int version, boolean secure) {
            updateTag(path.tag("NumGroupings"), supportedGroupings);
            for(int i = 0; i < supportedGroupings; i++){
                configureTag(path.tag("Associations", String.format("%d", i+1), "MaxAssociations"), DataType.Int2);
                configureTag(path.tag("Associations", String.format("%d", i+1), "Associations"), DataType.String, true);
                configureTagInitValue(path.tag("Associations", String.format("%d", i+1), "Remove"), DataType.Boolean, false, true);
                configureTagInitValue(path.tag("Associations", String.format("%d", i+1), "Refresh"), DataType.Boolean, false, true);
                sendCommand(path, new AssociationCommandClass.Get(i+1), secure);
            }
        }

        @Override
        public String toString() {
            return String.format("{\"Association.Groupings.Report\":{\"supportedGroupings\": %d}}", supportedGroupings);
        }
    }

    public static class GetGroup extends CommandAdapter {
        @CommandClassVersion(CommandClassVersion.Version.V2)
        public GetGroup() {
            super(GROUP_GET);
        }
    }

    public static class ReportGroup extends CommandAdapter {
        public final byte group;

        public ReportGroup(byte[] commandData) throws DecoderException {
            super(commandData);
            group = (byte) in.read();
        }

        @Override
        public String toString() {
            return String.format("{\"Association.Group.Report\":{\"group\": %d}}", group);
        }
    }
}
