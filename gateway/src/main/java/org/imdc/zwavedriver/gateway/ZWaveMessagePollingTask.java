package org.imdc.zwavedriver.gateway;

import org.imdc.zwavedriver.zwave.messages.framework.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledFuture;

public class ZWaveMessagePollingTask implements Runnable {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private Message message;
    private ZWavePath path;
    private ScheduledFuture futureTask;

    public ZWaveMessagePollingTask(ZWavePath path) {
        this(path, null);
    }

    public ZWaveMessagePollingTask(Message message) {
        this(null, message);
    }

    public ZWaveMessagePollingTask(ZWavePath path, Message message) {
        this.path = path;
        this.message = message;
    }

    public void setFutureTask(ScheduledFuture futureTask) {
        this.futureTask = futureTask;
    }

    public ScheduledFuture getFutureTask() {
        return futureTask;
    }

    @Override
    public void run() {
        try {
            ZWaveExecutor executor = ZWaveExecutor.getInstance();
            if(message != null){
                executor.sendMessage(message);
            } else {
                path.getCommandClassObj().getProcessor().refresh(path);
            }
        } catch (Throwable t) {
            String error = "";
            if(message != null){
                error = "message '" + message.toString() + "'";
            } else {
                error = "tag '" + path.getFullPath() + "'";
            }
            logger.error("Error running polling task for " + error, t);
        }
    }
}
