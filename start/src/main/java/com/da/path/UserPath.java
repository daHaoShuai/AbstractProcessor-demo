package com.da.path;

import com.da.orm.core.BaseMapper;
import com.da.po.User;
import com.da.web.annotations.Inject;
import com.da.web.annotations.Path;
import com.da.web.core.Context;
import com.da.web.function.Handler;
import com.da.web.util.Utils;

/**
 * @Author Da
 * @Description:
 * @Date: 2022-07-22
 * @Time: 17:08
 */
@Path("/")
public class UserPath implements Handler {

    @Inject("UserMapper")
    BaseMapper<User> userMapper;

    @Override
    public void callback(Context ctx) {
        ctx.send(Utils.parseListToJsonString(userMapper.list()));
    }
}
