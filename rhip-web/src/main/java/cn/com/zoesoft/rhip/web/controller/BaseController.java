package cn.com.zoesoft.rhip.web.controller;

import cn.com.zoesoft.framework.config.PropertyPlaceholder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;

/**
 * Created by zengjiyang on 2018/3/28.
 */
public class BaseController {

    boolean isEureka="true".equals(PropertyPlaceholder.getProperty("feign.url"));

    @Autowired
    private DiscoveryClient discoveryClient;

    // TODO: 2018/6/4 多个服务有多个地址怎么办
    //getInstances("SERVICE-HI").get(0)  如果有多个服务，可以用算法随机调用
    //getUrl每个服务写一个?getUserServiceUrl,getLoginServiceUrl->存到zk,eureka不也是注册中心吗

    public String getUrl(){
        if(isEureka&&discoveryClient!=null){
            return discoveryClient.getInstances("SERVICE-HI").get(0).getUri().toString();
        }else{
            return  PropertyPlaceholder.getProperty("feign.url");
        }
    }
}
