package org.imdc.zwavedriver.gateway;

import org.imdc.zwavedriver.zwave.Hex;
import org.imdc.zwavedriver.zwave.PortException;
import org.imdc.zwavedriver.zwave.ZWaveSerialPort;
import org.imdc.zwavedriver.zwave.messages.*;
import org.imdc.zwavedriver.zwave.messages.commandclasses.Security0CommandClass;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandAdapter;
import org.imdc.zwavedriver.zwave.messages.framework.Message;
import org.imdc.zwavedriver.zwave.messages.framework.MessageProcessor;
import org.imdc.zwavedriver.zwave.messages.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ZWaveMessages extends ZWaveWrapperFunctions implements MessageProcessor {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ZWaveSerialPort zwavePort;

    private PriorityBlockingQueue<Message> messageQueue = new PriorityBlockingQueue(200, new Messages.MessageComparator());

    private ScheduledExecutorService pollingThreadPool;
    private ScheduledExecutorService sendLoop;
    private ScheduledExecutorService messageUpdateExecPool;

    private Map<String, ZWaveMessagePollingTask> pollingTasks = new HashMap<>();

    public void startup(String port) throws PortException {
        if (pollingThreadPool != null || sendLoop != null || zwavePort != null) {
            shutdown();
        }

        pollingThreadPool = Executors.newScheduledThreadPool(2);
        sendLoop = Executors.newScheduledThreadPool(1);
        messageUpdateExecPool = Executors.newScheduledThreadPool(5);

        if (port != null && port.length() > 0) {
            logger.info("Starting Z-Wave port '" + port + "'");
            zwavePort = new ZWaveSerialPort(port);
            zwavePort.setReceiver(this);
            sendLoop.scheduleWithFixedDelay(zwavePort, 500, 500, TimeUnit.MILLISECONDS);

            logger.info("Queuing initial messages (MemoryGetId, GetVersion, GetInitData, ApplicationNodeInformation)");
            addMessageToQueue(new MemoryGetIdMessage.Request());
            addMessageToQueue(new GetVersionMessage.Request());
            addMessageToQueue(new ApplicationNodeInformationMessage.Request());
        }
    }

    public void shutdown() {
        if (pollingThreadPool != null) {
            pollingThreadPool.shutdown();
        }

        if(sendLoop != null){
            sendLoop.shutdown();
        }

        if (messageUpdateExecPool != null) {
            messageUpdateExecPool.shutdown();
        }

        if (zwavePort != null) {
            zwavePort.shutdown();
        }
    }

    @Override
    public synchronized Message getNextMessage() {
        Message msg = messageQueue.poll();
        updateQueue();
        if(msg != null) {
            logger.trace(String.format("Next Message {\"node\": %s, \"message\": %s}", Hex.asString(msg.getNodeId()), msg.toString()));
            if (msg != null) {
                updateCurrentMessage(msg);
            }
        }
        return msg;
    }

    @Override
    public boolean allowProcess() {
        return initComplete();
    }

    @Override
    public boolean allowProcessMessage(Messages message) {
        if (message == Messages.GET_VERSION || message == Messages.MEMORY_GET_ID) {
            return true;
        }
        return false;
    }

    @Override
    public void process(Message message){
        messageUpdateExecPool.execute(new ZWaveMessageUpdateTask(message));
    }

    public void addMessageToQueue(Message messageObj) {
        messageQueue.offer(messageObj);
        updateQueue();
    }

    private void updateQueue() {
        ZWavePath path = getPath();
        if (path.getHomeId() != 0) {
            updateTag(getPath().tag("QueueSize"), messageQueue.size());
        }
    }

    public void clearQueue(){
        messageQueue.clear();
        updateQueue();
    }

    private void updateCurrentMessage(Message message) {
        if (message != null && message.getMessageType().getMessage() != Messages.MEMORY_GET_ID) {
            updateTag(getPath().tag("CurrentMessage"), message.getMessageId());
        }
    }

    public void addCommandMessageToQueue(int nodeId, CommandAdapter command, boolean secure) {
        addCommandMessageToQueue(nodeId, command, secure, false);
    }

    public void addCommandMessageToQueue(int nodeId, CommandAdapter command, boolean secure, boolean priority) {
        addMessageToQueue(_createMessage(nodeId, command, secure, priority));
    }

    public Message _createMessage(int nodeId, CommandAdapter command, boolean secure) {
        return _createMessage(nodeId, command, secure, false);
    }

    public Message _createMessage(int nodeId, CommandAdapter command, boolean secure, boolean priority) {
        Message message = new SendDataMessage.Request(nodeId, secure ? new Security0CommandClass.GetNonce(nodeId, command) : command, SendDataMessage.TRANSMIT_OPTIONS_ALL);
        message.setPriority(priority);
        return message;
    }

    public void setupPolling(ZWavePath path, int pollRate) {
        setupPolling(path, null, pollRate);
    }

    public void setupPolling(Message message, int pollRate) {
        setupPolling(null, message, pollRate);
    }

    private void setupPolling(ZWavePath path, Message message, int pollRate) {
        String task = path == null ? message.getMessageId() : path.getFullPath();

        if (pollingTasks.containsKey(task)) {
            ZWaveMessagePollingTask t = pollingTasks.get(task);
            t.getFutureTask().cancel(true);
            pollingTasks.remove(task);
        }

        if (pollRate > 0) {
            ZWaveMessagePollingTask t = new ZWaveMessagePollingTask(path.copy(), message);
            ScheduledFuture<?> future = pollingThreadPool.scheduleWithFixedDelay(t, pollRate, pollRate, TimeUnit.MILLISECONDS);
            t.setFutureTask(future);
            pollingTasks.put(task, t);
        }
    }
}
