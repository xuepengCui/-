package com.qingcheng.service.impl;

import com.qingcheng.service.goods.CategoryService;
import com.qingcheng.service.goods.SkuService;
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
    private CategoryService categoryService;

    @Autowired
    private SkuService skuService;

    public void afterPropertiesSet() throws Exception {

        System.out.println("---------缓存预热565656565656aaaaaa--------");
        categoryService.saveCateGoryTreeToRedis();//加载商品分类导航缓存
        skuService.saveAllPriceToRedis();//加载全部价格到缓存中
    }
}
