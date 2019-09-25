package org.imdc.zwavedriver.zwave.messages.commandclasses.framework;

import org.imdc.zwavedriver.gateway.ZWavePath;
import org.imdc.zwavedriver.gateway.ZWaveWrapperFunctions;
import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import org.imdc.zwavedriver.zwave.messages.framework.MultiMessageProcessor;
import com.inductiveautomation.ignition.common.model.values.QualityCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public abstract class CommandProcessorAdapter extends ZWaveWrapperFunctions implements CommandProcessor {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private Map<Byte, Map<CommandCode, MultipleReportsCommandAdapter>> unfinishedCommands = new HashMap();
    private Map<Byte, Map<CommandCode, Object>> storedObjects = new HashMap();
    private MultiMessageProcessor messageProcessor;
    private MultiCommandProcessor commandProcessor;

    public CommandProcessorAdapter setMultiMessageProcessor(MultiMessageProcessor messageProcessor) {
        this.messageProcessor = messageProcessor;
        return this;
    }

    public CommandProcessorAdapter setMultiCommandProcessor(MultiCommandProcessor commandProcessor) {
        this.commandProcessor = commandProcessor;
        return this;
    }

    protected MultiMessageProcessor getMultiMessageProcessor() {
        return messageProcessor;
    }

    protected MultiCommandProcessor getMultiCommandProcessor() {
        return commandProcessor;
    }

    @Override
    public CommandCode[] getCommandCodes() {
        return new CommandCode[0];
    }

    @Override
    public Command process(CommandCode commandCode, CommandArgument argument, byte[] commandData) throws DecoderException {
        if (unfinishedCommands.containsKey(argument.getNode())) {
            if (unfinishedCommands.get(argument.getNode()).containsKey(commandCode)) {
                MultipleReportsCommandAdapter command = unfinishedCommands.get(argument.getNode()).get(commandCode);
                boolean removeCommand = false;
                boolean returnCommand = false;
                if (!command.isReportTimeout()) {
                    command.nextReport(commandData);
                    if (!command.hasReportsToFollow()) {
                        removeCommand = true;
                    }
                    returnCommand = true;
                } else {
                    removeCommand = true;
                }

                if (removeCommand) {
                    unfinishedCommands.remove(commandCode);
                }

                if(returnCommand) {
                    return command;
                }
            }
        }

        Command command = processCommandData(commandCode, argument, commandData);
        if (command != null && command instanceof MultipleReportsCommandAdapter) {
            if (((MultipleReportsCommandAdapter) command).hasReportsToFollow()) {
                ((MultipleReportsCommandAdapter) command).processedReport();

                if (!unfinishedCommands.containsKey(argument.getNode())) {
                    unfinishedCommands.put(argument.getNode(), new HashMap<CommandCode, MultipleReportsCommandAdapter>());
                }
                unfinishedCommands.get(argument.getNode()).put(commandCode, (MultipleReportsCommandAdapter) command);
            }
        }

        return command;
    }

    public abstract Command processCommandData(CommandCode commandCode, CommandArgument argument, byte[] commandData) throws DecoderException;

    public void storeCommand(byte node, CommandCode commandCode, CommandAdapter command) {
        storeObject(node, commandCode, command);
    }

    public void storeCommand(byte node, CommandAdapter command) {
        storeObject(node, command.getCommandCode(), command);
    }

    public void storeObject(byte node, CommandCode commandCode, Object obj) {
        if (!storedObjects.containsKey(node)) {
            storedObjects.put(node, new HashMap<CommandCode, Object>());
        }

        storedObjects.get(node).put(commandCode, obj);
    }

    public void clearStoredObject(byte node, CommandCode commandCode) {
        if (storedObjects.containsKey(node)) {
            if (storedObjects.get(node).containsKey(commandCode)) {
                storedObjects.remove(commandCode);
            }
        }
    }

    public CommandAdapter getStoredCommand(byte node, CommandCode commandCode) {
        Object obj = getStoredObject(node, commandCode);
        if (obj != null) {
            return (CommandAdapter) obj;
        }
        return null;
    }

    public Object getStoredObject(byte node, CommandCode commandCode) {
        if (storedObjects.containsKey(node)) {
            if (storedObjects.get(node).containsKey(commandCode)) {
                return storedObjects.get(node).get(commandCode);
            }
        }

        return null;
    }

    public void configureTags(ZWavePath path, int version) {

    }

    public void queueInitialMessages(ZWavePath path, int version, boolean secure, boolean initial) {

    }

    public void refresh(ZWavePath path) {
        int version = getVersion(path);
        boolean secure = isSecure(path);
        refresh(path, version, secure);
    }

    public void refresh(ZWavePath path, int version, boolean secure) {
        queueInitialMessages(path, version, secure, false);
    }

    public QualityCode write(ZWavePath path, int version, boolean secure, Object o) {
        return QualityCode.Good;
    }
}
