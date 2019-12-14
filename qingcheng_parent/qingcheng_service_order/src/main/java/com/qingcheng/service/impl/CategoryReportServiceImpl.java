package com.qingcheng.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.qingcheng.dao.CategoryReportMapper;
import com.qingcheng.pojo.order.CategoryReport;
import com.qingcheng.service.order.CategoryReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * @创建人 cxp
 * @创建时间 2019-10-25
 * @描述
 */
@Service(interfaceClass = CategoryReportService.class)
public class CategoryReportServiceImpl implements CategoryReportService {


    @Autowired
    private CategoryReportMapper categoryReportMapper;

    @Override
    public List<CategoryReport> categoryReport(LocalDate date) {

        return categoryReportMapper.categoryReport(date);
    }

    @Override
    @Transactional
    public void createDate() {
        LocalDate localDate = LocalDate.now().minusDays(1);

        List<CategoryReport> list = categoryReportMapper.categoryReport(localDate);

        for(CategoryReport categoryReport : list){

            categoryReportMapper.insert(categoryReport);
        }
    }

    @Override
    public List<Map> category1Count(String date1, String date2) {
        return categoryReportMapper.category1Count(date1, date2);
    }
}
