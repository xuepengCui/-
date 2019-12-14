package com.qingcheng.dao;

import com.qingcheng.pojo.goods.Spec;

import org.apache.ibatis.annotations.Select;
import org.springframework.data.repository.query.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

public interface SpecMapper extends Mapper<Spec> {



    @Select("select name,options  " +
             "from tb_spec where template_id IN (select template_id from tb_category where name = #{categoryName} order by seq)")
    public List<Map> findListByCategoryName(@Param("categoryName") String categoryName);
}
