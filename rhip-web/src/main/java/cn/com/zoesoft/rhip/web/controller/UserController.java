package cn.com.zoesoft.rhip.web.controller;

import cn.com.zoesoft.framework.entity.ActionResultEntity;
import cn.com.zoesoft.rhip.web.feign.FeignClientFactory;
import cn.com.zoesoft.rhip.web.feign.contract.UserContract;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by zengjiyang on 2018/3/8.
 */
@Controller("u")
public class UserController extends BaseController {

    @RequestMapping(value = "/home")
    public String index(){
        return "index";
    }


    @RequestMapping(value = "/data")
    @ResponseBody
    public ActionResultEntity data(){
        UserContract userContract= FeignClientFactory.getFeignClient(UserContract.class,getUrl());
        ActionResultEntity entity= userContract.selectByPrimaryKey("1");
        return entity;
    }

}
