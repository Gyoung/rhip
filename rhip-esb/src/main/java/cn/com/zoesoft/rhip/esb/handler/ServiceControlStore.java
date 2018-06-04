package cn.com.zoesoft.rhip.esb.handler;

import cn.com.zoesoft.rhip.esb.handler.support.ComplexServiceControlHandler;
import cn.com.zoesoft.rhip.esb.handler.support.ProxyServiceControlHandler;
import cn.com.zoesoft.rhip.esb.handler.support.RestfulServiceControlHandler;
import com.zoe.phip.ssp.model.ServiceType;
import com.zoe.phip.ssp.model.synapse.SynapseFaultException;


import java.util.HashMap;
import java.util.Map;

/**
 * Created by qiuyungen on 2016/5/25.
 */
public final class ServiceControlStore {

    private static Map<ServiceType, ServiceControlHandler> handlerMap = new HashMap<>();

    static {
        handlerMap.put(ServiceType.PROXY, new ProxyServiceControlHandler());
        handlerMap.put(ServiceType.RESTFUL, new RestfulServiceControlHandler());
        handlerMap.put(ServiceType.COMPLEX, new ComplexServiceControlHandler());
    }

    public static ServiceControlHandler getHandler(ServiceType serviceType) throws SynapseFaultException {
        if (handlerMap.containsKey(serviceType))
            return handlerMap.get(serviceType);

        throw new SynapseFaultException("The service type (" + serviceType + ") not support!");
    }
}
