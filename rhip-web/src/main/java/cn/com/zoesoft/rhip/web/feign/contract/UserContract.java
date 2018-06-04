package cn.com.zoesoft.rhip.web.feign.contract;

import cn.com.zoesoft.framework.entity.ActionResultEntity;
import feign.Param;
import feign.RequestLine;

/**
 * Created by zengjiyang on 2018/3/28.
 */

public interface UserContract {
    @RequestLine("GET /user/select/{id}")
    ActionResultEntity selectByPrimaryKey(@Param("id") String id);
}
