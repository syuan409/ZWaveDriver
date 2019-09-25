package org.imdc.zwavedriver.gateway;

import org.imdc.zwavedriver.zwave.messages.*;
import org.imdc.zwavedriver.zwave.messages.framework.ExclusionMode;
import org.imdc.zwavedriver.zwave.messages.framework.InclusionMode;
import com.inductiveautomation.ignition.common.config.BasicBoundPropertySet;
import com.inductiveautomation.ignition.common.config.BasicDescriptiveProperty;
import com.inductiveautomation.ignition.common.config.DescriptiveProperty;
import com.inductiveautomation.ignition.common.config.Property;
import com.inductiveautomation.ignition.common.model.values.BasicQualifiedValue;
import com.inductiveautomation.ignition.common.model.values.QualifiedValue;
import com.inductiveautomation.ignition.common.model.values.QualityCode;
import com.inductiveautomation.ignition.common.script.builtin.LegacyTagUtilities;
import com.inductiveautomation.ignition.common.sqltags.model.types.DataType;
import com.inductiveautomation.ignition.common.tags.model.SecurityContext;
import com.inductiveautomation.ignition.common.tags.model.TagPath;
import com.inductiveautomation.ignition.common.tags.model.TagProvider;
import com.inductiveautomation.ignition.common.tags.paths.parser.TagPathParser;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import com.inductiveautomation.ignition.gateway.tags.managed.ManagedTagProvider;
import com.inductiveautomation.ignition.gateway.tags.managed.ProviderConfiguration;
import com.inductiveautomation.ignition.gateway.tags.managed.WriteHandler;
import org.apache.commons.lang3.StringUtils;
import org.imdc.zwavedriver.zwave.messages.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.inductiveautomation.ignition.gateway.util.GroupMapCollate.groupMapCollate;

public class ZWaveTags extends ZWaveWrapperFunctions implements WriteHandler {
    public static final String CAT_VALUE = "value";
    public static final Property<DataType> DataType = desc("dataType",
            com.inductiveautomation.ignition.common.sqltags.model.types.DataType.class,
            com.inductiveautomation.ignition.common.sqltags.model.types.DataType.Int4, CAT_VALUE);
    public static final Property<QualifiedValue> Value = desc("value", QualifiedValue.class, null, CAT_VALUE);

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final String PROVIDER_NAME = "ZWave";

    private GatewayContext context;
    private ManagedTagProvider tagProvider;

    public void setGatewayContext(GatewayContext context) {
        this.context = context;
        tagProvider = context.getTagManager().getOrCreateManagedProvider(new ProviderConfiguration(PROVIDER_NAME).setAllowTagCustomization(true).setPersistTags(true).setPersistValues(true));
    }

    public void setTagProvider(ManagedTagProvider tagProvider) {
        this.tagProvider = tagProvider;
    }

    public void startup() {

    }

    public void shutdown() {
        if (tagProvider != null) {
            tagProvider.shutdown(false);
        }
    }

    public void configureTag(String tagPath, DataType dataType) {
        configureTag(tagPath, dataType, null, false);
    }

    public void configureTag(String tagPath, DataType dataType, Object value, boolean registerWriteHandler) {
        BasicBoundPropertySet props = new BasicBoundPropertySet();
        props.set(DataType, dataType);
        if (value != null) {
            props.set(Value, new BasicQualifiedValue(value));
        }
        tagProvider.configureTag(tagPath, props);

        if(registerWriteHandler){
            tagProvider.registerWriteHandler(tagPath, this);
        }
    }

    public void tagUpdate(String tagPath, Object value) {
        tagUpdate(tagPath, value, QualityCode.Good);
    }

    public void tagUpdate(String tagPath, Object value, QualityCode qualityCode) {
        tagProvider.updateValue(tagPath, value, qualityCode);
    }

    public void removeTag(String tagPath) {
        tagProvider.removeTag(tagPath);
    }

    public QualifiedValue readTag(String tagPath) {
        try {
            List<TagPath> paths = new ArrayList<>();
            paths.add(TagPathParser.parse(PROVIDER_NAME, tagPath));
            List<QualifiedValue> qv = groupMapCollate(paths, TagPath::getSource, prov -> {
                TagProvider p = getTagProvider(prov, false);
                return provPaths -> p.readAsync(provPaths, SecurityContext.systemContext());
            }).get(LegacyTagUtilities.LEGACY_DEFAULT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
            QualifiedValue[] arr = new QualifiedValue[qv.size()];
            return qv.toArray(arr)[0];
        } catch (Exception ex) {
            logger.error("Error reading tag '" + tagPath + "'", ex);
        }

        return null;
    }

    protected TagProvider getTagProvider(String name, boolean throwError) {
        String provName = StringUtils.isBlank(name) ? PROVIDER_NAME : name;
        TagProvider p = context.getTagManager().getTagProvider(provName);
        if (p == null && throwError) {
            throw new IllegalArgumentException(String.format("Tag provider '%s' could not be found", provName));
        }
        return p;
    }

    private void deleteHome(ZWavePath path) {
        tagProvider.removeTag(path.getHomePath());
    }

    @Override
    public QualityCode write(TagPath tagPath, Object o) {
        try {
            ZWavePath path = new ZWavePath(tagPath.toStringPartial());

            String pathStr = tagPath.toStringFull();
            tagUpdate(pathStr, o);

            if (pathStr.endsWith("AddNode") || pathStr.endsWith("AddSecureNode")) {
                tagUpdate(pathStr, false);

                if (pathStr.endsWith("AddSecureNode")) {
                    setSecureAdd(true);
                }

                sendMessage(new AddNodeMessage.Request(InclusionMode.ANY));
            } else if (pathStr.endsWith("RemoveNode")) {
                tagUpdate(pathStr, false);
                sendMessage(new RemoveNodeMessage.Request(ExclusionMode.ANY));
            } else if (pathStr.endsWith("RemoveFailedNode")) {
                tagUpdate(pathStr, false);
                sendMessage(new RemoveFailedNodeMessage.Request(path.getNodeId()));
            } else if (pathStr.endsWith("ReplaceFailedNode")) {
                tagUpdate(pathStr, false);
                setInitNode(path.getNodeId());
                sendMessage(new ReplaceFailedNodeMessage.Request(path.getNodeId()));
            } else if (pathStr.endsWith("InitNode")) {
                tagUpdate(pathStr, false);
                setInitNode(path.getNodeId());
            } else if (pathStr.endsWith("HealNetwork")) {
                tagUpdate(pathStr, false);
                healNetwork();
            }  else if (pathStr.endsWith("HealNetworkNode")) {
                tagUpdate(pathStr, false);
                healNetworkNode(path.getNodeId());
            } else if (pathStr.endsWith("Reinitialize")) {
                tagUpdate(pathStr, false);
                if (path.isNodeSet()) {
                    initNode(path.getNodeId());
                } else {
                    boolean currentHome = false;
                    if(path.getHomeId() == getPath().getHomeId()) {
                        currentHome = true;
                    }
                    deleteHome(path);

                    if(currentHome) {
                        updateConfiguredTags(path, null);
                        setInitComplete(false);
                        sendMessage(new MemoryGetIdMessage.Request());
                    }
                }
            } else {
                if (pathStr.endsWith("PollRate")) {
                    setupPolling(path, (Integer) o);
                } else if(!pathStr.endsWith("ApplicationUpdateRefreshCommands") && !pathStr.endsWith("Description")){
                    boolean secure = isSecure(path);
                    int version = getVersion(path);

                    if (pathStr.endsWith("Initialize") && ((Boolean) o)) {
                        tagUpdate(pathStr, false);
                        path.getCommandClassObj().getProcessor().refresh(path, version, secure);
                        return QualityCode.Good;
                    } else {
                        return path.getCommandClassObj().getProcessor().write(path, version, secure, o);
                    }
                }
            }

            return QualityCode.Good;
        } catch (Exception ex) {
            logger.error("Error writing to tag '" + tagPath.toStringPartial() + "'", ex);
            return QualityCode.Error;
        }
    }

    public static final String propertyKey(String property) {
        return "tags.editing.prop." + property;
    }

    public static final String categoryKey(String category) {
        return "tags.editing.category." + category;
    }

    protected static <T> DescriptiveProperty<T> desc(String name, Class<T> clazz, T defaultVal, String category) {
        return new BasicDescriptiveProperty<>(name, propertyKey(name), categoryKey(category),
                clazz, defaultVal);
    }
}
