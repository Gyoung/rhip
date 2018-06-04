package cn.com.zoesoft.rhip.esb.control;

import cn.com.zoe.extensions.synapse.mediators.environment.zoe.ZoeEnvironment;
import com.zoe.phip.ssp.model.synapse.SynapseAuthorizationObject;
import com.zoe.phip.ssp.model.synapse.SynapseControlSetting;
import com.zoe.phip.ssp.model.util.SynapseControlSettingValue;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by qiuyungen on 2016/8/24.
 */
public final class SynapseControl {

    private final ConcurrentHashMap<String, SynapseAuthorizationObject> authorizationObjectMap;
    private SynapseControlSetting setting = SynapseControlSettingValue.DEFAULT_SETTING;

    private SynapseControl() {
        this.authorizationObjectMap = new ConcurrentHashMap<>();
    }

    public static final SynapseControl instance() {
        return Holder.getSynapseControl();
    }

    public SynapseControlSetting getSetting() {
        return setting;
    }

    public <R extends SynapseControl> R applySetting(SynapseControlSetting setting) {
        synchronized (this) {
            this.setting = setting;
        }
        return (R) this;
    }

    public SynapseAuthorizationObject getAuthorizationObject(ZoeEnvironment environment, String serviceSerct) {
        return this.authorizationObjectMap.getOrDefault(serviceSerct, null);
    }

    public void putAuthorizationObject(SynapseAuthorizationObject authorizationObject) {
        final String serviceSerct = authorizationObject.getServiceSerct();
        if (authorizationObject.getState() == SynapseAuthorizationObject.STATE_MODIFY) {
            if (!this.authorizationObjectMap.containsKey(serviceSerct)) {
                this.authorizationObjectMap.put(serviceSerct, authorizationObject);
            } else {
                this.authorizationObjectMap.replace(serviceSerct, authorizationObject);
            }
        } else if (authorizationObject.getState() == SynapseAuthorizationObject.STATE_REMOVE) {
            this.authorizationObjectMap.remove(serviceSerct);
        }
    }

    private static final class Holder {
        private static final SynapseControl SYNAPSE_CONTROL = new SynapseControl();

        public static SynapseControl getSynapseControl() {
            return SYNAPSE_CONTROL;
        }
    }
}