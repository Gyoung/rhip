package cn.com.zoesoft.rhip.demo.service.impl;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Created by zengjiyang on 2018/3/23.
 */
@MapperScan("cn.com.zoesoft.rhip.demo.dao")
@SpringBootApplication
public class BootStarter {
}
