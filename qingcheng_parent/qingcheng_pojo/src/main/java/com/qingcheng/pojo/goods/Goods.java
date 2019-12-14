package com.qingcheng.pojo.goods;

import java.io.Serializable;
import java.util.List;

/**
 * @创建人 cxp
 * @创建时间 2019-10-22
 * @描述   sku 和spu 的组合类
 */
public class Goods implements Serializable {

    private Spu spu;

    private List<Sku> skuList;


    public Spu getSpu() {
        return spu;
    }

    public void setSpu(Spu spu) {
        this.spu = spu;
    }

    public List<Sku> getSkuList() {
        return skuList;
    }

    public void setSkuList(List<Sku> skuList) {
        this.skuList = skuList;
    }

    public Goods(Spu spu, List<Sku> skuList) {
        this.spu = spu;
        this.skuList = skuList;
    }
}
