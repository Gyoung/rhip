package cn.com.zoesoft.rhip.demo.service.impl;

import cn.com.zoesoft.rhip.demo.model.User;
import cn.com.zoesoft.rhip.demo.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

/**
 * Created by zengjiyang on 2018/3/23.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = BootStarter.class)

public class UserServiceImplTest {

    @Autowired
    private UserService userService;

    @Test
    public void testAddUser() throws Exception {

    }

    @Test
    public void testSelectByPrimaryKey() throws Exception {
        User user = userService.selectByPrimaryKey("1");
        System.out.println(user.getUsername());
    }
}