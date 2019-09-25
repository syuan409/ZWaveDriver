package org.imdc.zwavedriver.gateway.db;

import com.inductiveautomation.ignition.gateway.localdb.persistence.Category;
import com.inductiveautomation.ignition.gateway.localdb.persistence.IdentityField;
import com.inductiveautomation.ignition.gateway.localdb.persistence.PersistentRecord;
import com.inductiveautomation.ignition.gateway.localdb.persistence.RecordMeta;
import com.inductiveautomation.ignition.gateway.localdb.persistence.StringField;

public class ZWaveInternalConfiguration extends PersistentRecord {
    public static final RecordMeta<ZWaveInternalConfiguration> META = new RecordMeta(
            ZWaveInternalConfiguration.class,
            "zwave_config");

    public static final IdentityField Id = new IdentityField(META, "id");
    public static final StringField Port = new StringField(META, "port").setDefault("");
    public static final StringField NetworkKey = new StringField(META, "network_key").setDefault("");

    public static final Category ZwaveConfigCategory = new Category("ZWaveInternalConfiguration.Category.Config", 125).include(Port, NetworkKey);

    @Override
    public RecordMeta<?> getMeta() {
        return META;
    }

    public String getPort() {
        return getString(Port);
    }

    public String getNetworkKey() {
        return getString(NetworkKey);
    }
}