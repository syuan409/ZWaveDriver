package org.imdc.zwavedriver.zwave.messages.commandclasses.framework;

import org.imdc.zwavedriver.gateway.ZWavePath;
import org.imdc.zwavedriver.gateway.ZWaveWrapperFunctions;
import org.imdc.zwavedriver.zwave.messages.commandclasses.CommandClasses;
import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;

public abstract class CommandAdapter extends ZWaveWrapperFunctions implements Command {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private CommandCode commandCode;
    private Command encapsulatedCommand;
    protected ByteArrayInputStream in;
    protected int commandLength;

    protected CommandAdapter(CommandCode commandCode) {
        this.commandCode = commandCode;
    }

    protected CommandAdapter(byte[] commandData) throws DecoderException {
        init(commandData);
    }

    protected void init(byte[] commandData) throws DecoderException {
        commandCode = decodeCommandCode(commandData);
        commandLength = commandData.length - 2;
        in = new ByteArrayInputStream(commandData);
        in.read();
        in.read();
    }

    public byte[] getCommandData(byte[] commandData) throws DecoderException {
        return getCommandData(commandData, 0);
    }

    public byte[] getCommandData(byte[] commandData, int starting) throws DecoderException {
        return getCommandData(commandData, starting, 0);
    }

    public byte[] getCommandData(byte[] commandData, int starting, int ending) throws DecoderException {
        if (commandData == null || commandData.length < (2 + starting)) {
            throw new DecoderException("Invalid command buffer");
        }
        byte[] outCommandData = Arrays.copyOfRange(commandData, 2 + starting, commandData.length - ending);

        for (int i = 0; i < outCommandData.length; i++) {
            in.read();
        }

        return outCommandData;
    }

    @Override
    public byte[] encode() {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        addCommandData(result);
        return result.toByteArray();
    }

    protected void addCommandData(ByteArrayOutputStream result) {
        result.write(commandCode.getCommandClass().getValue());
        result.write(commandCode.getCommand());
    }

    @Override
    public CommandCode getCommandCode() {
        return commandCode;
    }

    @Override
    public CommandClasses getCommandClass() {
        return commandCode.getCommandClass();
    }

    public static CommandCode decodeCommandCode(byte[] commandData) throws DecoderException {
        if (commandData == null || commandData.length < 2) {
            throw new DecoderException("Invalid command buffer");
        }
        return new CommandCode(CommandClasses.from(commandData[0]), commandData[1]);
    }

    @Override
    public boolean process() {
        return true;
    }

    @Override
    public void setEncapsulatedCommand(Command encapsulatedCommand) {
        this.encapsulatedCommand = encapsulatedCommand;
    }

    @Override
    public Command getEncapsulatedCommand() {
        return encapsulatedCommand;
    }

    @Override
    public void sent(byte nodeId) {

    }

    @Override
    public void update(ZWavePath path, int version, boolean secure) {

    }

    @Override
    public void notificationUpdate(ZWavePath path){

    }

    protected Logger getLogger(){
        return logger;
    }
}
