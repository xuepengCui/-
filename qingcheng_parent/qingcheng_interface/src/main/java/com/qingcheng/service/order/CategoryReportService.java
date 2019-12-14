package com.qingcheng.service.order;

import com.qingcheng.pojo.order.CategoryReport;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * @创建人 cxp
 * @创建时间 2019-10-25
 * @描述
 */

public interface CategoryReportService {

    /**
     * 商品类目按日期统计(订单表关联查询)
     * @param date
     * @return
     */
    public List<CategoryReport> categoryReport(LocalDate date);

    public void createDate();



    /**
     * 一级类目统计
     * @param date1
     * @param date2
     * @return
     */
    public List<Map> category1Count(String date1 , String date2 );

}
