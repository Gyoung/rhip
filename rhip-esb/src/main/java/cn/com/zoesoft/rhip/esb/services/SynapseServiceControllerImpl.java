package cn.com.zoesoft.rhip.esb.services;

import cn.com.zoe.crypto.CompressMode;
import cn.com.zoe.crypto.CryptoUtil;
import cn.com.zoesoft.rhip.esb.control.SynapseControl;
import cn.com.zoesoft.rhip.esb.handler.ServiceControlStore;
import cn.com.zoesoft.util.JsonUtil;
import cn.com.zoesoft.util.StringUtil;
import com.zoe.phip.ssp.model.ServiceType;
import com.zoe.phip.ssp.model.synapse.*;
import com.zoe.phip.ssp.service.synapse.ISynapseServiceController;

import java.util.List;

/**
 * Created by qiuyungen on 2016/5/25.
 */
public class SynapseServiceControllerImpl implements ISynapseServiceController {

    @Override
    public boolean addProxyService(ProxyServiceEntity proxy) throws SynapseFaultException {
        return ServiceControlStore.getHandler(proxy.getBasis().serviceType()).add(proxy);
    }

    @Override
    public boolean addRestfulService(RestfulServiceEntity restfulService) throws SynapseFaultException {
        return ServiceControlStore.getHandler(restfulService.getBasis().serviceType()).add(restfulService);
    }

    @Override
    public boolean addComplexService(ComplexServiceEntity complexService) throws SynapseFaultException {
        return ServiceControlStore.getHandler(complexService.getBasis().serviceType()).add(complexService);
    }

    @Override
    public ProxyServiceEntity getProxyService(String proxyName) throws SynapseFaultException {
        return (ProxyServiceEntity) ServiceControlStore.getHandler(ServiceType.PROXY).getService(proxyName);
    }

    @Override
    public RestfulServiceEntity getRestfulService(String restfulName) throws SynapseFaultException {
        return (RestfulServiceEntity) ServiceControlStore.getHandler(ServiceType.RESTFUL).getService(restfulName);
    }

    @Override
    public ComplexServiceEntity getComplexService(String complexName) throws SynapseFaultException {
        return (ComplexServiceEntity) ServiceControlStore.getHandler(ServiceType.COMPLEX).getService(complexName);
    }

    @Override
    public String getSource(String serviceName, int serviceType) throws SynapseFaultException {
        return ServiceControlStore.getHandler(ServiceType.valueOf(serviceType)).getSource(serviceName);
    }

    @Override
    public boolean startService(String serviceName, int serviceType) throws SynapseFaultException {
        return ServiceControlStore.getHandler(ServiceType.valueOf(serviceType)).start(serviceName);
    }

    @Override
    public boolean stopService(String serviceName, int serviceType) throws SynapseFaultException {
        return ServiceControlStore.getHandler(ServiceType.valueOf(serviceType)).stop(serviceName);
    }

    @Override
    public boolean deleteService(String serviceName, int serviceType) throws SynapseFaultException {
        return ServiceControlStore.getHandler(ServiceType.valueOf(serviceType)).delete(serviceName);
    }

    @Override
    public boolean applyControlSetting(SynapseControlSetting setting) {
        SynapseControl.instance().applySetting(setting);
        return true;
    }

    @Override
    public boolean applyAuthorization(SynapseAuthorizationObject authorizationObject) {
        SynapseControl.instance().putAuthorizationObject(authorizationObject);
        return true;
    }

    @Override
    public boolean applyGlobalSetting(String setting) throws SynapseFaultException {

        SynapseGlobalSetting globalSetting = setting == null || setting.trim().isEmpty() ? null :
                JsonUtil.json2Object(setting, SynapseGlobalSetting.class);

        if (globalSetting != null) {
            CryptoUtil.setCompressMode(
                    globalSetting.isCryptoCompress() ? CompressMode.Internal : CompressMode.NotSupport
            );
        }

        return true;
    }

    @Override
    public boolean setPublicAddress(List<String> addresses) {
        return false;
    }
}
