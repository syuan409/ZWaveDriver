package org.imdc.zwavedriver.gateway.db;

import org.imdc.zwavedriver.gateway.ZWavePath;
import com.inductiveautomation.ignition.gateway.localdb.persistence.BooleanField;
import com.inductiveautomation.ignition.gateway.localdb.persistence.IdentityField;
import com.inductiveautomation.ignition.gateway.localdb.persistence.IntField;
import com.inductiveautomation.ignition.gateway.localdb.persistence.LongField;
import com.inductiveautomation.ignition.gateway.localdb.persistence.PersistenceInterface;
import com.inductiveautomation.ignition.gateway.localdb.persistence.PersistentRecord;
import com.inductiveautomation.ignition.gateway.localdb.persistence.RecordMeta;
import com.inductiveautomation.ignition.gateway.localdb.persistence.ReferenceField;
import com.inductiveautomation.ignition.gateway.localdb.persistence.StringField;
import simpleorm.dataset.SFieldFlags;
import simpleorm.dataset.SQuery;

public class ZWaveCommandClass extends PersistentRecord {
    public static final RecordMeta<ZWaveCommandClass> META = new RecordMeta(
            ZWaveCommandClass.class,
            "zwave_command_classes");

    public static final IdentityField Id = new IdentityField(META, "id");
    public static final LongField NodeId = new LongField(META, "node_id", SFieldFlags.SMANDATORY);
    public static final ReferenceField<ZWaveNode> Node = new ReferenceField<ZWaveNode>(META,
            ZWaveNode.META, "node", NodeId);
    public static final StringField CommandClass = new StringField(META, "command_class", SFieldFlags.SMANDATORY);
    public static final IntField Version = new IntField(META, "version", SFieldFlags.SMANDATORY).setDefault(1);
    public static final BooleanField IsSecure = new BooleanField(META, "is_secure", SFieldFlags.SMANDATORY).setDefault(false);
    public static final IntField PollRate = new IntField(META, "pollrate", SFieldFlags.SMANDATORY).setDefault(0);

    @Override
    public RecordMeta<?> getMeta() {
        return META;
    }

    public long getId() {
        return getLong(Id);
    }

    public static boolean createCommandClassIfNotExists(PersistenceInterface db, ZWavePath path, boolean secure) {
        SQuery<ZWaveHome> homeQuery = new SQuery(ZWaveHome.META);
        homeQuery.eq(ZWaveHome.HomeId, path.getHomeIdHex());
        ZWaveHome home = db.queryOne(homeQuery);

        SQuery<ZWaveNode> nodeQuery = new SQuery(ZWaveNode.META);
        nodeQuery.eq(ZWaveNode.HomeId, home.getId());
        nodeQuery.eq(ZWaveNode.NodeId, path.getNodeIdHex());
        ZWaveNode node = db.queryOne(nodeQuery);

        SQuery<ZWaveCommandClass> query = new SQuery(ZWaveCommandClass.META);
        query.eq(ZWaveCommandClass.NodeId, node.getId());
        query.eq(ZWaveCommandClass.CommandClass, path.getCommandClassHex());
        ZWaveCommandClass cc = db.queryOne(query);

        if (cc == null) {
            cc = db.createNew(ZWaveCommandClass.META);
            cc.installDefaultValues();
            cc.setLong(ZWaveCommandClass.NodeId, node.getId());
            cc.setString(ZWaveCommandClass.CommandClass, path.getCommandClassHex());
            cc.setBoolean(ZWaveCommandClass.IsSecure, secure);
            db.save(cc);
            return true;
        }

        return false;
    }

    public static int getVersion(PersistenceInterface db, ZWavePath path) {
        SQuery<ZWaveHome> homeQuery = new SQuery(ZWaveHome.META);
        homeQuery.eq(ZWaveHome.HomeId, path.getHomeIdHex());
        ZWaveHome home = db.queryOne(homeQuery);

        SQuery<ZWaveNode> nodeQuery = new SQuery(ZWaveNode.META);
        nodeQuery.eq(ZWaveNode.HomeId, home.getId());
        nodeQuery.eq(ZWaveNode.NodeId, path.getNodeIdHex());
        ZWaveNode node = db.queryOne(nodeQuery);

        if (node != null) {
            SQuery<ZWaveCommandClass> query = new SQuery(ZWaveCommandClass.META);
            query.eq(ZWaveCommandClass.NodeId, node.getId());
            query.eq(ZWaveCommandClass.CommandClass, path.getCommandClassHex());
            ZWaveCommandClass cc = db.queryOne(query);
            if (cc != null) {
                return cc.getInt(Version);
            }
        }

        return 1;
    }

    public static int getPollRate(PersistenceInterface db, ZWavePath path) {
        SQuery<ZWaveHome> homeQuery = new SQuery(ZWaveHome.META);
        homeQuery.eq(ZWaveHome.HomeId, path.getHomeIdHex());
        ZWaveHome home = db.queryOne(homeQuery);

        SQuery<ZWaveNode> nodeQuery = new SQuery(ZWaveNode.META);
        nodeQuery.eq(ZWaveNode.HomeId, home.getId());
        nodeQuery.eq(ZWaveNode.NodeId, path.getNodeIdHex());
        ZWaveNode node = db.queryOne(nodeQuery);

        SQuery<ZWaveCommandClass> query = new SQuery(ZWaveCommandClass.META);
        query.eq(ZWaveCommandClass.NodeId, node.getId());
        query.eq(ZWaveCommandClass.CommandClass, path.getCommandClassHex());
        ZWaveCommandClass cc = db.queryOne(query);
        if (cc != null) {
            return cc.getInt(PollRate);
        }

        return 0;
    }

    public static boolean isSecure(PersistenceInterface db, ZWavePath path) {
        SQuery<ZWaveHome> homeQuery = new SQuery(ZWaveHome.META);
        homeQuery.eq(ZWaveHome.HomeId, path.getHomeIdHex());
        ZWaveHome home = db.queryOne(homeQuery);

        SQuery<ZWaveNode> nodeQuery = new SQuery(ZWaveNode.META);
        nodeQuery.eq(ZWaveNode.HomeId, home.getId());
        nodeQuery.eq(ZWaveNode.NodeId, path.getNodeIdHex());
        ZWaveNode node = db.queryOne(nodeQuery);

        if (node != null) {
            SQuery<ZWaveCommandClass> query = new SQuery(ZWaveCommandClass.META);
            query.eq(ZWaveCommandClass.NodeId, node.getId());
            query.eq(ZWaveCommandClass.CommandClass, path.getCommandClassHex());
            ZWaveCommandClass cc = db.queryOne(query);
            if (cc != null) {
                return cc.getBoolean(IsSecure);
            }
        }

        return false;
    }

    public static void updateVersion(PersistenceInterface db, ZWavePath path, int version) {
        SQuery<ZWaveHome> homeQuery = new SQuery(ZWaveHome.META);
        homeQuery.eq(ZWaveHome.HomeId, path.getHomeIdHex());
        ZWaveHome home = db.queryOne(homeQuery);

        SQuery<ZWaveNode> nodeQuery = new SQuery(ZWaveNode.META);
        nodeQuery.eq(ZWaveNode.HomeId, home.getId());
        nodeQuery.eq(ZWaveNode.NodeId, path.getNodeIdHex());
        ZWaveNode node = db.queryOne(nodeQuery);

        SQuery<ZWaveCommandClass> query = new SQuery(ZWaveCommandClass.META);
        query.eq(ZWaveCommandClass.NodeId, node.getId());
        query.eq(ZWaveCommandClass.CommandClass, path.getCommandClassHex());
        ZWaveCommandClass cc = db.queryOne(query);
        if (cc != null) {
            cc.setInt(Version, version);
            db.save(cc);
        }
    }

    public static void updatePollRate(PersistenceInterface db, ZWavePath path, int pollRate) {
        SQuery<ZWaveHome> homeQuery = new SQuery(ZWaveHome.META);
        homeQuery.eq(ZWaveHome.HomeId, path.getHomeIdHex());
        ZWaveHome home = db.queryOne(homeQuery);

        SQuery<ZWaveNode> nodeQuery = new SQuery(ZWaveNode.META);
        nodeQuery.eq(ZWaveNode.HomeId, home.getId());
        nodeQuery.eq(ZWaveNode.NodeId, path.getNodeIdHex());
        ZWaveNode node = db.queryOne(nodeQuery);

        SQuery<ZWaveCommandClass> query = new SQuery(ZWaveCommandClass.META);
        query.eq(ZWaveCommandClass.NodeId, node.getId());
        query.eq(ZWaveCommandClass.CommandClass, path.getCommandClassHex());
        ZWaveCommandClass cc = db.queryOne(query);
        if (cc != null) {
            cc.setInt(PollRate, pollRate);
            db.save(cc);
        }
    }

    public static void remove(PersistenceInterface db, ZWavePath path) {
        SQuery<ZWaveHome> homeQuery = new SQuery(ZWaveHome.META);
        homeQuery.eq(ZWaveHome.HomeId, path.getHomeIdHex());
        ZWaveHome home = db.queryOne(homeQuery);

        SQuery<ZWaveNode> nodeQuery = new SQuery(ZWaveNode.META);
        nodeQuery.eq(ZWaveNode.HomeId, home.getId());
        nodeQuery.eq(ZWaveNode.NodeId, path.getNodeIdHex());
        ZWaveNode node = db.queryOne(nodeQuery);

        SQuery<ZWaveCommandClass> query = new SQuery(ZWaveCommandClass.META);
        query.eq(ZWaveCommandClass.NodeId, node.getId());
        query.eq(ZWaveCommandClass.CommandClass, path.getCommandClassHex());
        ZWaveCommandClass cc = db.queryOne(query);
        if (cc != null) {
            cc.deleteRecord();
            db.save(cc);
        }
    }
}