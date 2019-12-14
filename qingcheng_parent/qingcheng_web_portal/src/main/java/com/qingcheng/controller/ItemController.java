package com.qingcheng.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.qingcheng.pojo.goods.Goods;
import com.qingcheng.pojo.goods.Sku;
import com.qingcheng.pojo.goods.Spu;
import com.qingcheng.service.goods.CategoryService;
import com.qingcheng.service.goods.SpuService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @创建人 cxp
 * @创建时间 2019-10-29
 * @描述
 */

@RestController
@RequestMapping("/item")
public class ItemController {


    @Value("${pagePath}")
    private String  pagePath;

    @Reference
    private SpuService spuService;

    @Autowired
    private TemplateEngine templateEngine;

    @Reference
    private CategoryService categoryService;

    /**
     * 生成商品详情页
     * @param spuId
     */
    @GetMapping("/createPage")
    public void createPage(String spuId){

        //查询商品信息
        Goods goods = spuService.findGoodsById(spuId);
        //获得spu信息
        Spu spu = goods.getSpu();
        //得到sku列表
        List<Sku> skuList = goods.getSkuList();

        //查询商品分类
        List<String> categoryList = new ArrayList<>();
        //一级分类
        categoryList.add(categoryService.findById(spu.getCategory1Id()).getName());
        //二级分类
        categoryList.add(categoryService.findById(spu.getCategory2Id()).getName());
        //三级分类
        categoryList.add(categoryService.findById(spu.getCategory3Id()).getName());


        //sku地址列表
        Map<String,String> urlMap=new HashMap<>();
        for(Sku sku:skuList){
            if("1".equals(sku.getStatus())){
                String specJson = JSON.toJSONString( JSON.parseObject(sku.getSpec()), SerializerFeature.MapSortField);
                urlMap.put(specJson,sku.getId()+".html");
            }
        }


        //批量生成spu页面

        for(Sku sku: skuList){

            //1.创建上下文
            Context context = new Context();
            //1.2创建数据模型
            Map<String,Object> dataModel = new HashMap<>();
            dataModel.put("spu", spu);
            dataModel.put("sku", sku);
            dataModel.put("categoryList",categoryList);
            //sku图片列表
            dataModel.put("skuImages", sku.getImages().split(","));
            //spu图片列表
            dataModel.put("spuImages", spu.getImages().split(","));
            //spu的参数列表
            Map paraItems = JSON.parseObject(spu.getParaItems());
            dataModel.put("paraItems", paraItems);
            Map specItems = JSON.parseObject(sku.getSpec());
            //sku规格列表
            dataModel.put("specItems", specItems);
            //{"颜色":["红色","白色","紫色","蓝色","黑色"],"版本":["6GB+128GB","4GB+128GB","6GB+64GB"]}
            //{"颜色":[option:"红色"checked:true,option:"白色",option:"紫色","蓝色","黑色"]}
            //规格和规格选项
            Map<String,List> specMap = (Map)JSON.parseObject(spu.getSpecItems());


            //循环规格名称
            for(String key:specMap.keySet()){
                List<String> list = specMap.get(key);

                List<Map> mapList = new ArrayList<>();

                //循环规格选项值
                for(String value:list){
                    Map map = new HashMap();
                    map.put("option", value);

                    //判断此规格组合是否是当前SKU的，标记选中状态
                    if(specItems.get(key).equals(value)){

                        map.put("checked", "true");
                    }else{
                        map.put("checked", "false");
                    }

                    //当前的Sku列表
                    Map spec = (Map)JSON.parseObject(sku.getSpec());
                    //将选中的规格列表加入到spec中
                    System.out.println(key+"------------"+value);
                    spec.put(key, value);
                    String specJson = JSON.toJSONString(spec,SerializerFeature.MapSortField);
                    //System.out.println(specJson+"asdasdasdsad");

                    map.put("url", urlMap.get(specJson));
                    //System.out.println(map.values());
                    mapList.add(map);
                }
                    //用新集合覆盖原集合
                    specMap.put(key, mapList);
                    //规格面板
                    dataModel.put("specMap",specMap );

            }


            context.setVariables(dataModel);
            //2.准备文件
            File dir = new File(pagePath);

            if(!dir.exists()){
                dir.mkdirs();
            }

            File dest = new File(dir,sku.getId()+".html");
            //3.生成页面
            try {
                PrintWriter printWriter = new PrintWriter(dest,"UTF-8");

                templateEngine.process("item",context,printWriter);
                System.out.println("生成页面"+sku.getId()+".html");
            } catch (FileNotFoundException e) {

                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

    }
}
