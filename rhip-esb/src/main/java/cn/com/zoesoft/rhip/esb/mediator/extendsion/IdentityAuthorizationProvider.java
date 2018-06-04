package cn.com.zoesoft.rhip.esb.mediator.extendsion;

import org.apache.synapse.MessageContext;

import java.util.Map;

/**
 * Created by qiuyungen on 2016/8/10.
 */
public class IdentityAuthorizationProvider /*implements AuthorizationProvider*/ {

    //TODO 验证身份
    public IdentityAuthorizationProvider() {

    }

    public boolean verify(MessageContext synCtx, Map<String, Object> variables) {


        return false;
    }
}
