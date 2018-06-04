package cn.com.zoesoft.rhip;

import org.beetl.core.resource.WebAppResourceLoader;
import org.beetl.ext.spring.BeetlGroupUtilConfiguration;
import org.beetl.ext.spring.BeetlSpringViewResolver;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;

import java.io.IOException;

@SpringBootApplication
@EnableEurekaClient
@MapperScan("cn.com.zoesoft.rhip.demo.dao")
public class RhipStarterApplication {

    public static void main(String[] args) {
        SpringApplication.run(RhipStarterApplication.class, args);
    }

//    @Bean(initMethod = "init", name = "beetlConfig")
    public BeetlGroupUtilConfiguration getBeetlGroupUtilConfiguration() throws IOException {
        BeetlGroupUtilConfiguration beetlGroupUtilConfiguration = new BeetlGroupUtilConfiguration();
        ResourcePatternResolver patternResolver = ResourcePatternUtils.getResourcePatternResolver(new DefaultResourceLoader());
        // WebAppResourceLoader 配置root路径是关键 classpath*
        Resource[] resources = patternResolver.getResources("classpath*:/");
        Resource res = null;
        for (Resource resource : resources) {
            if (resource.getFile().getPath().contains("rhip-web")) {
                res = resource;
                break;
            }
        }
        if (res == null) {
            throw new IOException("未找到rhip-web资源文件!");
        }
        WebAppResourceLoader webAppResourceLoader = new WebAppResourceLoader(res.getFile().getPath());
        beetlGroupUtilConfiguration.setResourceLoader(webAppResourceLoader);

        //读取配置文件信息
        return beetlGroupUtilConfiguration;
    }

//    @Bean(name = "beetlViewResolver")
    public BeetlSpringViewResolver getBeetlSpringViewResolver(@Qualifier("beetlConfig") BeetlGroupUtilConfiguration beetlGroupUtilConfiguration) {
        BeetlSpringViewResolver beetlSpringViewResolver = new BeetlSpringViewResolver();
        beetlSpringViewResolver.setPrefix("templates/");
        beetlSpringViewResolver.setSuffix(".html");
        beetlSpringViewResolver.setContentType("text/html;charset=UTF-8");
        beetlSpringViewResolver.setOrder(0);
        beetlSpringViewResolver.setConfig(beetlGroupUtilConfiguration);
        return beetlSpringViewResolver;
    }
}
