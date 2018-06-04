package cn.com.zoesoft.rhip.esb.authorization.provider;

import cn.com.zoe.crypto.CryptoUtil;
import cn.com.zoe.extensions.synapse.exception.ZoeMediatorErrorCode;
import cn.com.zoe.extensions.synapse.mediators.environment.zoe.ZoeEnvironment;
import cn.com.zoe.extensions.synapse.mediators.security.authorization.AuthorizationProvider;
import cn.com.zoe.extensions.synapse.mediators.security.core.Base64;
import cn.com.zoesoft.rhip.esb.control.SynapseControl;
import cn.com.zoesoft.rhip.esb.services.SspControlService;
import com.alibaba.fastjson.JSON;
import com.zoe.phip.ssp.model.ServiceType;
import com.zoe.phip.ssp.model.synapse.SynapseAuthorizationObject;
import com.zoe.phip.ssp.model.synapse.ZoeAuthToken;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.exception.MediatorRuntimeException;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by qiuyungen on 2016/8/17.
 */
public class ZoeAuthorizationProvider implements AuthorizationProvider {

    public static final String WS_INTERNAL_AUTH_STATE = "ws.internal.auth.state";
    public static final String WS_INTERNAL_AUTH_STATE_THROWS = "ws.internal.auth.state.throws";

    private static final Log LOG = LogFactory.getLog(ZoeAuthorizationProvider.class);
    private static final String TOKEN = "token";
    private static final String EMPTY = "";
    private static final String TOKEN_CONF_CRYPTO = "token.crypto";
    private static final String ZOE_ENVIRONMENT_CRYPTO_ENABLED = "ws.crypto.enabled";
    private static final String ZOE_ENVIRONMENT_CRYPTO_KEY = "ws.crypto.key";
    private static final String ZOE_ENVIRONMENT_CRYPTO_PUBLICKEY = "ws.crypto.publicKey";
    private static final String ZOE_ENVIRONMENT_CRYPTO_PRIVATEKEY = "ws.crypto.privateKey";
    private static final String CLASS_NAME = "com.zoe.phip.ssp.synapse.authorization.provider.ZoeAuthorizationProvider";

    private static void throwException(String message, Object... args) throws MediatorRuntimeException {
        message = String.format("%s (%s)", ZoeMediatorErrorCode.PRE_AUTHORIZATION.message(), message);
        throw MediatorRuntimeException.create(ZoeMediatorErrorCode.PRE_AUTHORIZATION.code(), message, args);
    }

    private static void printMessage(String parameter, Object value) {
        if (parameter == null)
            System.out.println(value == null ? "null" : value);
        else
            System.out.println(String.format("%s => %s", parameter, value == null ? "null" : value));
    }

    @Override
    public String getClassName() {
        return CLASS_NAME;
    }

    private boolean skip(ZoeEnvironment environment) {

        if (SynapseControl.instance().getSetting().isSkipAuthorization())
            return true;

        if (environment.getServiceType() == ServiceType.PROXY.code() && SynapseControl.instance().getSetting().isSkipProxyAuthorization())
            return true;

        if (environment.getServiceType() == ServiceType.RESTFUL.code() && SynapseControl.instance().getSetting().isSkipRestfulAuthorization())
            return true;

        if (environment.getServiceType() == ServiceType.COMPLEX.code() && SynapseControl.instance().getSetting().isSkipComplexAuthorization())
            return true;
        return false;
    }

    private String getStringForMap(Map<String, Object> map, String key, Object defaultValue) {
        Object value = map.getOrDefault(key, defaultValue);
        if (value instanceof List) {
            if (((List) value).size() == 0)
                return defaultValue.toString();
        }
        return value.toString();
    }

    @Override
    public boolean verify(MessageContext messageContext, ZoeEnvironment environment, Map<String, Object> map) throws MediatorRuntimeException {

        //printMessage("start", new Date());

        boolean result;
        try {
            result = verify0(messageContext, environment, map);
        } catch (MediatorRuntimeException ex) {
            environment.add(WS_INTERNAL_AUTH_STATE_THROWS, ex);
            result = false;
        }

        //printMessage(null, "");

        environment.add(WS_INTERNAL_AUTH_STATE, result);
        return true;
    }

    private boolean verify0(MessageContext messageContext, ZoeEnvironment environment, Map<String, Object> map) throws MediatorRuntimeException {

        if (skip(environment))
            return true;

        final String cryptoToken = getStringForMap(map, TOKEN, EMPTY);

        if (cryptoToken != null && !cryptoToken.trim().isEmpty() && cryptoToken.equalsIgnoreCase(SuperSynapseUser.Instance.getAuthorizationCode())) {
            environment.add(ZoeEnvironment.SERVICE_SERCT, SuperSynapseUser.Instance.getAuthorizationCode());
            environment.add(ZoeEnvironment.SERVICE_OBJECT_ACCESS_BASE_ID, SuperSynapseUser.Instance.getAuthorizationCode());
            environment.add(ZOE_ENVIRONMENT_CRYPTO_ENABLED, false);
            environment.add(ZOE_ENVIRONMENT_CRYPTO_PUBLICKEY, null);
            environment.add(ZOE_ENVIRONMENT_CRYPTO_PRIVATEKEY, null);
            environment.add(ZOE_ENVIRONMENT_CRYPTO_KEY, null);
            return true;
        }

        final boolean confTokenCryptoEnable = Boolean.parseBoolean(getStringForMap(map, TOKEN_CONF_CRYPTO, Boolean.TRUE));

        String token = cryptoToken.replace(" ", "+");

        //printMessage("cryptoToken", token);

        if (token == null || token.trim().isEmpty())
            throwException("缺失认证凭据[token]");

        LOG.debug("cryptoToken: " + cryptoToken);

        if (confTokenCryptoEnable) {
            final byte[] privateKey = Base64.decode(environment.getCryptoPrivateKey());
            try {
                token = CryptoUtil.RevertDataWithRSA(cryptoToken, privateKey);
            } catch (Exception e) {
                throwException("认证凭据[token]解密失败");
            }
        }

        //printMessage("token", token);

        if (token == null || token.trim().isEmpty())
            throwException("认证凭据解密后数据异常[token:null/empty]");

        LOG.debug("encode token: " + token);

        ZoeAuthToken authorzToken = JSON.parseObject(token, ZoeAuthToken.class);
        if (authorzToken == null)
            return false;

        return verifyToken(environment, authorzToken);
    }

    private void checkFail(long currentTime, ZoeAuthToken authToken, Integer failTime) throws MediatorRuntimeException {
        if (failTime == null)
            return;
        if ((currentTime - authToken.getCreatetime()) > failTime)
            throwException("您使用的认证凭据[token]已经过期");
    }

    private VerifyAuthModel loadVerifyAuthModel(ZoeEnvironment environment, String serviceSerct, Function<VerifyAuthModel> missProvider) throws MediatorRuntimeException {

        final SynapseAuthorizationObject authorizationObject = SynapseControl.instance().getAuthorizationObject(environment, serviceSerct);

        if (authorizationObject == null) {
            return missProvider.apply();
        }
        if (authorizationObject.getState() == SynapseAuthorizationObject.STATE_NO_AUTHORIZATION)
            return null;


        return new VerifyAuthModel()
                .setPrivateKey(authorizationObject.getPrivateKey())
                .setSuccess(true)
                .setAccessBaseId(authorizationObject.getAccessBaseId())
                .setFailTime(authorizationObject.getFailTime());
    }

    private boolean verifyToken(ZoeEnvironment environment, ZoeAuthToken authorzToken) throws MediatorRuntimeException {

        final String serct = authorzToken.getLicence();

        if (serct == null)
            throwException("认证凭据中未设置授权码信息");

        final long currentTime = new Date().getTime();

        checkFail(currentTime, authorzToken, SynapseControl.instance().getSetting().getAuthorizationFailTime());

        VerifyAuthModel authorization = loadVerifyAuthModel(environment, serct, () -> {
            VerifyAuthModel authorization0 = null;
            try {
                authorization0 = SspControlService.instance().verifyServiceSerct(environment.getServiceId(), environment.getServiceType(), serct);
            } catch (Exception e) {
                throwException(e.getMessage());
            }
            return authorization0;
        });

        // 验证失败
        if (authorization == null) {
            return false;
        }

        if (!authorization.isSuccess())
            throwException(authorization.getMessage());

        final String accessBaseId = authorization.getAccessBaseId();

        // 设置 客户端密钥、接收主键到环境数据中
        environment.add(ZoeEnvironment.SERVICE_SERCT, serct);
        environment.add(ZoeEnvironment.SERVICE_OBJECT_ACCESS_BASE_ID, accessBaseId);

        if (!authorization.isAuthorizationState())
            return false;

        checkFail(currentTime, authorzToken, authorization.getFailTime());

        if (authorization.isEnabledCrypto() && (authorzToken.getDecryptkey() == null || authorzToken.getDecryptkey().trim().isEmpty()))
            throw MediatorRuntimeException.create(PHIPMediatorErrorCode.CRYPTO_NO_OPENED);

        if (!authorization.isEnabledCrypto() && (authorzToken.getDecryptkey() != null && !authorzToken.getDecryptkey().trim().isEmpty()))
            throw MediatorRuntimeException.create(PHIPMediatorErrorCode.CRYPTO_OPEN_NO_CRYPTO_PARAMETER);

        final byte[] privateKey = authorization.getPrivateKey() == null || authorization.getPrivateKey().trim().isEmpty() ? null : Base64.decode(authorization.getPrivateKey());
        final byte[] publicKey = authorization.getPublicKey() == null || authorization.getPublicKey().trim().isEmpty() ? null : Base64.decode(authorization.getPublicKey());

        //if (authorzToken.getDecryptkey() != null && webServicePrivateKey == null)
        //    throwException("该请求授权码未设置私钥");

        environment.add(ZOE_ENVIRONMENT_CRYPTO_ENABLED, authorization.isEnabledCrypto());
        environment.add(ZOE_ENVIRONMENT_CRYPTO_PUBLICKEY, publicKey);
        environment.add(ZOE_ENVIRONMENT_CRYPTO_PRIVATEKEY, privateKey);
        environment.add(ZOE_ENVIRONMENT_CRYPTO_KEY, authorzToken.getDecryptkey());

        return true;
    }

    @FunctionalInterface
    private interface Function<R> {
        R apply() throws MediatorRuntimeException;
    }

    private static class SuperSynapseUser {
        public static final SuperSynapseUser Instance = new SuperSynapseUser();
        /*
        public byte[] getPrivateKey() {
            return privateKey;
        }

        public byte[] getPublicKey() {
            return publicKey;
        }
        */
        private String authorizationCode;

        private SuperSynapseUser() {
            this.authorizationCode = "ZOE.SYNAPSE.SUPER.USER";
            //this.privateKey = Base64.decode("");
            //this.publicKey = Base64.decode("");
        }
        //private byte[] privateKey;
        //private byte[] publicKey;

        public String getAuthorizationCode() {
            return authorizationCode;
        }
    }
}