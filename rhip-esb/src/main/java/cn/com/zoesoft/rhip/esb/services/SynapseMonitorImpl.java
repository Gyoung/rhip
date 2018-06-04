package cn.com.zoesoft.rhip.esb.services;

import java.util.Date;

/**
 * Created by qiuyungen on 2016/9/1.
 */
public class SynapseMonitorImpl implements ISynapseMonitor {

    private static final String SYSTEM_START_TIME_MILLIS = String.valueOf(new Date().getTime());

    @Override
    public String getSystemStartTimeMillis() {
        return SYSTEM_START_TIME_MILLIS;
    }
}
