package org.imdc.zwavedriver.zwave;

import org.imdc.zwavedriver.zwave.messages.framework.Message;
import org.imdc.zwavedriver.zwave.messages.framework.MessageAdaptor;
import org.imdc.zwavedriver.zwave.messages.framework.MessageProcessor;
import org.imdc.zwavedriver.zwave.messages.framework.MessageType;
import org.imdc.zwavedriver.zwave.messages.framework.MultiMessageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * An extension of the ZWavePortRaw that adds a send queue and the ability to resend messages that was not properly
 * received by the ZWave controller.
 */
public class ZWaveSerialPort implements ZWavePort, Receiver, Runnable {
    public static final int ACK_TIMEOUT_MS = 1500;
    public static final int SEND_TIMEOUT_MS = 1500;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private MultiMessageProcessor messageProcessor;
    private MessageProcessor externalReceiver;
    private Message outstandingSentMessage;
    private ZWaveRawSerialPort port;
    private Date lastAck;

    public ZWaveSerialPort(String portName) throws PortException {
        this(new ZWaveRawSerialPort(portName));
    }

    public ZWaveSerialPort(ZWaveRawSerialPort port) {
        this.port = port;
        this.port.setReceiver(this);
        this.messageProcessor = new MultiMessageProcessor();
        this.lastAck = new Date();
    }

    @Override
    public void setReceiver(MessageProcessor receiver) {
        externalReceiver = receiver;
    }

    @Override
    public void receiveMessage(byte[] messageBytes) {
        try {
            if (externalReceiver != null && !externalReceiver.allowProcess()) {
                MessageType messageType = MessageAdaptor.decodeMessageId(messageBytes);
                if (!externalReceiver.allowProcessMessage(messageType.getMessage())) {
                    return;
                }
            }

            Message message = messageProcessor.process(messageBytes);
            Message osm = getOutstandingSentMessage();
            message.setNodeId(osm == null ? (byte) 0xFF : osm.getNodeId());

            logger.debug(String.format("Receiving {\"node\": %s, \"messageId\": %s, \"message\": %s}", Hex.asString(message.getNodeId()), message.getMessageId(), message.toString()));

            if(osm != null) {
                logger.debug(String.format("Outstanding {\"node\": %s, \"messageId\": %s, \"message\": %s}", Hex.asString(osm.getNodeId()), osm.getMessageId(), osm.toString()));
                if (message.getMessageId().equals(osm.getMessageId())) {
                    logger.debug(String.format("Message successful {\"node\": %s, \"messageId\": %s, \"message\": %s}", Hex.asString(message.getNodeId()), message.getMessageId(), message.toString()));
                    osm.success();
                }
            }

            if (externalReceiver != null) {
                externalReceiver.process(message);
            }
        } catch(Exception ex){
            logger.error("Error decoding message", ex);
        }
    }

    public synchronized Message getOutstandingSentMessage() {
        return outstandingSentMessage;
    }

    public synchronized void setOutstandingSentMessage(Message outstandingSentMessage) {
        this.outstandingSentMessage = outstandingSentMessage;
    }

    @Override
    public void receiveFrameByte(byte frameByte) {
        logger.debug(String.format("Receiving frame byte {\"byte\": %s}", Hex.asString(frameByte)));
        switch (frameByte) {
            case ZWaveRawSerialPort.ACK: {
                lastAck = new Date();
                break;
            }
            case ZWaveRawSerialPort.NAK:
            case ZWaveRawSerialPort.CAN: {
                resendCurrentMessage();
                break;
            }
        }
    }

    private void checkCurrentMessage() {
        try {
            Message osm = getOutstandingSentMessage();

            if (osm != null) {
                if (osm.getStatus() == Message.Status.UNKNOWN) {
                    if (osm.getSentDate() != null && (getNow().getTime() - osm.getSentDate().getTime()) > SEND_TIMEOUT_MS) {
                        osm.failed();
                    }
                }
            }

            processNextMessage(osm);
        } catch(Exception ex){
            logger.error("Error processing next message", ex);
        }
    }

    private void processNextMessage(Message osm) {
        boolean doSend = false;

        if(osm == null){
            doSend = true;
        } else if(osm.getSentDate().before(lastAck) || (getNow().getTime() - lastAck.getTime()) > ACK_TIMEOUT_MS){
            if(osm.getStatus() != Message.Status.UNKNOWN) {
                doSend = true;
            }
        }

        if(doSend){
            Message msg = externalReceiver.getNextMessage();
            setOutstandingSentMessage(msg);
            if (msg != null) {
                sendMessage(msg);
            }
        }
    }

    @Override
    public void sendMessage(Message message) {
        try {
            logger.debug(String.format("Sending {\"node\": %s, \"messageId\": %s, \"message\": %s}", Hex.asString(message.getNodeId()), message.getMessageId(), message.toString()));
            this.port.sendMessage(message.encode());
            message.sent();
        } catch (PortException ex) {
            logger.error("Z-Wave port failed to send message", ex);
        }
    }

    private void resendCurrentMessage() {
        Message osm = getOutstandingSentMessage();

        if (osm != null && osm.getStatus() == Message.Status.UNKNOWN) {
            sendMessage(osm);
        }
    }

    @Override
    public void shutdown() {
        if(this.port != null) {
            this.port.close();
        }
    }

    @Override
    public boolean isOpen() {
        return port.isOpen();
    }

    Date getNow() {
        return new Date();
    }

    @Override
    public void run() {
        checkCurrentMessage();
    }
}
