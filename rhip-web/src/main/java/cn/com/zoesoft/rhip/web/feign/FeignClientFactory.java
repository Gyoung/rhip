package cn.com.zoesoft.rhip.web.feign;

import cn.com.zoesoft.framework.config.PropertyPlaceholder;
import feign.Feign;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;

/**
 * Created by zengjiyang on 2018/3/28.
 */
public class FeignClientFactory {

    public static final  String REMOTE_URL= PropertyPlaceholder.getProperty("feign.url");

    public static <T> T getFeignClient(Class<T> cls,String url){
        if(url==null||url.isEmpty()){
            url=REMOTE_URL;
        }
        return Feign.builder()
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .target(cls, url);

    }
}
