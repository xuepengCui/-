package com.qingcheng.service.goods;

import com.qingcheng.pojo.goods.Audit;

import javax.swing.tree.RowMapper;
import java.util.List;

/**
 * @创建人 cxp
 * @创建时间 2019-10-22
 * @描述
 */
public interface AuditService {

    public List<Audit> findAll();

    public void add(Audit audit);

    public void delete(String spuId);

    public List<Audit> findById(String spuId);


}
