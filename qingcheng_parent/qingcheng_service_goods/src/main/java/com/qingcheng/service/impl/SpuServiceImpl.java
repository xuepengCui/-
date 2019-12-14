package com.qingcheng.service.impl;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.qingcheng.dao.*;
import com.qingcheng.entity.PageResult;
import com.qingcheng.pojo.goods.*;
import com.qingcheng.service.goods.SkuService;
import com.qingcheng.service.goods.SpuService;
import com.qingcheng.util.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.*;

@Service(interfaceClass = SpuService.class)
public class SpuServiceImpl implements SpuService {

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private IdWorker idWorker;
    
    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private CategoryBrandMapper categoryBrandMapper;

    @Autowired
    private AuditMapper auditMapper;

    @Autowired
    private SkuService skuService;

    /**
     * 还原商品
     * @param id
     */
    public void recoverySpu(String id) {
        Spu spu = new Spu();
        spu.setId(id);
        spu.setIsDelete("0");
        spuMapper.updateByPrimaryKey(spu);
    }

    /**
     * 物理删除商品
     * @param id
     */
    public void deleteSpu(String id) {
        Spu spu = new Spu();
        spu.setId(id);
        spu.setIsDelete("1");
        spuMapper.updateByPrimaryKey(spu);

        Map map = new HashMap();
        map.put("spuId", id);
        List<Sku> skuList = skuService.findList(map);
        for(Sku sku:skuList){

            //删除缓存中的价格
            skuService.deletePriceFromRedis(sku.getId());
        }

    }

    /**
     * 批量下架
     * @param ids
     * @return
     */
    public int pullMany(String[] ids) {

        //修改状态
        Spu spu = new Spu();
        spu.setIsMarketable("0");
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("idMarketable", "1");//成功上架的
        criteria.andEqualTo("isDelete", "0");//未删除的商品
        int count = spuMapper.updateByExampleSelective(spu, example);
        return count;

        //商品日志信息记录
    }

    /**
     * 批量上架
     * @param ids
     * @return
     */
    public int putMany(String[] ids) {

        //修改状态
        Spu spu = new Spu();
        spu.setIsMarketable("1");//上架商品
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id", Arrays.asList(ids));
        criteria.andEqualTo("isMarketable", "0");//未上架的
        criteria.andEqualTo("status", "1");//审核通过的
        criteria.andEqualTo("isDelete", "0");//未删除的商品
        int count = spuMapper.updateByExample(spu, example); //符合上架条件的数量

        return count;

        //添加商品日志
    }

    /**
     * 上架商品
     * @param id
     */

    public void put(String id) {
        Spu spu = spuMapper.selectByPrimaryKey(id);


        if(!"1".equals(spu.getStatus())){
            throw new RuntimeException("侧商品尚未通过审核，请审核后重试");
        }
            spu.setIsMarketable("1");
            spuMapper.updateByPrimaryKeySelective(spu);

            //2.记录商品日志


    }

    /**
     * 下架商品
     * @param id
     */
    public void pull(String id) {

        Spu spu = new Spu();
        spu.setId(id);

        spu.setIsMarketable("0");//下架商品

        spuMapper.updateByPrimaryKeySelective(spu);

        //2.记录商品日志

    }

    /**
     *  商品审核
     * @param id
     * @param status
     * @param message
     */
    public void audit(String id, String status, String message) {

        //修改状态，审核状态和上架状态
        Spu spu = new Spu();
        spu.setId(id);
        spu.setStatus(status);
        //如果状态为1，上架该商品
        if("1".equals(status)){//审核通过
            //自动上架
            spu.setStatus(status);
        }
        spuMapper.updateByPrimaryKeySelective(spu);


        //2.记录商品审核日志
        Audit audit = new Audit();
        audit.setDetails(message);
        audit.setResult(status);
        audit.setSpuId(id);
        auditMapper.insert(audit);

        //3.记录商品日志   待实现


    }

    /**
     * 根据id查询Goods
     * @param id
     * @return
     */
    public Goods findGoodsById(String id) {

        //查询spu
        Spu spu = spuMapper.selectByPrimaryKey(id);

        //查询sku列表
        Example example = new Example(Sku.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("spuId",id);
        List<Sku> skuList = skuMapper.selectByExample(example);

        //将查询结果返回为Goods对象
        Goods goods = new Goods(spu,skuList);

        return goods;
    }

    /**
     * 保存商品组合实体类
     * @param goods
     */
    @Transactional
    public void saveGoods(Goods goods) {
        //保存一个spu信息
        Spu spu = goods.getSpu();

        //新增spu
        if(spu.getId() == null || "".equals(spu.getId())){
            //用雪花算法生成分布式id
            spu.setId(idWorker.nextId()+"");
            spuMapper.insert(spu);
        }else{
            //修改spu
            //修改spu需要先删除spu绑定的sku列表
            Example example = new Example(Sku.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("spuId", spu.getId());
            skuMapper.deleteByExample(example);
            //执行spu的修改
            spuMapper.updateByPrimaryKeySelective(spu);
        }




        //保存sku列表的信息
        List<Sku> skuList = goods.getSkuList();

        Date date = new Date();
        //分类对象
        Category category = categoryMapper.selectByPrimaryKey(spu.getCategory3Id());
        for(Sku sku : skuList){

            if(sku.getSpuId()==null || "".equals(sku.getSpuId())){
                //新增
                sku.setCreateTime(date);//创建日期
                sku.setId(idWorker.nextId()+"");

            }

            //未启用规格的sku
            if(sku.getSpec()==null || "".equals(sku.getSpec())){
                sku.setSpec("{}");
            }
            //sku名称   spu名称加规格值列表
            String name = spu.getName();
            String spec = sku.getSpec();
            Map<String,String> map = JSON.parseObject(spec, Map.class);//将规格转化为map集合

            for(String value : map.values()){
                name+= " "+ value;
            }
            sku.setName(name);//设置，名称

            sku.setUpdateTime(date);//修改日期
            sku.setCategoryId(spu.getCategory3Id());//分类id
            sku.setCategoryName(category.getName());//分类名称
            sku.setCommentNum(0); //评论数
            sku.setSaleNum(0);// 销售数量

            sku.setSpuId(spu.getId()); //设置spuId


            skuMapper.insert(sku);
            //重新将价格更新到缓存中
            skuService.savePriceToRedisById(sku.getId(), sku.getPrice());
        }

        //建立分类与品牌的关联
        CategoryBrand categoryBrand = new CategoryBrand(spu.getCategory3Id(),spu.getBrandId());

        //检查分类与品牌是否已经关联，如果已经给关联的话，count>0
        int count = categoryBrandMapper.selectCount(categoryBrand);

        if(count == 0){
            categoryBrandMapper.insert(categoryBrand);
        }

    }

    /**
     * 返回全部记录
     * @return
     */
    public List<Spu> findAll() {
        return spuMapper.selectAll();
    }

    /**
     * 分页查询
     * @param page 页码
     * @param size 每页记录数
     * @return 分页结果
     */
    public PageResult<Spu> findPage(int page, int size) {
        PageHelper.startPage(page,size);
        Page<Spu> spus = (Page<Spu>) spuMapper.selectAll();
        return new PageResult<Spu>(spus.getTotal(),spus.getResult());
    }

    /**
     * 条件查询
     * @param searchMap 查询条件
     * @return
     */
    public List<Spu> findList(Map<String, Object> searchMap) {
        Example example = createExample(searchMap);
        return spuMapper.selectByExample(example);
    }

    /**
     * 分页+条件查询
     * @param searchMap
     * @param page
     * @param size
     * @return
     */
    public PageResult<Spu> findPage(Map<String, Object> searchMap, int page, int size) {
        PageHelper.startPage(page,size);
        Example example = createExample(searchMap);
        Page<Spu> spus = (Page<Spu>) spuMapper.selectByExample(example);
        return new PageResult<Spu>(spus.getTotal(),spus.getResult());
    }

    /**
     * 根据Id查询
     * @param id
     * @return
     */
    public Spu findById(String id) {
        return spuMapper.selectByPrimaryKey(id);
    }

    /**
     * 新增
     * @param spu
     */
    public void add(Spu spu) {
        spuMapper.insert(spu);
    }

    /**
     * 修改
     * @param spu
     */
    public void update(Spu spu) {
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    /**
     *  删除
     * @param id
     */
    public void delete(String id) {

        spuMapper.deleteByPrimaryKey(id);

        Map map = new HashMap();
        map.put("spuId", id);
        List<Sku> skuList = skuService.findList(map);
        for(Sku sku:skuList){

        //删除缓存中的价格
        skuService.deletePriceFromRedis(sku.getId());
        }

    }

    /**
     * 构建查询条件
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap){
        Example example=new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        if(searchMap!=null){
            // 主键
            if(searchMap.get("id")!=null && !"".equals(searchMap.get("id"))){
                criteria.andLike("id","%"+searchMap.get("id")+"%");
            }
            // 货号
            if(searchMap.get("sn")!=null && !"".equals(searchMap.get("sn"))){
                criteria.andLike("sn","%"+searchMap.get("sn")+"%");
            }
            // SPU名
            if(searchMap.get("name")!=null && !"".equals(searchMap.get("name"))){
                criteria.andLike("name","%"+searchMap.get("name")+"%");
            }
            // 副标题
            if(searchMap.get("caption")!=null && !"".equals(searchMap.get("caption"))){
                criteria.andLike("caption","%"+searchMap.get("caption")+"%");
            }
            // 图片
            if(searchMap.get("image")!=null && !"".equals(searchMap.get("image"))){
                criteria.andLike("image","%"+searchMap.get("image")+"%");
            }
            // 图片列表
            if(searchMap.get("images")!=null && !"".equals(searchMap.get("images"))){
                criteria.andLike("images","%"+searchMap.get("images")+"%");
            }
            // 售后服务
            if(searchMap.get("saleService")!=null && !"".equals(searchMap.get("saleService"))){
                criteria.andLike("saleService","%"+searchMap.get("saleService")+"%");
            }
            // 介绍
            if(searchMap.get("introduction")!=null && !"".equals(searchMap.get("introduction"))){
                criteria.andLike("introduction","%"+searchMap.get("introduction")+"%");
            }
            // 规格列表
            if(searchMap.get("specItems")!=null && !"".equals(searchMap.get("specItems"))){
                criteria.andLike("specItems","%"+searchMap.get("specItems")+"%");
            }
            // 参数列表
            if(searchMap.get("paraItems")!=null && !"".equals(searchMap.get("paraItems"))){
                criteria.andLike("paraItems","%"+searchMap.get("paraItems")+"%");
            }
            // 是否上架
            if(searchMap.get("isMarketable")!=null && !"".equals(searchMap.get("isMarketable"))){
                criteria.andLike("isMarketable","%"+searchMap.get("isMarketable")+"%");
            }
            // 是否启用规格
            if(searchMap.get("isEnableSpec")!=null && !"".equals(searchMap.get("isEnableSpec"))){
                criteria.andLike("isEnableSpec","%"+searchMap.get("isEnableSpec")+"%");
            }
            // 是否删除
            if(searchMap.get("isDelete")!=null && !"".equals(searchMap.get("isDelete"))){
                criteria.andLike("isDelete","%"+searchMap.get("isDelete")+"%");
            }
            // 审核状态
            if(searchMap.get("status")!=null && !"".equals(searchMap.get("status"))){
                criteria.andLike("status","%"+searchMap.get("status")+"%");
            }

            // 品牌ID
            if(searchMap.get("brandId")!=null ){
                criteria.andEqualTo("brandId",searchMap.get("brandId"));
            }
            // 一级分类
            if(searchMap.get("category1Id")!=null ){
                criteria.andEqualTo("category1Id",searchMap.get("category1Id"));
            }
            // 二级分类
            if(searchMap.get("category2Id")!=null ){
                criteria.andEqualTo("category2Id",searchMap.get("category2Id"));
            }
            // 三级分类
            if(searchMap.get("category3Id")!=null ){
                criteria.andEqualTo("category3Id",searchMap.get("category3Id"));
            }
            // 模板ID
            if(searchMap.get("templateId")!=null ){
                criteria.andEqualTo("templateId",searchMap.get("templateId"));
            }
            // 运费模板id
            if(searchMap.get("freightId")!=null ){
                criteria.andEqualTo("freightId",searchMap.get("freightId"));
            }
            // 销量
            if(searchMap.get("saleNum")!=null ){
                criteria.andEqualTo("saleNum",searchMap.get("saleNum"));
            }
            // 评论数
            if(searchMap.get("commentNum")!=null ){
                criteria.andEqualTo("commentNum",searchMap.get("commentNum"));
            }

        }
        return example;
    }

}
