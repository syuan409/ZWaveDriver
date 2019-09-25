package org.imdc.zwavedriver.gateway;

import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandAdapter;
import org.imdc.zwavedriver.zwave.messages.framework.Message;
import com.inductiveautomation.ignition.common.sqltags.model.types.DataType;

public class ZWaveWrapperFunctions {

    public boolean initComplete() {
        return ZWaveExecutor.getInstance().initComplete();
    }

    public void setInitComplete(boolean initComplete) {
        ZWaveExecutor.getInstance().setInitComplete(initComplete);
    }

    // ZWavePath Functions
    public ZWavePath getPath() {
        return ZWaveExecutor.getInstance().getPath();
    }

    public ZWavePath getPath(int nodeId) {
        return ZWaveExecutor.getInstance().getPath(nodeId);
    }

    // Home Functions
    public byte getControllerNodeId() {
        return ZWaveExecutor.getInstance().getControllerNodeId();
    }

    public boolean addHome(int homeId, byte nodeId) {
        return ZWaveExecutor.getInstance().addHome(homeId, nodeId);
    }

    public void healNetwork() {
        ZWaveExecutor.getInstance().healNetwork();
    }

    public void healNetworkNode(byte nodeId) {
        ZWaveExecutor.getInstance().healNetworkNode(nodeId);
    }

    public void healNetworkNodeFix(byte nodeId) {
        ZWaveExecutor.getInstance().healNetworkNodeFix(nodeId);
    }

    // Security Functions
    public byte[] getNetworkKey() {
        return ZWaveExecutor.getInstance().getNetworkKey();
    }

    public byte[] getNetworkKeyA(boolean temp) {
        return ZWaveExecutor.getInstance().getNetworkKeyA(temp);
    }

    public byte[] getNetworkKeyE(boolean temp) {
        return ZWaveExecutor.getInstance().getNetworkKeyE(temp);
    }

    public boolean isSecure(ZWavePath path) {
        return ZWaveExecutor.getInstance().isSecure(path);
    }

    public boolean isSecureAdd() {
        return ZWaveExecutor.getInstance().isSecureAdd();
    }

    public void setSecureAdd(boolean secureAdd) {
        ZWaveExecutor.getInstance().setSecureAdd(secureAdd);
    }

    // Node Functions
    public void addNode(byte nodeId) {
        ZWaveExecutor.getInstance().addNode(nodeId);
    }

    public void initNode(byte nodeId) {
        ZWaveExecutor.getInstance().initNode(nodeId);
    }

    public void removeNode(byte nodeId) {
        ZWaveExecutor.getInstance().removeNode(nodeId);
    }

    public void setInitNode(byte nodeId) {
        ZWaveExecutor.getInstance().setInitNode(nodeId);
    }

    public boolean isInitNode(byte nodeId){
        return ZWaveExecutor.getInstance().isInitNode(nodeId);
    }

    public void setHealNode(byte nodeId) {
        ZWaveExecutor.getInstance().setHealNode(nodeId);
    }

    public boolean isHealNode(byte nodeId){
        return ZWaveExecutor.getInstance().isHealNode(nodeId);
    }

    // Command Class Functions
    public int getVersion(ZWavePath path) {
        return ZWaveExecutor.getInstance().getVersion(path);
    }

    public void updateVersion(ZWavePath path, int version) {
        ZWaveExecutor.getInstance().updateVersion(path, version);
    }

    public void addCommandClasses(byte nodeId, byte[] supportedCommandClasses, boolean secure) {
        ZWaveExecutor.getInstance().addCommandClasses(nodeId, supportedCommandClasses, secure);
    }

    public void refreshCommands(ZWavePath path, String refreshCommands){
        ZWaveExecutor.getInstance().refreshCommands(path, refreshCommands);
    }

    public void setupPolling(ZWavePath path, int pollRate) {
        ZWaveExecutor.getInstance().setupPolling(path, pollRate);
    }

    // Message Functions
    public void clearQueue(){
        ZWaveExecutor.getInstance().clearQueue();

    }

    public void sendMessage(Message message) {
        ZWaveExecutor.getInstance().sendMessage(message);
    }

    public Message createMessage(ZWavePath path, CommandAdapter command, boolean secure) {
        return ZWaveExecutor.getInstance().createMessage(path.getNodeId(), command, secure);
    }

    public void sendCommand(ZWavePath path, CommandAdapter command, boolean secure) {
        sendCommand(path.getNodeId(), command, secure);
    }

    public void sendCommand(int nodeId, CommandAdapter command, boolean secure) {
        sendCommand(nodeId, command, secure, false);
    }

    public void sendCommand(int nodeId, CommandAdapter command, boolean secure, boolean priority) {
        ZWaveExecutor.getInstance().sendCommand(nodeId, command, secure, priority);
    }

    // Tag Functions
    public void configureTag(ZWavePath path, DataType dataType) {
        configureTag(path, dataType, false);
    }

    public void configureTag(ZWavePath path, DataType dataType, boolean registerWriteHandler) {
        ZWaveExecutor.getInstance().configureTag(path, dataType, registerWriteHandler);
    }

    public void updateConfiguredTags(ZWavePath path, String configuredTags){
        ZWaveExecutor.getInstance().updateConfiguredTags(path, configuredTags);
    }

    public void configureTagInitValue(ZWavePath path, DataType dataType, Object value) {
        configureTagInitValue(path, dataType, value, false);
    }

    public void configureTagInitValue(ZWavePath path, DataType dataType, Object value, boolean registerWriteHandler) {
        ZWaveExecutor.getInstance().configureTagInitValue(path, dataType, value, registerWriteHandler);
    }

    public Object readTag(ZWavePath path) {
        return ZWaveExecutor.getInstance().readTag(path);
    }

    public void updateTag(ZWavePath path, Object o) {
        if(path.getHomeId() != 0) {
            ZWaveExecutor.getInstance().updateTag(path, o);
        }
    }

    public void removeTag(ZWavePath path) {
        ZWaveExecutor.getInstance().removeTag(path);
    }

}
