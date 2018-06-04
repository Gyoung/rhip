package cn.com.zoesoft.rhip.demo.service;


import cn.com.zoesoft.rhip.demo.model.User;

/**
 * Created by zengjiyang on 2018/3/8.
 */
public interface UserService {
    int addUser(User user);

    User selectByPrimaryKey(String id);
}
