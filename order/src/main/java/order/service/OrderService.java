package order.service;

import model.Order;
import model.Result;

import java.util.List;

/**
 * 订单服务接口
 */
public interface OrderService {
    //创建订单(含收货地址+优惠卷)
    Result createOrder(Integer uid, List<Integer> cartIds, Integer aid, Integer userCouponId);
    //获取订单
    List<Order> getOrders(Integer uid);
    //删除订单
    Result deleteOrder(Integer uid, Integer oid);
    //修改订单收货地址
    Result updateOrderAddress(Integer uid, Integer oid, Integer aid);
}
