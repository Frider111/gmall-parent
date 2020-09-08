package com.atguigu.gmall.user.controller;

import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Blue Grass
 * @date 2020/9/4 - 14:45
 */
@RequestMapping("api/user/passport")
@RestController
public class PassportController {

    @Autowired
    UserService userService ;


    /**
     * 需要传递给前端的数据有
     * 登录失败的情况，返回给前端一个 错误信息
     * 登录成功的情况，返回一个封装好的 token nickName name
     * @return
     */
    @PostMapping("login")
    public Result login(@RequestBody UserInfo userInfo){

        Map map = new HashMap();

        Map infoMap = userService.login(userInfo) ;

        UserInfo userInfo1 = (UserInfo) infoMap.get("userInfo") ;
        // 如果数据等于 null 就返回
        if (infoMap==null ||infoMap.isEmpty() )
        {
            return Result.fail().message("账户名与密码不匹配，请重新输入");
        }
        // 返回前端需要的数据
        map.put("token",infoMap.get("token"));
        map.put("name",userInfo1.getName());
        map.put("nickName",userInfo1.getNickName());

        return Result.ok(map);

    }

    @GetMapping("logout")
    public Result logout(HttpServletRequest request){

        String token = request.getHeader("token");

        userService.logout(token);
        return Result.ok();

    }

}
