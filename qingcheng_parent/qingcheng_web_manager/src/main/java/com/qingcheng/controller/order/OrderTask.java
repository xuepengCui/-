package com.qingcheng.controller.order;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.service.order.CategoryReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @创建人 cxp
 * @创建时间 2019-10-25
 * @描述
 */

@Component
public class OrderTask {


    @Reference
    private CategoryReportService categoryReportService;

    @Scheduled(cron = "0 0 1 * * ?")
    public void createCategoryReportDate(){

        System.out.println("createCategoryReportDate");
        categoryReportService.createDate();
    }
}
