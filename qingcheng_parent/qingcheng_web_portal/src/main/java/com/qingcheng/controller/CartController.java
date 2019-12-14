package com.qingcheng.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.entity.Result;
import com.qingcheng.pojo.order.Order;
import com.qingcheng.pojo.user.Address;
import com.qingcheng.service.order.CartService;
import com.qingcheng.service.order.OrderService;
import com.qingcheng.service.user.AddressService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cart")
public class CartController {

	@Reference
	private CartService cartService;

	@Reference
	private AddressService addressService;

	@Reference
	private OrderService orderService;

	public static String getUsername(){
		return SecurityContextHolder.getContext().getAuthentication().getName();
	}

	/**
	 * 从redis中获取购物车
	 * @return
	 */
	@GetMapping("/findCartList")
	public List<Map<String,Object>> findCartList(){

		//得到当前登录用户名，根据用户名查询购物车列表
		String username = getUsername();
		List<Map<String, Object>> cartList = cartService.findCartList(username);
		return cartList;
	}


	/**
	 * 添加商品到购物车
	 * @param skuId  商品id
	 * @param num    商品数量
	 * @return
	 */
	@GetMapping("/addItem")
	public Result addItem(String skuId,Integer num){

		//得到当前登录用户名
		String username = getUsername();
		cartService.addItem(username,skuId,num);
		return new Result();

	}


	/**
	 * 实现详情页到购物车的页面跳转并添加购物项到购物车
	 * @param response
	 * @param skuId   商品id
	 * @param num     商品数量
	 * @throws IOException
	 */
	@GetMapping("/buy")
	public void buy(HttpServletResponse response,String skuId,Integer num) throws IOException {

		//得到当前登录用户名
		String username = getUsername();
		cartService.addItem(username,skuId,num);
		response.sendRedirect("http://localhost:9102/cart.html");
	}

	/**
	 * 更新购物项选中状态
	 * @param skuId
	 * @param checked
	 * @return
	 */
	@GetMapping("/updateChecked")
	public Result updateChecked(String skuId,Boolean checked){

		//得到当前登录用户名
		String username = getUsername();
		Boolean flag = cartService.updateChecked(username, skuId, checked);
		if(flag){
			return new Result(0,"更新成功");
		}
		return new Result(1,"更新失败");
	}


	/**
	 * 删除选中的购物项
	 * @return
	 */
	@GetMapping("/deleteCheckedCart")
	public Result deleteCheckedCart(){
		//得到当前登录用户名
		String username = getUsername();
		cartService.deleteCheckedCart(username);
		return new Result();
	}


	/**
	 * 计算当前购物车中选中的购物项的优惠金额
	 * @return
	 */
	@GetMapping("/preferential")
	public Map preferential(){
		//得到当前登录用户名
		String username = getUsername();
		int preMoney = cartService.preferential(username);
		Map map = new HashMap();
		map.put("preferential",preMoney);

		return map;
	}


	/**
	 * 获取刷新单价后的购物车列表
	 * @return
	 */
	@GetMapping("/findNewOrderItemList")
	public List<Map<String,Object>> findNewOrderItemList(){
		//得到当前登录用户名
		String username = getUsername();
		//查询最新的购物车列表
		return cartService.findNewOrderItemList(username);

	}


	/**
	 * 根据用户名查询地址列表
	 * @return
	 */
	@GetMapping("/findAddressList")
	public List<Address> findAddressList(){
		//得到当前登录用户名
		String username = getUsername();
		return addressService.findByUsername(username);

	}


	/**
	 * 保存订单
	 * @param order
	 * @return
	 */
	@PostMapping("/saveOrder")
	public Map<String,Object> saveOrder(@RequestBody Order order){
		String username = getUsername();
		order.setUsername(username);
		return orderService.add(order);
	}

}
