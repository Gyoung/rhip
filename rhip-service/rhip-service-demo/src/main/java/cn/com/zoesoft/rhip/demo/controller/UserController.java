package cn.com.zoesoft.rhip.demo.controller;



import cn.com.zoesoft.framework.entity.ActionResultEntity;
import cn.com.zoesoft.rhip.demo.model.User;
import cn.com.zoesoft.rhip.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by zengjiyang on 2018/3/8.
 */
@Controller
@RequestMapping(value = "/user")
public class UserController {

    @Autowired
    private UserService userService;

//    @ApiOperation(value="新增用户", notes="")
//    @ApiImplicitParam(name = "user", value = "用户详细实体user", required = true, dataType = "User")
    @ResponseBody
    @RequestMapping(value = "/add", produces = {"application/json;charset=UTF-8"},method = RequestMethod.POST)
    public int addUser(User user){
        return userService.addUser(user);
    }

//    @ApiOperation(value="根据ID选择用户", notes="")
//    @ApiImplicitParam(name = "id", value = "用户ID", required = true, dataType = "String",paramType = "path")
    @ResponseBody
    @RequestMapping(value = "/select/{id}",method = RequestMethod.GET)
    public ActionResultEntity selectByPrimaryKey(@PathVariable("id") String id){
        ActionResultEntity entity=new ActionResultEntity();
        User user= userService.selectByPrimaryKey(id);
        entity.setResult(user);
        return entity;
    }


}
