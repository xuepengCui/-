package com.qingcheng.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.pojo.system.LoginLog;
import com.qingcheng.service.system.LoginLogService;
import com.qingcheng.util.WebUtil;
import org.apache.zookeeper.Login;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

/**
 * @创建人 cxp
 * @创建时间 2019-10-28
 * @描述   登录日志
 */

public class AuthenticationSuccessHandlerImpl implements AuthenticationSuccessHandler{

    @Reference
    private LoginLogService loginLogService;
    @Override
    public void onAuthenticationSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException, ServletException {
        System.out.println("登陆成功了，我要记录日志了111111111");

        httpServletRequest.getRequestDispatcher("/main.html").forward(httpServletRequest,httpServletResponse);

        String loginName = authentication.getName();//当前登录用户姓名
        String ip = httpServletRequest.getRemoteAddr();//当前登录用户ip
        String agent = httpServletRequest.getHeader("user-agent");

        LoginLog loginLog = new LoginLog();

        loginLog.setIp(ip);

        loginLog.setLoginName(loginName);

        loginLog.setLoginTime(new Date());

        loginLog.setLocation(WebUtil.getCityByIP(ip));

        loginLog.setBrowserName(WebUtil.getBrowserName(agent));

        loginLogService.add(loginLog);

    }
}
