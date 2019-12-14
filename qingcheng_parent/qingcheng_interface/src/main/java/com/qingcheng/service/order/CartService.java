package com.qingcheng.service.order;

import java.util.List;
import java.util.Map;

/**
 * 购物车服务接口
 */
public interface CartService {


	/**
	 * 从redis中获取到购物车
	 * @param username
	 * @return
	 */
	public List<Map<String,Object>> findCartList(String username);


	/**
	 * 添加购物项到购物车
	 * @param username 用户名
	 * @param skuId    商品id
	 * @param num      数量
	 */
	public void addItem(String username,String skuId,Integer num);


	/**
	 * 更新商品选中状态
	 * @param username 当前登录用户
	 * @param skuId   商品id
	 * @param checked  商品选中状态
	 */
	public Boolean updateChecked(String username,String skuId,boolean checked);


	/**
	 * 删除选中的购物项
	 * @param username  登录用户名
	 */
	public void deleteCheckedCart(String username);


	/**
	 * 计算当前选中购物项的优惠金额
	 * @param username
	 * @return
	 */
	public int preferential(String username);


	/**
	 *   获取最新的购物车列表
	 * @param username  当前登录用户名
	 * @return
	 */
	public List<Map<String,Object>> findNewOrderItemList(String username);


}
