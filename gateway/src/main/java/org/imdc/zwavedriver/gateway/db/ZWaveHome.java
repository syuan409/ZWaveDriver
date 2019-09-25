package org.imdc.zwavedriver.gateway.db;

import org.imdc.zwavedriver.gateway.ZWavePath;
import com.inductiveautomation.ignition.gateway.localdb.persistence.*;
import simpleorm.dataset.SFieldFlags;
import simpleorm.dataset.SQuery;

import java.util.Date;

public class ZWaveHome extends PersistentRecord {
    public static final RecordMeta<ZWaveHome> META = new RecordMeta(
            ZWaveHome.class,
            "zwave_homes");

    public static final IdentityField Id = new IdentityField(META, "id");
    public static final StringField HomeId = new StringField(META, "home_id", SFieldFlags.SMANDATORY);
    public static final StringField NodeId = new StringField(META, "node_id", SFieldFlags.SMANDATORY);
    public static final BlobField ConfiguredTags = new BlobField(META, "tags", SFieldFlags.SMANDATORY).setDefault("{}".getBytes());
    public static final DateField Added = new DateField(META, "added_date", SFieldFlags.SMANDATORY);

    @Override
    public RecordMeta<?> getMeta() {
        return META;
    }

    public long getId() {
        return getLong(Id);
    }

    public String getConfiguredTags() { return new String(getBytes(ConfiguredTags)); }

    public static ZWaveHome getHome(PersistenceInterface db, ZWavePath path) {
        SQuery<ZWaveHome> query = new SQuery(ZWaveHome.META);
        query.eq(ZWaveHome.HomeId, path.getHomeIdHex());
        return db.queryOne(query);
    }

    public static void addHome(PersistenceInterface db, ZWavePath path) {
        ZWaveHome home = db.createNew(ZWaveHome.META);
        home.installDefaultValues();
        home.setString(ZWaveHome.HomeId, path.getHomeIdHex());
        home.setString(ZWaveHome.NodeId, path.getNodeIdHex());
        home.setBytes(ZWaveHome.ConfiguredTags, "{}".getBytes());
        home.setDate(ZWaveHome.Added, new Date());
        db.save(home);
    }

    public static void updateConfiguredTags(PersistenceInterface db, ZWavePath path, String configuredTags) {
        ZWaveHome home = getHome(db, path);
        if (home != null) {
            home.setBytes(ConfiguredTags, configuredTags.getBytes());
            db.save(home);
        }
    }
}