package com.qingcheng.controller;
import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.entity.Result;
import com.qingcheng.pojo.user.User;
import com.qingcheng.service.user.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * @创建人 cxp
 * @创建时间 2019-11-8
 * @描述
 */
@RestController
@RequestMapping("/user")
public class UserController {


    @Reference
    private UserService userService;

    /**
     *  发送短信验证码
     * @param phone
     * @return
     */
    @GetMapping("/sendSms")
    public Result sendSms(String phone){

        userService.sendSms(phone);

        return new Result();
    }


    @PostMapping("/save")
    public Result add(@RequestBody User user, String smsCode){

        //密码加密
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String newPassword = encoder.encode(user.getPassword());
        user.setPassword(newPassword);

        userService.add(user,smsCode );

        return new Result();

    }

}
