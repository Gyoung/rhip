package cn.com.zoesoft.rhip.esb.authorization.provider;

import org.apache.synapse.exception.MediatorErrorCode;

/**
 * Created by qiuyungen on 2016/9/21.
 */
public final class PHIPMediatorErrorCode {
    public static final MediatorErrorCode CRYPTO_NO_OPENED = MediatorErrorCode.createError(30007, "该服务已开启加密选项, 接口参数信息未进行加密设置!");
    public static final MediatorErrorCode CRYPTO_OPEN_NO_CRYPTO_PARAMETER = MediatorErrorCode.createError(30008, "该服务未开启加密选项, 接口参数信息无需设置加密!");
}
