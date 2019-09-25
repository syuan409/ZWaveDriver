package org.imdc.zwavedriver.gateway;

import org.imdc.zwavedriver.gateway.db.ZWaveCommandClass;
import org.imdc.zwavedriver.gateway.db.ZWaveHome;
import org.imdc.zwavedriver.gateway.db.ZWaveInternalConfiguration;
import org.imdc.zwavedriver.gateway.db.ZWaveInternalConfigurationPage;
import org.imdc.zwavedriver.gateway.db.ZWaveNode;
import com.inductiveautomation.ignition.common.BundleUtil;
import com.inductiveautomation.ignition.common.licensing.LicenseState;
import com.inductiveautomation.ignition.common.script.ScriptManager;
import com.inductiveautomation.ignition.gateway.localdb.persistence.IRecordListener;
import com.inductiveautomation.ignition.gateway.localdb.persistence.PersistentRecord;
import com.inductiveautomation.ignition.gateway.model.AbstractGatewayModuleHook;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import com.inductiveautomation.ignition.gateway.web.models.ConfigCategory;
import com.inductiveautomation.ignition.gateway.web.models.IConfigTab;
import com.inductiveautomation.ignition.gateway.web.models.KeyValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import simpleorm.dataset.SQuery;

import java.util.Arrays;
import java.util.List;

public class GatewayHook extends AbstractGatewayModuleHook implements IRecordListener {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private GatewayContext context;
    private ZWaveExecutor executor;

    @Override
    public void setup(GatewayContext context) {
        this.context = context;

        BundleUtil.get().addBundle("zwave", GatewayHook.class, "zwave");

        ZWaveInternalConfiguration.META.addRecordListener(this);

        try {
            context.getSchemaUpdater().updatePersistentRecords(ZWaveInternalConfiguration.META);
            context.getSchemaUpdater().updatePersistentRecords(ZWaveHome.META);
            context.getSchemaUpdater().updatePersistentRecords(ZWaveNode.META);
            context.getSchemaUpdater().updatePersistentRecords(ZWaveCommandClass.META);
            executor = ZWaveExecutor.getInstance();
            executor.setGatewayContext(context);

            logger.info("Z-Wave module setup.");
        } catch (Exception e) {
            logger.error("Error setting up Z-Wave module.", e);
        }
    }

    @Override
    public void startup(LicenseState activationState) {
        try {
            init();

            logger.info("Z-Wave module started.");
        } catch (Exception e) {
            logger.error("Error starting up Z-Wave module.", e);
        }
    }

    public void init() {
        SQuery<ZWaveInternalConfiguration> query = new SQuery(ZWaveInternalConfiguration.META);
        ZWaveInternalConfiguration configuration = context.getPersistenceInterface().queryOne(query);

        if (configuration == null) {
            configuration = context.getPersistenceInterface().createNew(ZWaveInternalConfiguration.META);
            configuration.installDefaultValues();
            configuration.setInt(ZWaveInternalConfiguration.Id, 0);
            configuration.setString(ZWaveInternalConfiguration.Port, "");
            configuration.setString(ZWaveInternalConfiguration.NetworkKey, "");
            context.getPersistenceInterface().save(configuration);
        }

        reloadConfig(configuration);
    }

    private void reloadConfig(PersistentRecord persistentRecord) {
        try {
            ZWaveInternalConfiguration config = (ZWaveInternalConfiguration) persistentRecord;
            executor.startup(config.getPort(), config.getNetworkKey());
        } catch (Exception ex) {
            logger.error("Error starting up Z-Wave", ex);
        }
    }

    @Override
    public void shutdown() {
        try {
            executor.shutdown();

            logger.info("Z-Wave module stopped.");
        } catch (Exception e) {
            logger.error("Error stopping Z-Wave tag provider.", e);
        }
    }

    @Override
    public List<ConfigCategory> getConfigCategories() {
        return Arrays.asList(ZWaveInternalConfigurationPage.ZWAVE_CATEGORY);
    }

    @Override
    public List<? extends IConfigTab> getConfigPanels() {
        return Arrays.asList(ZWaveInternalConfigurationPage.CONFIG_TAB);
    }

    @Override
    public boolean isFreeModule() {
        return true;
    }

    @Override
    public void recordUpdated(PersistentRecord persistentRecord) {
        reloadConfig(persistentRecord);
    }

    @Override
    public void recordAdded(PersistentRecord persistentRecord) {
        reloadConfig(persistentRecord);
    }

    @Override
    public void recordDeleted(KeyValue keyValue) {

    }

    @Override
    public void initializeScriptManager(ScriptManager manager) {
        manager.addScriptModule("system.zwave", new ZWaveScriptingFunctions());
    }
}
