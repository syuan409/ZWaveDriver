package org.imdc.zwavedriver.zwave.messages.commandclasses.framework;

import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;

import java.util.Date;

public abstract class MultipleReportsCommandAdapter extends CommandAdapter {
    public static final int SEND_TIMEOUT_MS = 30000;

    protected int reportsToFollow = 0;
    protected Date lastReport = null;

    protected MultipleReportsCommandAdapter(CommandCode commandCode) {
        super(commandCode);
    }

    protected MultipleReportsCommandAdapter(byte[] commandData) throws DecoderException {
        super(commandData);
    }

    public boolean hasReportsToFollow() {
        return reportsToFollow > 0;
    }

    public void processedReport() {
        this.lastReport = new Date();
    }

    public boolean isReportTimeout() {
        if (lastReport != null) {
            return (new Date().getTime() - lastReport.getTime()) > SEND_TIMEOUT_MS;
        }

        return false;
    }

    public void nextReport(byte[] commandData) throws DecoderException {
        init(commandData);
        processNextReport(commandData);
    }

    public abstract void processNextReport(byte[] commandData) throws DecoderException;

    @Override
    public boolean process() {
        return !hasReportsToFollow();
    }
}
