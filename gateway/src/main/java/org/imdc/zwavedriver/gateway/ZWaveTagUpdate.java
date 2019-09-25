package org.imdc.zwavedriver.gateway;

import com.inductiveautomation.ignition.common.model.values.QualityCode;

public class ZWaveTagUpdate implements Runnable {
    private String path;
    private Object value;
    private QualityCode quality;

    public ZWaveTagUpdate(String path, Object value, QualityCode quality) {
        this.path = path;
        this.value = value;
        this.quality = quality;
    }

    @Override
    public void run() {
        ZWaveExecutor.getInstance().updateTag(path, value, quality);
    }
}
