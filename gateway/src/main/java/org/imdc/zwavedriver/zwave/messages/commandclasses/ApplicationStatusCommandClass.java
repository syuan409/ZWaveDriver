package org.imdc.zwavedriver.zwave.messages.commandclasses;

import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.Command;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandAdapter;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandArgument;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandClass;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandClassVersion;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandCode;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandProcessorAdapter;
import org.imdc.zwavedriver.zwave.messages.commandclasses.objects.ApplicationStatus;
import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;

import java.io.ByteArrayOutputStream;

public class ApplicationStatusCommandClass implements CommandClass {

    private static final CommandCode BUSY = new CommandCode(CommandClasses.APPLICATION_STATUS, 0x01);
    private static final CommandCode REJECTED = new CommandCode(CommandClasses.APPLICATION_STATUS, 0x02);

    public static class Processor extends CommandProcessorAdapter {
        @Override
        public CommandCode[] getCommandCodes() {
            return new CommandCode[]{BUSY, REJECTED};
        }

        @Override
        public Command processCommandData(CommandCode commandCode, CommandArgument argument, byte[] commandData) throws DecoderException {
            return new Reject(commandData);
        }
    }

    public static class Busy extends CommandAdapter {
        private final ApplicationStatus status;
        private final byte waitTime;

        @CommandClassVersion(CommandClassVersion.Version.V1)
        public Busy(ApplicationStatus status, int waitTime) {
            super(BUSY);
            this.status = status;
            this.waitTime = (byte) waitTime;
        }

        @Override
        protected void addCommandData(ByteArrayOutputStream result) {
            super.addCommandData(result);
            result.write(status.value);
            result.write(waitTime);
        }
    }

    public static class Reject extends CommandAdapter {
        public final byte status;

        public Reject(byte[] commandData) throws DecoderException {
            super(commandData);
            status = (byte) in.read();
        }

        @Override
        public String toString() {
            return String.format("{\"ApplicationStatus.Report\": {\"status\": %d", status);
        }
    }
}
