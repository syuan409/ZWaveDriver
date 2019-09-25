package org.imdc.zwavedriver.zwave;

import org.imdc.zwavedriver.zwave.messages.*;
import org.imdc.zwavedriver.zwave.messages.commandclasses.NoOpCommandClass;
import org.imdc.zwavedriver.zwave.messages.framework.*;
import org.imdc.zwavedriver.zwave.messages.*;
import org.imdc.zwavedriver.zwave.messages.framework.Message;
import org.imdc.zwavedriver.zwave.messages.framework.MessageProcessor;
import org.imdc.zwavedriver.zwave.messages.framework.MultiMessageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An extension of the ZWavePortRaw that adds a send queue and the ability to resend messages that was not properly
 * received by the ZWave controller.
 */
public class ZWaveSerialPortTest implements ZWavePort, Receiver {
    public static final int ACK_TIMEOUT_MS = 1500;
    public static final int SEND_TIMEOUT_MS = 5000;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private MultiMessageProcessor messageProcessor;
    private ZWaveRawSerialPort port;

    public ZWaveSerialPortTest(String portName) throws PortException {
        this(new ZWaveRawSerialPort(portName));
    }

    public ZWaveSerialPortTest(ZWaveRawSerialPort port) {
        this.port = port;
        this.port.setReceiver(this);
        this.messageProcessor = new MultiMessageProcessor();
    }

    @Override
    public void setReceiver(MessageProcessor receiver) {

    }

    @Override
    public void receiveMessage(byte[] messageBytes) {
        try {
            Message message = messageProcessor.process(messageBytes);
            logger.debug(String.format("Receiving {\"node\": %s, \"messageId\": %s, \"message\": %s}", Hex.asString(message.getNodeId()), message.getMessageId(), message.toString()));
        } catch(Exception ex){
            logger.error("Error decoding message", ex);
        }
    }

    @Override
    public void receiveFrameByte(byte frameByte) {
        logger.debug(String.format("Receiving frame byte {\"byte\": %s}", Hex.asString(frameByte)));
    }

    @Override
    public void sendMessage(Message message) {
        try {
            logger.debug(String.format("Sending {\"node\": %s, \"messageId\": %s, \"message\": %s}", Hex.asString(message.getNodeId()), message.getMessageId(), message.toString()));
            this.port.sendMessage(message.encode());
        } catch (PortException ex) {
            logger.error("Z-Wave port failed to send message", ex);
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

    public static void main(String[] args) {
        try {
            ZWaveSerialPortTest test = new ZWaveSerialPortTest("/dev/ttyACM0");

            test.sendMessage(new GetVersionMessage.Request());
            Thread.sleep(5000);

            test.sendMessage(new MemoryGetIdMessage.Request());
            Thread.sleep(5000);

            test.sendMessage(new GetInitDataMessage.Request());
            Thread.sleep(5000);

            test.sendMessage(new ApplicationNodeInformationMessage.Request());
            Thread.sleep(5000);

            test.sendMessage(new IdentifyNodeMessage.Request(1));
            Thread.sleep(5000);
            test.sendMessage(new GetRoutingInfoMessage.Request(1));
            Thread.sleep(5000);

            test.sendMessage(new IdentifyNodeMessage.Request(2));
            Thread.sleep(5000);
            test.sendMessage(new GetRoutingInfoMessage.Request(2));
            Thread.sleep(5000);
            test.sendMessage(new NoOperationMessage.Request(2, new NoOpCommandClass.Set()));
            Thread.sleep(5000);
            test.sendMessage(new RequestNodeInfoMessage.Request(2));
            Thread.sleep(5000);

            test.shutdown();
        } catch(Exception ex){
            ex.printStackTrace();
        }
    }
}
