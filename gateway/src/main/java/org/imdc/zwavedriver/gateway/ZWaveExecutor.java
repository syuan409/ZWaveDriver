package org.imdc.zwavedriver.gateway;

import org.imdc.zwavedriver.gateway.db.ZWaveCommandClass;
import org.imdc.zwavedriver.gateway.db.ZWaveHome;
import org.imdc.zwavedriver.gateway.db.ZWaveNode;
import org.imdc.zwavedriver.zwave.Hex;
import org.imdc.zwavedriver.zwave.PortException;
import org.imdc.zwavedriver.zwave.messages.*;
import org.imdc.zwavedriver.zwave.messages.commandclasses.CommandClasses;
import org.imdc.zwavedriver.zwave.messages.commandclasses.VersionCommandClass;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandAdapter;
import org.imdc.zwavedriver.zwave.messages.framework.Message;
import com.inductiveautomation.ignition.common.model.values.QualityCode;
import com.inductiveautomation.ignition.common.sqltags.model.types.DataType;
import com.inductiveautomation.ignition.gateway.localdb.persistence.PersistenceInterface;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import org.imdc.zwavedriver.zwave.messages.*;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ZWaveExecutor {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static ZWaveExecutor _instance = null;

    private GatewayContext context;
    private ZWaveConfiguration zwaveConfig;
    private ZWaveMessages zwaveMessages;
    private ZWaveTags zwaveTags;
    private JSONObject configuredTags;

    public static ZWaveExecutor getInstance() {
        if (_instance == null) {
            _instance = new ZWaveExecutor();
        }

        return _instance;
    }

    private ZWaveExecutor() {
        zwaveConfig = new ZWaveConfiguration();
        zwaveMessages = new ZWaveMessages();
        zwaveTags = new ZWaveTags();
        configuredTags = new JSONObject();
    }

    public void setGatewayContext(GatewayContext context) {
        this.context = context;
        zwaveTags.setGatewayContext(context);
    }

    public void startup(String port, String networkKey) throws PortException {
        zwaveConfig.setNetworkKey(networkKey);
        zwaveMessages.startup(port);
        zwaveTags.startup();
    }

    public void shutdown() {
        zwaveMessages.shutdown();
        zwaveTags.shutdown();
    }

    private PersistenceInterface getPersistenceInterface() {
        return context.getPersistenceInterface();
    }

    public boolean initComplete() {
        return zwaveConfig.initComplete();
    }

    public void setInitComplete(boolean initComplete) {
        zwaveConfig.setInitComplete(initComplete);
    }

    // ZWavePath Functions
    public ZWavePath getPath() {
        return new ZWavePath(getHomeId());
    }

    public ZWavePath getPath(int nodeId) {
        return new ZWavePath(getHomeId(), (byte) nodeId);
    }

    // Home Functions
    public int getHomeId() {
        return zwaveConfig.getHomeId();
    }

    public byte getControllerNodeId() {
        return zwaveConfig.getControllerNodeId();
    }

    public boolean addHome(int homeId, byte nodeId) {
        ZWavePath path = new ZWavePath(homeId, nodeId);
        ZWaveHome home = ZWaveHome.getHome(getPersistenceInterface(), path);

        if (home == null) {
            logger.info("Home id '" + path.getHomeIdHex() + "' doesn't exist");
            ZWaveHome.addHome(getPersistenceInterface(), path);
        } else {
            logger.info("Found home id '" + path.getHomeIdHex() + "'");
            try {
                configuredTags = new JSONObject(home.getConfiguredTags());
                Iterator<String> itr = configuredTags.keys();
                while (itr.hasNext()) {
                    String key = itr.next();
                    JSONObject config = (JSONObject) configuredTags.get(key);
                    configureTag(new ZWavePath(key), DataType.valueOf(config.getString("dataType")), config.getBoolean("registerWriteHandler"), null);
                }
            } catch(Exception ex){
                logger.error("Error parsing JSON configured tags", ex);
            }
        }

        zwaveConfig.setHome(homeId, nodeId);
        setInitComplete(true);
        return configuredTags.length() > 0;
    }

    public void healNetworkNode(byte nodeId){
        if(nodeId != getControllerNodeId()) {
            ZWavePath path = getPath(nodeId);
            setHealNode(nodeId);
            sendMessage(new RequestNodeNeighborUpdate.Request(nodeId));
        }
    }

    public void healNetworkNodeFix(byte nodeId){
        ZWavePath path = getPath(nodeId);
        List<Byte> associations = new ArrayList<>();
        String basicDeviceClass = (String) readTag(path.tag("BasicDeviceClass"));
        if (basicDeviceClass.equals("04")) {
            path.clear();
            path.setCommandClass(CommandClasses.ASSOCIATION.getValue());
            Object ngObj = readTag(path.tag("NumGroupings"));
            if(ngObj != null) {
                int numGroupings = (Byte) ngObj;
                for (int i = 0; i < numGroupings; i++) {
                    String associationsHex = (String) readTag(path.tag("Associations", String.format("%d", i + 1), "Associations"));
                    byte[] associationBytes = Hex.hexToByteArray(associationsHex);
                    for (byte association : associationBytes) {
                        if (!associations.contains(association)) {
                            associations.add(association);
                        }

                        if (associations.size() == 5) {
                            break;
                        }
                    }

                    if (associations.size() == 5) {
                        break;
                    }
                }

                if (associations.size() > 0) {
                    sendMessage(new DeleteAllReturnRoutes.Request(nodeId));
                    for (byte association : associations) {
                        sendMessage(new AssignReturnRoute.Request(nodeId, association));
                    }
                }
            }
        }
    }

    public void healNetwork(){
        List<ZWaveNode> nodes = getNodes();
        for(ZWaveNode node : nodes){
            byte nodeId = Hex.stringToByte(node.getNodeId());
            healNetworkNode(nodeId);
        }
    }

    // Security Functions
    public byte[] getNetworkKey() {
        return zwaveConfig.getNetworkKey();
    }

    public byte[] getNetworkKeyA(boolean temp) {
        return zwaveConfig.getNetworkKeyA(temp);
    }

    public byte[] getNetworkKeyE(boolean temp) {
        return zwaveConfig.getNetworkKeyE(temp);
    }

    public boolean isSecure(ZWavePath path) {
        return ZWaveCommandClass.isSecure(getPersistenceInterface(), path);
    }

    public boolean isSecureAdd() {
        return zwaveConfig.isSecureAdd();
    }

    public void setSecureAdd(boolean secureAdd) {
        zwaveConfig.setSecureAdd(secureAdd);
    }

    // Node Functions
    public List<ZWaveNode> getNodes() {
        return ZWaveNode.getNodes(getPersistenceInterface(), getPath());
    }

    public void addNode(byte nodeId) {
        if (nodeId != 0 && nodeId != (byte) 0xFF) {
            ZWavePath path = getPath(nodeId);
            ZWaveNode.createNodeIfNotExists(getPersistenceInterface(), path);

            configureTagInitValue(path.tag("Id"), DataType.String, path.getNodeIdHex());
            configureTagInitValue(path.tag("Name"), DataType.String, "Node " + path.getNodeIdHex());
            configureTag(path.tag("Description"), DataType.String, true);
            configureTag(path.tag("IsListening"), DataType.Boolean);
            configureTag(path.tag("IsRouting"), DataType.Boolean);
            configureTag(path.tag("IsFrequentListening"), DataType.Boolean);
            configureTag(path.tag("Version"), DataType.Int2);
            configureTag(path.tag("BasicDeviceClass"), DataType.String);
            configureTag(path.tag("GenericDeviceClass"), DataType.String);
            configureTag(path.tag("SpecificDeviceClass"), DataType.String);
            configureTag(path.tag("NodeType"), DataType.String);
            configureTag(path.tag("Status"), DataType.String);
            configureTag(path.tag("Neighbors"), DataType.String);
            configureTagInitValue(path.tag("HealNetworkNode"), DataType.Boolean, false, true);
            configureTagInitValue(path.tag("Reinitialize"), DataType.Boolean, false, true);
            configureTagInitValue(path.tag("InitNode"), DataType.Boolean, false, true);
            configureTag(path.tag("ApplicationUpdateRefreshCommands"), DataType.String, true);
            configureTag(path.tag("ApplicationUpdate"), DataType.DateTime);

            initNode(nodeId);
        }
    }

    public void initNode(byte nodeId) {
        sendMessage(new IdentifyNodeMessage.Request(nodeId));
        sendMessage(new GetRoutingInfoMessage.Request(nodeId));

        if (nodeId != getControllerNodeId()) {
            setInitNode(nodeId);
//            sendMessage(new NoOperationMessage.Request(nodeId, new NoOpCommandClass.Set()));
            sendMessage(new RequestNodeInfoMessage.Request(nodeId));
        }
    }

    public void removeNode(byte nodeId) {
        ZWavePath path = getPath(nodeId);
        zwaveTags.removeTag(path.getNodePath());
        ZWaveNode.remove(getPersistenceInterface(), path);
    }

    public void setInitNode(byte nodeId) {
        zwaveConfig.setInitNode(nodeId);
    }

    public boolean isInitNode(byte nodeId){
        return zwaveConfig.isInitNode(nodeId);
    }

    public void setHealNode(byte nodeId) {
        zwaveConfig.setHealNode(nodeId);
    }

    public boolean isHealNode(byte nodeId) {
        return zwaveConfig.isHealNode(nodeId);
    }

    // Command Class Functions
    public int getVersion(ZWavePath path) {
        return ZWaveCommandClass.getVersion(getPersistenceInterface(), path);
    }

    public void updateVersion(ZWavePath path, int version) {
        ZWaveCommandClass.updateVersion(getPersistenceInterface(), path, version);
    }

    public void addCommandClasses(byte nodeId, byte[] supportedCommandClasses, boolean secure) {
        ZWavePath path = getPath(nodeId);

        for (byte commandClassByte : supportedCommandClasses) {
            if (commandClassByte != CommandClasses.UNKNOWN.getValue()) {
                path.setCommandClass(commandClassByte);

                try {
                    CommandClasses commandClass = path.getCommandClassObj();

                    ZWaveCommandClass.createCommandClassIfNotExists(getPersistenceInterface(), path, secure);
                    int version = getVersion(path);
                    int pollRate = getPollRate(path);

                    setupPolling(path, pollRate);

                    configureTagInitValue(path.tag("Id"), DataType.String, path.getCommandClassHex());
                    configureTagInitValue(path.tag("Name"), DataType.String, commandClass.getName());
                    configureTagInitValue(path.tag("Version"), DataType.Int2, version);
                    configureTagInitValue(path.tag("PollRate"), DataType.Int4, pollRate, true);
                    configureTagInitValue(path.tag("Secure"), DataType.Boolean, secure);
                    configureTagInitValue(path.tag("Initialize"), DataType.Boolean, false, true);
                    configureTagInitValue(path.tag("Supported"), DataType.Boolean, true);
                    configureTag(path.tag("LastUpdate"), DataType.DateTime);

                    if(commandClassByte != CommandClasses.NOOP.getValue() && commandClass.getProcessor() != null) {
                        commandClass.getProcessor().configureTags(path, version);

                        // Queue initial messages
                        sendCommand(path, new VersionCommandClass.GetCommandClass(commandClass), secure);
                        commandClass.getProcessor().queueInitialMessages(path, version, secure, true);
                    }
                } catch (Exception ex) {
                    logger.info("Command class " + path.getCommandClassHex() + " doesn't exist", ex);
                    configureTagInitValue(path.tag("Id"), DataType.String, path.getCommandClassHex());
                    configureTagInitValue(path.tag("Supported"), DataType.Boolean, false);
                }
            }
        }
    }

    public void refreshCommands(ZWavePath path, String refreshCommands){
        if (refreshCommands != null && refreshCommands.length() > 0) {
            try {
                String[] commands = refreshCommands.split(",");
                for (String command : commands) {
                    CommandClasses commandClass = CommandClasses.from(Hex.stringToByte(command.trim()));
                    if (commandClass != null) {
                        ZWavePath ccPath = path.copy();
                        ccPath.clear();
                        ccPath.setCommandClass(commandClass.getValue());
                        commandClass.getProcessor().refresh(ccPath);
                    }
                }
            } catch (Exception ex) {
                logger.error("Error refreshing commands", ex);
            }
        }
    }

    public int getPollRate(ZWavePath path) {
        return ZWaveCommandClass.getPollRate(getPersistenceInterface(), path);
    }

    public void setupPolling(ZWavePath path, int pollRate) {
        ZWaveCommandClass.updatePollRate(getPersistenceInterface(), path, pollRate);
        zwaveMessages.setupPolling(path, pollRate);
    }

    public void setupPolling(Message message, int pollRate) {
        zwaveMessages.setupPolling(message, pollRate);
    }

    // Message Functions
    public void clearQueue(){
        zwaveMessages.clearQueue();
    }

    public void sendMessage(Message messageObj) {
        zwaveMessages.addMessageToQueue(messageObj);
    }

    public Message createMessage(int nodeId, CommandAdapter command, boolean secure) {
        ZWavePath path = getPath(nodeId);
        return createMessage(path, command, secure);
    }

    public Message createMessage(ZWavePath path, CommandAdapter command, boolean secure) {
        return zwaveMessages._createMessage(path.getNodeId(), command, secure);
    }

    public void sendCommand(ZWavePath path, CommandAdapter command, boolean secure) {
        sendCommand(path, command, secure, false);
    }

    public void sendCommand(ZWavePath path, CommandAdapter command, boolean secure, boolean priority) {
        zwaveMessages.addCommandMessageToQueue(path.getNodeId(), command, secure, priority);
    }

    public void sendCommand(int nodeId, CommandAdapter command, boolean secure) {
        sendCommand(nodeId, command, secure, false);
    }

    public void sendCommand(int nodeId, CommandAdapter command, boolean secure, boolean priority) {
        ZWavePath path = getPath(nodeId);
        sendCommand(path, command, secure, priority);
    }

    // Tag Functions
    public void configureTag(ZWavePath path, DataType dataType) {
        configureTag(path, dataType, false);
    }

    public void configureTag(ZWavePath path, DataType dataType, boolean registerWriteHandler) {
        configureTag(path, dataType, registerWriteHandler, null);
    }

    public void configureTag(ZWavePath path, DataType dataType, boolean registerWriteHandler, Object value) {
        if(path.getHomeId() != 0) {
            try {
                if (!configuredTags.has(path.getFullPath())) {
                    JSONObject config = new JSONObject();
                    config.put("dataType", dataType.toString());
                    config.put("registerWriteHandler", registerWriteHandler);
                    configuredTags.put(path.getFullPath(), config);
                    updateConfiguredTags(path, configuredTags.toString());
                }
            } catch (Exception ex) {
                logger.error("Error creating JSON tag", ex);
            }

            zwaveTags.configureTag(path.getFullPath(), dataType, value, registerWriteHandler);
        }
    }

    public void updateConfiguredTags(ZWavePath path, String configuredTags){
        if(configuredTags == null){
            configuredTags = "{}";
        }
        ZWaveHome.updateConfiguredTags(getPersistenceInterface(), path, configuredTags.toString());
    }

    public void configureTagInitValue(ZWavePath path, DataType dataType, Object value) {
        configureTagInitValue(path, dataType, value, false);
    }

    public void configureTagInitValue(ZWavePath path, DataType dataType, Object value, boolean registerWriteHandler) {
        configureTagInitValue(path, dataType, value, registerWriteHandler, QualityCode.Good);
    }

    public void configureTagInitValue(ZWavePath path, DataType dataType, Object value, boolean registerWriteHandler, QualityCode quality) {
        configureTag(path, dataType, registerWriteHandler, value);
    }

    public Object readTag(ZWavePath path) {
        return zwaveTags.readTag(path.getFullPath()).getValue();
    }

    public void updateTag(ZWavePath path, Object value) {
        updateTag(path, value, QualityCode.Good);
    }

    public void updateTag(ZWavePath path, Object value, QualityCode quality) {
        updateTag(path.getFullPath(), value, quality);
    }

    public void updateTag(String path, Object value, QualityCode quality) {
        zwaveTags.tagUpdate(path, value, quality);
    }

    public void removeTag(ZWavePath path) {
        zwaveTags.removeTag(path.getFullPath());
    }

    public void scheduleTask(int delay, Runnable task) {
        ScheduledExecutorService scheduler
                = Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(task, delay, TimeUnit.SECONDS);
        scheduler.shutdown();
    }
}