package com.qingcheng.controller.order;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.pojo.order.CategoryReport;
import com.qingcheng.service.order.CategoryReportService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * @创建人 cxp
 * @创建时间 2019-10-25
 * @描述
 */
@RestController
@RequestMapping("/categoryReport")
public class CategoryReportController {

    @Reference
    private CategoryReportService categoryReportService;


    /**
     * 昨天的数据统计(商品类目)
     * @return
     */
    @GetMapping("/yesterday")
    public List<CategoryReport> yesterday(){

        LocalDate localDate = LocalDate.now().minusDays(1);//得到昨天的日期
        System.out.println(localDate.toString());
        List<CategoryReport> list = categoryReportService.categoryReport(localDate);
        System.out.println(list);
        return list;

    }


    /**
     * 统计一级类目
     * @param date1
     * @param date2
     * @return
     */
    @GetMapping("/category1Count")
    public List<Map> category1Count(String date1, String date2){
        return categoryReportService.category1Count(date1, date2);
    }
}
