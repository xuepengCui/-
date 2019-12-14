package com.qingcheng.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.pojo.business.Ad;
import com.qingcheng.service.business.AdService;
import com.qingcheng.service.goods.CategoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

/**
 * @创建人 cxp
 * @创建时间 2019-10-29
 * @描述
 */

@Controller
public class IndexController {


    @Reference
    private AdService adService;

    @Reference
    private CategoryService categoryService;

    /**
     * 网站首页
     * @param model
     * @return
     */
    @GetMapping("/index")
    public String index(Model model){
        //得到首页广告轮播图
        List<Ad> lbList = adService.findByPosition("web_index_lb");
        model.addAttribute("lbt",lbList);

        //首页分类导航栏
        List<Map> categoryList = categoryService.findCategoryTree();

        model.addAttribute("categoryList",categoryList);


        return "index";
    }


}
