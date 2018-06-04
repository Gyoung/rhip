package cn.com.zoesoft.rhip.esb.services;


import cn.com.zoesoft.rhip.esb.authorization.provider.VerifyAuthModel;
import cn.com.zoesoft.util.JsonUtil;
import cn.com.zoesoft.util.StringUtil;
import com.zoe.phip.ssp.model.service.webservice.VerifyAuthorization;

import com.zoe.phip.ssp.service.service.webservice.*;
import com.zoe.phip.ssp.service.stub.generated.ISspControlServiceServiceStub;
import org.apache.axis2.AxisFault;

/**
 * Created by qiuyungen on 2016/8/24.
 */
public final class SspControlService {

    private static SspControlService controlService;
    private final String url;

    private SspControlService(String url) throws AxisFault {
        this.url = url;
    }

    public static final void build(String url) throws AxisFault {
        controlService = new SspControlService(url);
    }

    public static final SspControlService instance() {
        return controlService;
    }

    public String applySynapseSetting() throws Exception {
        ISspControlServiceServiceStub serviceStub = new ISspControlServiceServiceStub(url);
        try {
            ApplySynapseSettingE applySynapseSettingE = new ApplySynapseSettingE();
            applySynapseSettingE.setApplySynapseSetting(new ApplySynapseSetting());
            ApplySynapseSettingResponseE response = serviceStub.applySynapseSetting(applySynapseSettingE);
            final String result = response.getApplySynapseSettingResponse().get_return();
            if (result != null)
                throw new Exception(result);
            return result;
        } finally {
            serviceStub.cleanup();
        }
    }

    public String publishService() throws Exception {
        ISspControlServiceServiceStub serviceStub = new ISspControlServiceServiceStub(url);
        try {
            PublishServiceE publishServiceE = new PublishServiceE();
            publishServiceE.setPublishService(new PublishService());
            PublishServiceResponseE response = serviceStub.publishService(publishServiceE);
            final String result = response.getPublishServiceResponse().get_return();
            if (result != null)
                throw new Exception(result);
            return result;
        } finally {
            serviceStub.cleanup();
        }
    }

    public VerifyAuthModel verifyServiceSerct(String serviceId, int serviceType, String serviceSerct) throws Exception {
        ISspControlServiceServiceStub serviceStub = new ISspControlServiceServiceStub(url);
        try {
            VerifyServiceSerctE serviceSerctE = new VerifyServiceSerctE();
            VerifyServiceSerct verifyServiceSerct = new VerifyServiceSerct();
            verifyServiceSerct.setArg0(serviceId);
            verifyServiceSerct.setArg1(serviceType);
            verifyServiceSerct.setArg2(serviceSerct);

            serviceSerctE.setVerifyServiceSerct(verifyServiceSerct);
            VerifyServiceSerctResponseE response = serviceStub.verifyServiceSerct(serviceSerctE);
            final String result = response.getVerifyServiceSerctResponse().get_return();

            if (result == null)
                return null;

            VerifyAuthorization authorization = JsonUtil.json2Object(result, VerifyAuthorization.class);

            VerifyAuthModel authModel = new VerifyAuthModel()
                    .setAccessBaseId(authorization.getAccessBaseId())
                    .setFailTime(authorization.getFailTime())
                    .setMessage(authorization.getMessage())
                    .setSuccess(authorization.isSuccess())
                    .setPrivateKey(authorization.getPrivateKey());
            authModel.setPublicKey(authorization.getPublicKey());
            authModel.setEnabledCrypto(authorization.isEnabledCrypto());
            authModel.setAuthorizationState(authorization.isAuthorizationState());
            return authModel;
        } finally {
            serviceStub.cleanup();
        }
    }
}