package com.qingcheng.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.qingcheng.pojo.goods.Category;
import com.qingcheng.pojo.goods.Sku;
import com.qingcheng.pojo.order.OrderItem;
import com.qingcheng.service.goods.CategoryService;
import com.qingcheng.service.goods.SkuService;
import com.qingcheng.service.order.CartService;
import com.qingcheng.service.order.PreferentialService;
import com.qingcheng.util.CacheKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {


	@Autowired
	private RedisTemplate redisTemplate;

	@Reference
	private CategoryService categoryService;

	@Reference
	private SkuService skuService;

	@Autowired
	private PreferentialService preferentialService;


	/**
	 * 查看购物车
	 * @param username
	 * @return
	 */
	@Override
	public List<Map<String, Object>> findCartList(String username) {

		System.out.println("从redis中获取购物车"+username);
		List<Map<String, Object>> cartList = (List<Map<String, Object>>)redisTemplate.boundHashOps(CacheKey.CART_LIST).get(username);
		//如果购物车为空，返回一个空集合
		if(cartList==null||"".equals(cartList)){
			return new ArrayList<>();
		}
		return cartList;
	}

	/**
	 * 添加购物项到购物车
	 * @param username 用户名
	 * @param skuId    商品id
	 * @param num      数量
	 */
	@Override
	public void addItem(String username, String skuId, Integer num) {
		//实现思路： 遍历购物车，如果购物车中存在该商品则累加数量，如果不存在则添加购物车项//获取购物车
		//获取当前购物车
		List<Map<String, Object>> cartList = findCartList(username);

		boolean flag = false;//是否在购物车中存在
		for(Map map:cartList){

			OrderItem orderItem = (OrderItem) map.get("item");

			if(orderItem.getSkuId().equals(skuId)){//购物车中存在该商品

				//如果数量小于0,,移除该购物项
				if(orderItem.getNum()<=0){
					cartList.remove(map);
					flag=true;
					break;
				}
				//单个商品的重量
				int weight = orderItem.getWeight()/orderItem.getNum();
				//数量的更改，加上要添加的数量
				orderItem.setNum(orderItem.getNum()+num);
				//金额的变更
				orderItem.setMoney(orderItem.getNum()*orderItem.getPrice());
				//重量的变更
				orderItem.setWeight(weight*orderItem.getNum());

				//如果数量小于等于0,,移除该购物项
				if(orderItem.getNum()<=0){
					cartList.remove(map);
				}

				flag=true;
				break;
			}

		}
		//如果购物车中没有该商品，则添加该购物车
		if(flag==false){

			OrderItem orderItem = new OrderItem();//购物项

			//为orderItem设置属性，根据skuId查询其他属性
			Sku sku = skuService.findById(skuId);

			//判断商品是否存在
			if(sku==null){
				throw new RuntimeException("商品不存在");
			}
			//判断商品状态是否合法
			if (!"1".equals(sku.getStatus())) {
				throw new RuntimeException("商品状态不合法");
			}
			//判断要添加的数量是否合法
			if(num<=0){
				throw new RuntimeException("商品数量不合法");
			}

			orderItem.setSkuId(skuId);//设置skuId
			orderItem.setSpuId(sku.getSpuId());//设置spuId
			orderItem.setImage(sku.getImage());//设置图片
			orderItem.setNum(num);//设置数量
			orderItem.setPrice(sku.getPrice());//设置单价
			orderItem.setName(sku.getName());//设置商品名称
			orderItem.setMoney(sku.getPrice()*num);//金额的计算
			if(sku.getWeight()==null){
				sku.setWeight(0);
			}
			orderItem.setWeight(sku.getWeight()*num);//重量的计算


			/*
			   为提升效率，将categoryId1\2\3存入redis中
			 */

			orderItem.setCategoryId3(sku.getCategoryId());

			Category category2 = (Category) redisTemplate.boundHashOps(CacheKey.CATEGORY).get(sku.getCategoryId());

			if(category2==null){
				//根据三级分类id查找二级分类
				category2 = categoryService.findById(sku.getCategoryId());
			    redisTemplate.boundHashOps(CacheKey.CATEGORY).put(sku.getCategoryId(),category2);
			}
			orderItem.setCategoryId2(category2.getParentId());

			Category category1 = (Category) redisTemplate.boundHashOps(CacheKey.CATEGORY).get(category2.getParentId());

			if(category1==null){
			    //根据二级id查询一级分类
			    category1 = categoryService.findById(category2.getParentId());
				redisTemplate.boundHashOps(CacheKey.CATEGORY).put(category2.getParentId(),category1);

			}
			orderItem.setCategoryId1(category1.getParentId());


			Map map = new HashMap();
			map.put("item",orderItem);
			map.put("checked",true);//默认选中状态
			cartList.add(map);
		}

		redisTemplate.boundHashOps(CacheKey.CART_LIST).put(username,cartList);
	}


	/**
	 * 更新购物项选中状态
	 * @param username 当前登录用户
	 * @param skuId   商品id
	 * @param checked  商品选中状态
	 */
	@Override
	public Boolean updateChecked(String username,String skuId, boolean checked) {

		//从redis中获取当前购物车
		List<Map<String,Object>> cartList = (List<Map<String, Object>>) redisTemplate.boundHashOps(CacheKey.CART_LIST).get(username);
		//判断缓存中是否有已购物品
		boolean isOK= false;
		for(Map map:cartList){
			OrderItem orderItem = (OrderItem) map.get("item");
			//如果购物车里已经包含该购物项
			if(skuId.equals(orderItem.getSkuId())){
				//更改checked状态
				map.put("checked",checked);
				isOK=true;  //执行成功
				break;
			}
		}

		if(isOK){
			//将更新后的状态重新存入redis中
			redisTemplate.boundHashOps(CacheKey.CART_LIST).put(username,cartList);
		}
		return isOK;
	}


	/**
	 * 删除购物项
	 * @param username  登录用户名
	 */
	@Override
	public void deleteCheckedCart(String username) {
		//获取未选中的购物车
		List<Map<String,Object>> cartList = findCartList(username).stream()
				.filter(cart -> (boolean)cart.get("checked")==false)
				.collect(Collectors.toList());
		//将值包含未选中的购物项的购物车放入缓存中，顶替掉原来的购物车，达到删除选中购物车中购物项的目的
		redisTemplate.boundHashOps(CacheKey.CART_LIST).put(username,cartList);
	}


	/**
	 * 计算当前购物车中的购物项的优惠金额
	 * @param username
	 * @return
	 */
	@Override
	public int preferential(String username) {

		//获取选中的购物项的购物车  List<OrderItem >
		List<OrderItem> orderItemList = (List<OrderItem>)findCartList(username).stream()
				.filter(cart -> (boolean)cart.get("checked")==true)
				.map(cart -> (OrderItem)cart.get("item"))
				.collect(Collectors.toList());

		//按分类聚合统计每个分类的金额 group by
		//分类     金额
		//1        120
		//2        200
		Map<Integer, IntSummaryStatistics> cartMap = orderItemList.stream()
				.collect(Collectors.groupingBy(OrderItem::getCategoryId3, Collectors.summarizingInt(OrderItem::getMoney)));

		//循环结果，统计每个分类的优惠金额，循环相加

		int allPreMoney=0;//累加优惠金额
		for(Integer categorId : cartMap.keySet()){
			//获取品牌的消费金额
			int money = (int)cartMap.get(categorId).getSum();
			//获取优惠金额
			int preMoney= preferentialService.findPreMoneyByCategoryId(categorId, money);
			System.out.println("分类"+categorId+"消费金额"+money+"优惠金额"+preMoney);
			//累加优惠金额
			allPreMoney +=preMoney;
		}
		return allPreMoney;
	}

	/**
	 *  获取最新的购物车列表
	 * @param username  当前登录用户名
	 * @return
	 */
	@Override
	public List<Map<String, Object>> findNewOrderItemList(String username) {

		//获取选中的购物车
		List<Map<String, Object>> cartList = findCartList(username);
		//循环购物车列表，重新读取每个商品的最新价格
		for( Map<String,Object> cart:cartList ){
			OrderItem orderItem = (OrderItem)cart.get("item");
			//查询最新的价格
			Sku sku = skuService.findById(orderItem.getSkuId());
			//更新购物车中的价格
			orderItem.setPrice(sku.getPrice());
			//更新金额
			orderItem.setMoney(orderItem.getNum()*sku.getPrice());

		}
		//将更新后的购物车存入redis中
		redisTemplate.boundHashOps(CacheKey.CART_LIST).put(username,cartList);
		return cartList;
	}


}
