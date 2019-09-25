package org.imdc.zwavedriver.gateway;

import org.imdc.zwavedriver.zwave.messages.framework.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZWaveMessageUpdateTask implements Runnable {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private Message message;

    public ZWaveMessageUpdateTask(Message message) {
        this.message = message;
    }

    @Override
    public void run() {
        try {
            if(message != null){
                message.update();
            }
        } catch (Throwable t) {
            String error = "";
            if(message != null){
                error = "message '" + message.toString() + "'";
            }
            logger.error("Error running message update task for " + error, t);
        }
    }
}
