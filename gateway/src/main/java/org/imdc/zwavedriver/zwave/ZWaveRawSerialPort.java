package org.imdc.zwavedriver.zwave;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;

/**
 * ZWave communication port for sending raw ZWave messages via a serial port to a ZWave controller.
 * The ZWawePort handles the packaging of the ZWave messages, with frame start byte, message length encoding
 * and checksum, so the input and output from the port is raw ZWave byte strings with messages.
 */
@SuppressWarnings("UnusedDeclaration")
public class ZWaveRawSerialPort implements SerialPortDataListener {
    public static final byte SOF = 0x01;
    public static final byte ACK = 0x06;
    public static final byte NAK = 0x15;
    public static final byte CAN = 0x18;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    String portName = "/dev/ttyAMA0";
    private Receiver receiver;

    protected SerialPort serialPort;
    protected volatile boolean isOpen = false;

    protected byte[] prevMessage;

    public ZWaveRawSerialPort(String portName) throws PortException {
        this(portName, new DummyProcessor());
    }

    public ZWaveRawSerialPort(String portName, Receiver receiver) throws PortException {
        this.receiver = receiver;
        this.portName = portName;
        serialPort = SerialPort.getCommPort(this.portName);
        open();
    }

    public void setReceiver(Receiver receiver) {
        this.receiver = receiver;
    }

    public static List<SerialPort> listAvailablePortNames() {
        return Arrays.asList(SerialPort.getCommPorts());
    }

    private void open() throws PortException {
        serialPort.setBaudRate(115200);
        serialPort.setNumDataBits(8);
        serialPort.setNumStopBits(SerialPort.ONE_STOP_BIT);
        serialPort.setParity(SerialPort.NO_PARITY);

        if (!serialPort.openPort()) {
            throw new PortException("Failed opening serial port '" + portName + "'");
        }

        serialPort.addDataListener(this);
        synchronizeCommunication();
    }

    public void close() {
        if (serialPort != null) {
            serialPort.closePort();
        }
    }

    public boolean isOpen() {
        return serialPort != null && serialPort.isOpen();
    }

    public void sendResponse(byte message) throws PortException {
        try {
            byte[] messageToWrite = new byte[]{ message };
            logger.debug("Sending message: " + Hex.asString(messageToWrite, " "));
            int ret = serialPort.writeBytes(messageToWrite, messageToWrite.length);
            if (ret == -1) {
                throw new PortException("Error writing '" + Hex.asString(message) + "'");
            }
        } catch (PortException ex) {
            throw ex;
        } catch(Exception ex){
            throw new PortException("Could not send message", ex);
        }
    }

    public void sendMessage(byte[] message) throws PortException {
        try {
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            result.write(SOF);
            byte messageLength = (byte) (message.length + 1);
            result.write(messageLength);
            result.write(message);
            result.write(calculateChecksum(message, messageLength));
            byte[] messageToWrite = result.toByteArray();
            logger.debug("Sending message: " + Hex.asString(messageToWrite, " "));
            int ret = serialPort.writeBytes(messageToWrite, messageToWrite.length);
            if (ret == -1) {
                throw new PortException("Error writing '" + Hex.asString(message) + "'");
            }
        } catch (PortException ex) {
            throw ex;
        } catch(Exception ex){
            throw new PortException("Could not send message", ex);
        }
    }

    public void synchronizeCommunication() throws PortException {
        sendResponse(NAK);
    }

    private static byte calculateChecksum(byte[] buffer, byte messageLength) {
        byte checkSum = -1;
        checkSum ^= messageLength;
        for (byte messageByte : buffer) {
            checkSum ^= messageByte;
        }
        return checkSum;
    }

    @Override
    public int getListeningEvents() {
        return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
    }

    @Override
    public void serialEvent(SerialPortEvent event) {
        try {
            byte[] newMessage = new byte[serialPort.bytesAvailable()];
            int numRead = serialPort.readBytes(newMessage, newMessage.length);

            if (numRead == -1) {
                logger.error("Error reading from serial port");
            } else {
                byte[] fullMessage;

                if (prevMessage != null && prevMessage.length > 0) {
                    fullMessage = ByteUtilities.combine(prevMessage, newMessage);
                } else {
                    fullMessage = newMessage;
                }

                logger.debug("Read message: " + Hex.asString(fullMessage, " "));

                boolean exitLoop = false;

                for (int i = 0; i < fullMessage.length; i++) {
                    byte frameByte = fullMessage[i];

                    switch (frameByte) {
                        case SOF:
                            try {
                                if ((i + 1) < fullMessage.length) {
                                    byte messageLength = fullMessage[i + 1];

                                    if ((i + 1 + messageLength) < fullMessage.length) {
                                        byte[] message = ByteUtilities.copy(fullMessage, i + 2, i + 1 + messageLength);
                                        byte checksum = fullMessage[i + 1 + messageLength];
                                        i = i + 1 + messageLength;
                                        sendResponse(ACK);
                                        receiver.receiveMessage(message);
                                    } else {
                                        prevMessage = ByteUtilities.copy(fullMessage, i, fullMessage.length);
                                        exitLoop = true;
                                    }
                                } else {
                                    prevMessage = new byte[]{frameByte};
                                    exitLoop = true;
                                }
                            } catch (Exception ex) {
                                logger.error("Error processing message", ex);
                            }
                            break;
                        case ACK:
                        case NAK:
                        case CAN:
                            receiver.receiveFrameByte(frameByte);
                            break;
                        default:
                            logger.warn(String.format("Z-Wave received unexpected frame byte %s, resynchronizing", Hex.asString(frameByte)));
                            synchronizeCommunication();
                            prevMessage = null;
                            exitLoop = true;
                            break;
                    }

                    if (exitLoop) {
                        break;
                    } else {
                        prevMessage = null;
                    }
                }
            }
        } catch(Exception ex){
            logger.error("Error receiving serial event", ex);
        }
    }

    private static class DummyProcessor implements Receiver {
        @Override
        public void receiveMessage(byte[] message) {
        }

        @Override
        public void receiveFrameByte(byte frameByte) {
        }
    }
}
