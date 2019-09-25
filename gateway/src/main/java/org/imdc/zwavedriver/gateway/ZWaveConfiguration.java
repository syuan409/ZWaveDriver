package org.imdc.zwavedriver.gateway;

import org.imdc.zwavedriver.zwave.ByteUtilities;
import org.imdc.zwavedriver.zwave.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ZWaveConfiguration {
    private static byte[] TEMP_NETWORK_KEY = new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private int homeId;
    private byte controllerNodeId;
    private byte[] privateNetworkKey;
    private boolean secureAdd = false, initComplete = false;
    private List<Byte> initNodes = new ArrayList<>();
    private List<Byte> healNodes = new ArrayList<>();

    public void setHome(int homeId, byte controllerNodeId) {
        this.homeId = homeId;
        this.controllerNodeId = controllerNodeId;
    }

    public int getHomeId() {
        return homeId;
    }

    public byte getControllerNodeId() {
        return controllerNodeId;
    }

    public void setNetworkKey(String networkKey) {
        privateNetworkKey = Hex.hexToByteArray(networkKey);
    }

    public byte[] getNetworkKey() {
        return privateNetworkKey;
    }

    public byte[] getNetworkKeyE(boolean temp) {
        return ByteUtilities.copy(ByteUtilities.encryptAES128ECB(temp ? TEMP_NETWORK_KEY : privateNetworkKey, new byte[]{(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA}), 0, 16);
    }

    public byte[] getNetworkKeyA(boolean temp) {
        return ByteUtilities.copy(ByteUtilities.encryptAES128ECB(temp ? TEMP_NETWORK_KEY : privateNetworkKey, new byte[]{(byte) 0x55, (byte) 0x55, (byte) 0x55, (byte) 0x55, (byte) 0x55, (byte) 0x55, (byte) 0x55, (byte) 0x55, (byte) 0x55, (byte) 0x55, (byte) 0x55, (byte) 0x55, (byte) 0x55, (byte) 0x55, (byte) 0x55, (byte) 0x55}), 0, 16);
    }

    public boolean isSecureAdd() {
        return secureAdd;
    }

    public void setSecureAdd(boolean secureAdd) {
        this.secureAdd = secureAdd;
    }

    public boolean initComplete() {
        return initComplete;
    }

    public void setInitComplete(boolean initComplete) {
        this.initComplete = initComplete;
    }

    public void setInitNode(byte nodeId){
        if(!initNodes.contains(nodeId)){
            initNodes.add(nodeId);
        }
    }

    public boolean isInitNode(byte nodeId){
        if(initNodes.contains(nodeId)){
            initNodes.remove(new Byte(nodeId));
            return true;
        }

        return false;
    }

    public void setHealNode(byte nodeId){
        if(!healNodes.contains(nodeId)){
            healNodes.add(nodeId);
        }
    }

    public boolean isHealNode(byte nodeId){
        if(healNodes.contains(nodeId)){
            healNodes.remove(new Byte(nodeId));
            return true;
        }

        return false;
    }
}
