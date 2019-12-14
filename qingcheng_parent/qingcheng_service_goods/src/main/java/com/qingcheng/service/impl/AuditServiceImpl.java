package com.qingcheng.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.qingcheng.dao.AuditMapper;
import com.qingcheng.pojo.goods.Audit;
import com.qingcheng.service.goods.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * @创建人 cxp
 * @创建时间 2019-10-22
 * @描述
 */
@Service
public class AuditServiceImpl implements AuditService {

    @Autowired
    private AuditMapper auditMapper;

    public List<Audit> findAll() {
        return auditMapper.selectAll();
    }

    public void add(Audit audit) {
        auditMapper.insert(audit);
    }

    public void delete(String spuId) {

        Example example = new Example(Audit.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("specId", spuId);
        auditMapper.deleteByExample(example);
    }


    public List<Audit> findById(String spuId) {

        Example example = new Example(Audit.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("specId", spuId);
        List<Audit> auditList = auditMapper.selectByExample(example);
        return auditList;
    }
}
