package com.qingcheng.service.goods;

import java.util.Map;

/**
 * @创建人 cxp
 * @创建时间 2019-11-6
 * @描述
 */
public interface SkuSearchService {


    /**
     * 关键字搜索
     */
    public Map search(Map<String,String> searchMap);




}
