package cn.com.zoesoft.rhip.esb.authorization.provider;

import cn.com.zoe.extensions.synapse.mediators.environment.zoe.ZoeEnvironment;
import cn.com.zoe.extensions.synapse.mediators.security.authorization.AuthorizationProvider;
import org.apache.synapse.MessageContext;
import org.apache.synapse.exception.MediatorRuntimeException;

import java.util.Map;

/**
 * Created by qiuyungen on 2016/8/17.
 */
public class ZoeAuthorizationApplyProvider implements AuthorizationProvider {
    private static final String CLASS_NAME = "com.zoe.phip.ssp.synapse.authorization.provider.ZoeAuthorizationApplyProvider";

    @Override
    public String getClassName() {
        return CLASS_NAME;
    }

    @Override
    public boolean verify(MessageContext messageContext, ZoeEnvironment zoeEnvironment, Map<String, Object> map) throws MediatorRuntimeException {

        final boolean result = zoeEnvironment.getValue(ZoeAuthorizationProvider.WS_INTERNAL_AUTH_STATE, true);
        if (!result) {
            final MediatorRuntimeException exception = zoeEnvironment.getValue(ZoeAuthorizationProvider.WS_INTERNAL_AUTH_STATE_THROWS, null);
            if (exception != null)
                throw exception;
        }
        return result;
    }
}