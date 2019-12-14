package com.qingcheng.service.impl;

import com.qingcheng.service.business.AdService;
import com.qingcheng.service.goods.CategoryService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * @创建人 cxp
 * @创建时间 2019-10-31
 * @描述
 */


@Component
public class Init implements InitializingBean {

    @Autowired
    private AdService adService;

    public void afterPropertiesSet() throws Exception {

        System.out.println("---------缓存预热565656565656aaaaaa--------");
        adService.saveAllAdToRedis();
    }
}
