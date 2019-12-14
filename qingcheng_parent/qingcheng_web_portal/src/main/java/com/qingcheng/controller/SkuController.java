package com.qingcheng.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.service.goods.SkuService;


import org.springframework.web.bind.annotation.*;

/**
 * @创建人 cxp
 * @创建时间 2019-10-31
 * @描述
 */
@RestController
@RequestMapping("/sku")
@CrossOrigin
public class SkuController {

    @Reference
    private SkuService skuService;

    /**
     * 根据skuId得到商品价格
     * @param id
     * @return
     */
    @GetMapping("/price")
    public Integer price(String id){

        return skuService.findPrice(id);
    }
}
