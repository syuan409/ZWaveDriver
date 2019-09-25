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
import org.imdc.zwavedriver.zwave.messages.commandclasses.objects.NotificationType;
import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import com.inductiveautomation.ignition.common.model.values.QualityCode;
import com.inductiveautomation.ignition.common.sqltags.model.types.DataType;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class NotificationCommandClass implements CommandClass {

    private static final CommandCode EVENT_SUPPORTED_GET = new CommandCode(CommandClasses.NOTIFICATION, 0x01);
    private static final CommandCode EVENT_SUPPORTED_REPORT = new CommandCode(CommandClasses.NOTIFICATION, 0x02);
    private static final CommandCode GET = new CommandCode(CommandClasses.NOTIFICATION, 0x04);
    private static final CommandCode REPORT = new CommandCode(CommandClasses.NOTIFICATION, 0x05);
    private static final CommandCode SET = new CommandCode(CommandClasses.NOTIFICATION, 0x06);
    private static final CommandCode SUPPORTED_GET = new CommandCode(CommandClasses.NOTIFICATION, 0x07);
    private static final CommandCode SUPPORTED_REPORT = new CommandCode(CommandClasses.NOTIFICATION, 0x08);

    public static class Processor extends CommandProcessorAdapter {
        @Override
        public CommandCode[] getCommandCodes() {
            return new CommandCode[]{GET, SET, REPORT, EVENT_SUPPORTED_GET, EVENT_SUPPORTED_REPORT, SUPPORTED_GET, SUPPORTED_REPORT};
        }

        @Override
        public Command processCommandData(CommandCode commandCode, CommandArgument argument, byte[] commandData) throws DecoderException {
            Command object = null;
            if (Arrays.asList(GET, REPORT).contains(commandCode)) {
                object = new Report(commandData, argument, getMultiCommandProcessor());
            } else if (Arrays.asList(SUPPORTED_GET, SUPPORTED_REPORT).contains(commandCode)) {
                object = new ReportSupported(commandData);
            } else if (Arrays.asList(EVENT_SUPPORTED_GET, EVENT_SUPPORTED_REPORT).contains(commandCode)) {
                object = new ReportEventSupported(commandData);
            }
            return object;
        }

        @Override
        public void configureTags(ZWavePath path, int version) {
            configureTagInitValue(path.tag("Lookup", "Types"), DataType.DataSet, NotificationType.buildDS());
        }

        @Override
        public void queueInitialMessages(ZWavePath path, int version, boolean secure, boolean initial) {
            sendCommand(path, new NotificationCommandClass.GetSupported(), secure);
        }

        @Override
        public QualityCode write(ZWavePath path, int version, boolean secure, Object o) {
            try {
                if (path.get(2).equals("Enabled")) {
                    NotificationType type = NotificationType.from(path.get(1));
                    sendCommand(path, new NotificationCommandClass.Set(type, (Boolean) o), secure);
                }
            } catch (Exception ex) {
                logger.error("Error writing to notification tag", ex);
                return QualityCode.Error;
            }
            return QualityCode.Good;
        }
    }

    public static class Get extends CommandAdapter {
        private boolean v1Alarm;
        private NotificationType type;
        private byte event;

        @CommandClassVersion(CommandClassVersion.Version.V8)
        public Get(boolean v1Alarm, NotificationType type, int event) {
            super(GET);
            this.v1Alarm = v1Alarm;
            this.type = type;
            this.event = (byte) event;
        }

        @Override
        protected void addCommandData(ByteArrayOutputStream result) {
            super.addCommandData(result);
            result.write(v1Alarm ? 0xFF : 0x00);
            result.write(type.value);
            result.write(event);
        }

        @Override
        public void sent(byte nodeId) {
            Object obj = CommandClasses.NOTIFICATION.getProcessor().getStoredObject(nodeId, GET);
            if (obj == null) {
                obj = new ReportEvents();
                CommandClasses.NOTIFICATION.getProcessor().storeObject(nodeId, GET, obj);
            }
            ((ReportEvents) obj).addEvent(event);
        }
    }

    public static class Set extends CommandAdapter {
        private NotificationType type;
        private final byte status;

        @CommandClassVersion(CommandClassVersion.Version.V8)
        public Set(NotificationType type, boolean enabled) {
            super(SET);
            this.type = type;
            this.status = (byte) (enabled ? 0xFF : 0);
        }

        @Override
        protected void addCommandData(ByteArrayOutputStream result) {
            super.addCommandData(result);
            result.write(type.value);
            result.write(status);
        }
    }

    public static class Report extends CommandAdapter {
        public byte v1AlarmType, v1AlarmLevel;
        public boolean status, validNotification;
        public NotificationType type;
        public byte event;
        public byte[] eventParams;
        public byte sequence;
        public Command command;

        public Report(byte[] commandData, CommandArgument argument, MultiCommandProcessor processor) throws DecoderException {
            super(commandData);
            v1AlarmType = (byte) in.read();
            v1AlarmLevel = (byte) in.read();
            in.read(); // Reserved

            byte b = (byte) in.read();
            status = b != 0;

            boolean hasSequence = (b & 0x80) != 0;
            type = NotificationType.from(in.read());
            event = (byte) in.read();

            Object reportEvent = CommandClasses.NOTIFICATION.getProcessor().getStoredObject(argument.getNode(), GET);
            if (reportEvent != null && ((ReportEvents) reportEvent).eventCheck(event)) {
                validNotification = false;
            } else {
                validNotification = true;
            }

            int eventLength = in.read() & 0x1F;

            if (eventLength > 0) {
                try{
                    eventParams = getCommandData(commandData, 7, commandLength - 7 - eventLength);

                    if (eventParams.length > 2) {
                        command = processor.process(argument, eventParams);
                    }
                } catch(Exception ex){
                    logger.error("Error getting event params", ex);
                }
            }

            if (hasSequence) {
                sequence = (byte) in.read();
            }
        }

        @Override
        public void update(ZWavePath path, int version, boolean secure) {
            if (!validNotification) {
                updateTag(path.tag("Types", type.name, "Enabled"), status);
            } else {
                if (command != null) {
                    command.notificationUpdate(path.copy().tag("Types", type.name, "Events", Hex.asString(event)));
                } else {
                    configureTagInitValue(path.copy().tag("Types", type.name, "Events", Hex.asString(event), "EventParams"), DataType.String, Hex.asString(eventParams));
                }

                String refreshCommands = (String) readTag(path.tag("Types", type.name, "Events", Hex.asString(event), "RefreshCommands"));
                refreshCommands(path, refreshCommands);
                updateTag(path.tag("Types", type.name, "Events", Hex.asString(event), "LastNotification"), new Date());
            }
        }

        @Override
        public String toString() {
            return String.format("{\"Notification.Report\":{\"valid\": %b, \"status\": %s, \"type\": %s, \"event\": %s, \"eventParams\": [%s], \"command\": %s, \"sequence\": %d}}", validNotification, status ? "Enabled" : " Disabled", type.toString(), Hex.asString(event), Hex.asString(eventParams), command == null ? "" : command.toString(), sequence);
        }
    }

    public static class GetSupported extends CommandAdapter {
        @CommandClassVersion(CommandClassVersion.Version.V8)
        public GetSupported() {
            super(SUPPORTED_GET);
        }
    }

    public static class ReportSupported extends CommandAdapter {
        public boolean v1Alarm;
        public final List<NotificationType> supportedTypes;

        public ReportSupported(byte[] commandData) throws DecoderException {
            super(commandData);
            int b = in.read();
            v1Alarm = (b & 0x80) != 0;
            int length = b & 0x1F;
            supportedTypes = NotificationType.getTypes(Hex.getMaskInts(in, length));
        }

        @Override
        public void update(ZWavePath path, int version, boolean secure) {
            for (NotificationType type : supportedTypes) {
                configureTag(path.tag("Types", type.name, "Enabled"), DataType.Boolean, true);
                configureTagInitValue(path.tag("Types", type.name, "V1Alarm"), DataType.Boolean, v1Alarm);
                configureTag(path.tag("Types", type.name, "SupportedEvents"), DataType.String);
                sendCommand(path, new NotificationCommandClass.GetEventSupported(type), secure);
            }
        }

        @Override
        public String toString() {
            return String.format("{\"Notification.Supported.Report\":{\"v1Alarm\": %b, \"types\": [%s]}}", v1Alarm, Hex.asString(supportedTypes));
        }
    }

    public static class GetEventSupported extends CommandAdapter {
        private NotificationType type;

        @CommandClassVersion(CommandClassVersion.Version.V8)
        public GetEventSupported(NotificationType type) {
            super(EVENT_SUPPORTED_GET);
            this.type = type;
        }

        @Override
        protected void addCommandData(ByteArrayOutputStream result) {
            super.addCommandData(result);
            result.write(type.value);
        }
    }

    public static class ReportEventSupported extends CommandAdapter {
        public NotificationType type;
        public final List<Integer> supportedEvents;

        public ReportEventSupported(byte[] commandData) throws DecoderException {
            super(commandData);
            type = NotificationType.from(in.read());
            int length = in.read() & 0x1F;
            supportedEvents = Hex.getMaskInts(in, length);
        }

        @Override
        public void update(ZWavePath path, int version, boolean secure) {
            if(!supportedEvents.contains(new Integer(0))){
                supportedEvents.add(new Integer(0));
            }

            updateTag(path.tag("Types", type.name, "SupportedEvents"), Hex.asString(supportedEvents));

            boolean v1Alarm = (Boolean) readTag(path.tag("Types", type.name, "V1Alarm"));
            for (Integer event : supportedEvents) {
                configureTag(path.tag("Types", type.name, "Events", Hex.asString(event.byteValue()), "RefreshCommands"), DataType.String,true);
                configureTag(path.tag("Types", type.name, "Events", Hex.asString(event.byteValue()), "LastNotification"), DataType.DateTime);
                configureTag(path.tag("Types", type.name, "Events", Hex.asString(event.byteValue()), "Description"), DataType.String, true);

                sendCommand(path, new NotificationCommandClass.Get(v1Alarm, type, event), secure);
            }
        }

        @Override
        public String toString() {
            return String.format("{\"Notification.Event.Supported.Report\":{\"type\": %s, \"events\": [%s]}}", type.toString(), Hex.asString(supportedEvents));
        }
    }

    private static class ReportEvents {
        private List<Byte> events;

        public ReportEvents() {
            events = new ArrayList<>();
        }

        public void addEvent(byte event) {
            events.add(new Byte(event));
        }

        public boolean eventCheck(byte event) {
            Byte eventByte = new Byte(event);

            if (events.contains(eventByte)) {
                events.remove(eventByte);
                return true;
            }

            return false;
        }

        public List<Byte> getEvents() {
            return events;
        }
    }
}
