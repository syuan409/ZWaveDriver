package org.imdc.zwavedriver.gateway.db;

import com.inductiveautomation.ignition.gateway.model.IgnitionWebApp;
import com.inductiveautomation.ignition.gateway.web.components.RecordEditForm;
import com.inductiveautomation.ignition.gateway.web.models.ConfigCategory;
import com.inductiveautomation.ignition.gateway.web.models.DefaultConfigTab;
import com.inductiveautomation.ignition.gateway.web.models.IConfigTab;
import com.inductiveautomation.ignition.gateway.web.pages.IConfigPage;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.wicket.Application;
import org.apache.wicket.model.Model;

public class ZWaveInternalConfigurationPage extends RecordEditForm {
    public static final ConfigCategory ZWAVE_CATEGORY =
            new ConfigCategory("zwave", "zwave.Config.MenuTitle", 700);
    public static final IConfigTab CONFIG_TAB = DefaultConfigTab.builder()
            .category(ZWAVE_CATEGORY)
            .name("zwave")
            .i18n("zwave.Config.Settings.MenuTitle")
            .page(ZWaveInternalConfigurationPage.class)
            .terms("zwave")
            .build();

    public ZWaveInternalConfigurationPage(IConfigPage configPage) {
        super(configPage, null, Model.of("ZWave Configuration"), (((IgnitionWebApp) Application.get()).getContext())
                .getPersistenceInterface().find(
                        ZWaveInternalConfiguration.META, 0L));
    }

    @Override
    public Pair<String, String> getMenuLocation() {
        return CONFIG_TAB.getMenuLocation();
    }
}
