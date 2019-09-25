package org.imdc.zwavedriver.zwave.messages.commandclasses;

import org.imdc.zwavedriver.gateway.ZWavePath;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.Command;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandAdapter;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandArgument;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandClass;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandClassVersion;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandCode;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandProcessorAdapter;
import org.imdc.zwavedriver.zwave.messages.commandclasses.objects.UserIDStatus;
import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import com.inductiveautomation.ignition.common.model.values.QualityCode;
import com.inductiveautomation.ignition.common.sqltags.model.types.DataType;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

public class UserCodeCommandClass implements CommandClass {

    private static final CommandCode SET = new CommandCode(CommandClasses.USER_CODE, 0x01);
    private static final CommandCode GET = new CommandCode(CommandClasses.USER_CODE, 0x02);
    private static final CommandCode REPORT = new CommandCode(CommandClasses.USER_CODE, 0x03);
    private static final CommandCode NUMBER_GET = new CommandCode(CommandClasses.USER_CODE, 0x04);
    private static final CommandCode NUMBER_REPORT = new CommandCode(CommandClasses.USER_CODE, 0x05);

    public static class Processor extends CommandProcessorAdapter {
        @Override
        public CommandCode[] getCommandCodes() {
            return new CommandCode[]{GET, SET, REPORT, NUMBER_GET, NUMBER_REPORT};
        }

        @Override
        public Command processCommandData(CommandCode commandCode, CommandArgument argument, byte[] commandData) throws DecoderException {
            Command object = null;
            if (Arrays.asList(GET, REPORT).contains(commandCode)) {
                object = new Report(commandData);
            } else if (Arrays.asList(NUMBER_GET, NUMBER_REPORT).contains(commandCode)) {
                object = new ReportNumber(commandData);
            }
            return object;
        }

        @Override
        public void configureTags(ZWavePath path, int version) {
            configureTagInitValue(path.tag("Lookup", "UserStatus"), DataType.DataSet, UserIDStatus.buildDS());
            configureTag(path.tag("NumberUsers"), DataType.Int1);
        }

        @Override
        public void queueInitialMessages(ZWavePath path, int version, boolean secure, boolean initial) {
            sendCommand(path, new UserCodeCommandClass.GetNumber(), secure);
        }

        @Override
        public QualityCode write(ZWavePath path, int version, boolean secure, Object o) {
            try {
                int userId = Integer.parseInt(path.get(1));

                if (path.get(2).equals("Refresh") && ((Boolean) o)) {
                    updateTag(path, false);
                    sendCommand(path, new UserCodeCommandClass.Get(userId), secure);
                } else if (path.get(2).equals("Set") && ((Boolean) o)) {
                    updateTag(path, false);
                    UserIDStatus status = UserIDStatus.from((String) readTag(path.tag("Users", String.format("%d", userId), "Status")));
                    String code = (String) readTag(path.tag("Users", String.format("%d", userId), "Code"));
                    sendCommand(path, new UserCodeCommandClass.Set(userId, status, code), secure);
                }
            } catch (Exception ex) {
                logger.error("Error writing to user code tag", ex);
                return QualityCode.Error;
            }
            return QualityCode.Good;
        }
    }

    public static class Get extends CommandAdapter {
        private final byte userId;

        @CommandClassVersion(CommandClassVersion.Version.V1)
        public Get(int userId) {
            super(GET);
            this.userId = (byte) userId;
        }

        @Override
        protected void addCommandData(ByteArrayOutputStream result) {
            super.addCommandData(result);
            result.write(userId);
        }
    }

    public static class Set extends CommandAdapter {
        private final byte userId;
        private final UserIDStatus status;
        private String userCode;

        @CommandClassVersion(CommandClassVersion.Version.V1)
        public Set(int userId, UserIDStatus status) {
            this(userId, status, null);
        }

        @CommandClassVersion(CommandClassVersion.Version.V1)
        public Set(int userId, UserIDStatus status, String userCode) {
            super(SET);
            this.userId = (byte) userId;
            this.status = status;
            this.userCode = userCode;
        }

        @Override
        protected void addCommandData(ByteArrayOutputStream result) {
            super.addCommandData(result);
            result.write(userId);
            result.write(status.value);
            if (status == UserIDStatus.AVAILABLE || userCode == null) {
                userCode = new String(new byte[]{0x00, 0x00, 0x00, 0x00});
            }
            result.write(userCode.getBytes(), 0, userCode.getBytes().length);
        }
    }

    public static class Report extends CommandAdapter {
        public final byte userId;
        public final UserIDStatus status;
        public String userCode;

        public Report(byte[] commandData) throws DecoderException {
            super(commandData);
            userId = (byte) in.read();
            status = UserIDStatus.from(in.read());
            userCode = new String(getCommandData(commandData, 2));
        }

        @Override
        public void update(ZWavePath path, int version, boolean secure) {
            updateTag(path.tag("Users", String.format("%d", userId), "Status"), status.name);
            updateTag(path.tag("Users", String.format("%d", userId), "Code"), userCode);
        }

        @Override
        public void notificationUpdate(ZWavePath path) {
            configureTagInitValue(path.tag("UserId"), DataType.Int1, userId);
        }

        @Override
        public String toString() {
            return String.format("{\"UserCode.Report\":{\"userId\": %d, \"status\": %s, \"userCode\": %s}}", userId, status.toString(), userCode);
        }
    }

    public static class GetNumber extends CommandAdapter {
        @CommandClassVersion(CommandClassVersion.Version.V1)
        public GetNumber() {
            super(NUMBER_GET);
        }
    }

    public static class ReportNumber extends CommandAdapter {
        public final byte users;

        public ReportNumber(byte[] commandData) throws DecoderException {
            super(commandData);
            users = (byte) in.read();
        }

        @Override
        public void update(ZWavePath path, int version, boolean secure) {
            updateTag(path.tag("NumberUsers"), users);
            for (int i = 0; i < (int) users; i++) {
                configureTag(path.tag("Users", String.format("%d", (i + 1)), "Status"), DataType.String, true);
                configureTag(path.tag("Users", String.format("%d", (i + 1)), "Code"), DataType.String, true);
                configureTag(path.tag("Users", String.format("%d", (i + 1)), "Description"), DataType.String, true);
                configureTagInitValue(path.tag("Users", String.format("%d", (i + 1)), "Set"), DataType.Boolean, false, true);
                configureTagInitValue(path.tag("Users", String.format("%d", (i + 1)), "Refresh"), DataType.Boolean, false, true);
//                sendCommand(path, new UserCodeCommandClass.Get(i + 1), secure);
            }
        }

        @Override
        public String toString() {
            return String.format("{\"UserCode.Number.Report\":{\"users\": %d}}", users);
        }
    }
}
