package com.qingcheng.service.user;
import com.qingcheng.entity.PageResult;
import com.qingcheng.pojo.user.User;

import javax.xml.transform.Result;
import java.util.*;

/**
 * user业务逻辑层
 */
public interface UserService {


    public List<User> findAll();


    public PageResult<User> findPage(int page, int size);


    public List<User> findList(Map<String,Object> searchMap);


    public PageResult<User> findPage(Map<String,Object> searchMap,int page, int size);


    public User findById(String username);

    public void add(User user);


    public void update(User user);


    public void delete(String username);


    /**
     * 向注册的用户发送发送验证码
     * @param phone  注册的用户的手机号
     */
    public void sendSms(String phone);


    /**
     * 用户注册
     * @param user  用户对象信息
     * @param smsCode  短信验证码
     */
    public void add(User user,String smsCode );

}
