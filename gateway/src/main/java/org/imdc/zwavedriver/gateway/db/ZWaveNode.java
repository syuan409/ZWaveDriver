package org.imdc.zwavedriver.gateway.db;

import org.imdc.zwavedriver.gateway.ZWavePath;
import com.inductiveautomation.ignition.gateway.localdb.persistence.IdentityField;
import com.inductiveautomation.ignition.gateway.localdb.persistence.LongField;
import com.inductiveautomation.ignition.gateway.localdb.persistence.PersistenceInterface;
import com.inductiveautomation.ignition.gateway.localdb.persistence.PersistentRecord;
import com.inductiveautomation.ignition.gateway.localdb.persistence.RecordMeta;
import com.inductiveautomation.ignition.gateway.localdb.persistence.ReferenceField;
import com.inductiveautomation.ignition.gateway.localdb.persistence.StringField;
import simpleorm.dataset.SFieldFlags;
import simpleorm.dataset.SQuery;

import java.util.List;

public class ZWaveNode extends PersistentRecord {
    public static final RecordMeta<ZWaveNode> META = new RecordMeta(
            ZWaveNode.class,
            "zwave_nodes");

    public static final IdentityField Id = new IdentityField(META, "id");
    public static final LongField HomeId = new LongField(META, "home_id", SFieldFlags.SMANDATORY);
    public static final ReferenceField<ZWaveHome> Home = new ReferenceField<ZWaveHome>(META,
            ZWaveHome.META, "home", HomeId);
    public static final StringField NodeId = new StringField(META, "node_id", SFieldFlags.SMANDATORY);

    @Override
    public RecordMeta<?> getMeta() {
        return META;
    }

    public long getId() {
        return getLong(Id);
    }

    public String getNodeId(){
        return getString(NodeId);
    }

    public static List<ZWaveNode> getNodes(PersistenceInterface db, ZWavePath path) {
        SQuery<ZWaveHome> homeQuery = new SQuery(ZWaveHome.META);
        homeQuery.eq(ZWaveHome.HomeId, path.getHomeIdHex());
        ZWaveHome home = db.queryOne(homeQuery);

        SQuery<ZWaveNode> query = new SQuery(ZWaveNode.META);
        query.eq(ZWaveNode.HomeId, home.getId());
        return db.query(query);
    }

    public static boolean createNodeIfNotExists(PersistenceInterface db, ZWavePath path) {
        SQuery<ZWaveHome> homeQuery = new SQuery(ZWaveHome.META);
        homeQuery.eq(ZWaveHome.HomeId, path.getHomeIdHex());
        ZWaveHome home = db.queryOne(homeQuery);

        SQuery<ZWaveNode> query = new SQuery(ZWaveNode.META);
        query.eq(ZWaveNode.HomeId, home.getId());
        query.eq(ZWaveNode.NodeId, path.getNodeIdHex());
        ZWaveNode node = db.queryOne(query);

        if (node == null) {
            node = db.createNew(ZWaveNode.META);
            node.installDefaultValues();
            node.setLong(ZWaveNode.HomeId, home.getId());
            node.setString(ZWaveNode.NodeId, path.getNodeIdHex());
            db.save(node);
            return true;
        }

        return false;
    }

    public static void remove(PersistenceInterface db, ZWavePath path) {
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
            for (ZWaveCommandClass cc : db.query(query)) {
                if (cc != null) {
                    cc.deleteRecord();
                    db.save(cc);
                }
            }

            node.deleteRecord();
            db.save(node);
        }
    }
}