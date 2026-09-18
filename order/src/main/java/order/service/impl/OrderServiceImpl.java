package order.service.impl;

import com.alibaba.fastjson.JSON;
import model.Cart;
import model.LogisticsTrack;
import model.Order;
import model.OrderItem;
import model.Result;
import model.ResultCodeEnum;
import order.mapper.OrderMapper;
import order.service.AddressServiceFeignClient;
import order.service.CartServiceFeignClient;
import order.service.CouponServiceFeignClient;
import order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 订单服务实现类
 */
@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private CartServiceFeignClient cartServiceFeignClient;
    @Autowired
    private CouponServiceFeignClient couponServiceFeignClient;
    @Autowired
    private AddressServiceFeignClient addressServiceFeignClient;
    @Autowired
    private OrderMapper orderMapper;

    @Override
    public Result createOrder(Integer uid, List<Integer> cartIds, Integer aid, Integer userCouponId) {
        //校验收货地址归属
        Result addrRes = addressServiceFeignClient.getAddressDetail(uid, aid);
        if (addrRes.getCode() != 200) {
            return Result.failure(ResultCodeEnum.FAIL, "收货地址无效");
        }
        Result res = cartServiceFeignClient.subCartlist(uid, cartIds);
        if (res.getCode() != 200)
        {
            return res;
        }
        List<Cart> selectedCarts = JSON.parseArray(JSON.toJSONString(res.getData()), Cart.class);
        if (selectedCarts.isEmpty()) {
            return Result.failure(ResultCodeEnum.FAIL, "结算清单为空");
        }
        Order order = new Order();
        order.setUid(uid);
        BigDecimal totalPrice = selectedCarts.stream()
                .map(cart -> new BigDecimal(cart.getPrice()).multiply(BigDecimal.valueOf(cart.getNumber())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalPrice(totalPrice);
        //处理优惠卷（仅校验并记录，不核销；支付成功后由回调核销）
        BigDecimal discountAmount = BigDecimal.ZERO;
        Integer couponId = null;
        if (userCouponId != null) {
            Result couponRes = couponServiceFeignClient.validateForOrder(uid, userCouponId, totalPrice);
            // 优惠券校验通过，提取抵扣金额和用户优惠券ID
            if (couponRes.getCode() == 200 && couponRes.getData() != null) {
                Map<String, Object> couponData = JSON.parseObject(JSON.toJSONString(couponRes.getData()), Map.class);
                discountAmount = new BigDecimal(couponData.get("discount").toString());
                couponId = Integer.valueOf(couponData.get("ucId").toString());
            }
        }
        order.setDiscountAmount(discountAmount);
        order.setCouponId(couponId);
        order.setStatus("PAY");
        order.setCreatedTime(new Date());
        order.setAid(aid);
        orderMapper.insertOrder(order);
        for (Cart cart : selectedCarts) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOid(order.getId());
            orderItem.setGid(cart.getGid());
            orderItem.setQuantity(cart.getNumber());
            orderItem.setPrice(BigDecimal.valueOf(cart.getPrice()));
            orderItem.setGname(cart.getGname());
            orderItem.setGpic(cart.getGpic());
            orderMapper.insertOrderItem(orderItem);
        }
        // 从购物车中移除已下单的商品
        cartServiceFeignClient.deleteCartlist(uid, cartIds);
        // 生成初始物流轨迹节点（失败不阻塞下单）
        try {
            LogisticsTrack track = new LogisticsTrack();
            track.setOrderId(order.getId());
            track.setAddressId(aid);
            track.setStatus("已下单");
            track.setLocation("商家仓库");
            track.setDescription("订单已创建，等待商家发货");
            track.setTrackTime(new Date());
            addressServiceFeignClient.addLogisticsTrack(track);
        } catch (Exception e) {
            // 忽略物流节点写入异常，保证下单流程正常
        }
        return Result.success(order);
    }

    @Override
    public List<Order> getOrders(Integer uid) {
        return orderMapper.findOrderByUid(uid);
    }

    @Override
    @Transactional
    public Result deleteOrder(Integer uid, Integer oid) {
        // 归属校验：确保订单属于当前用户
        Order order = orderMapper.findById(Long.valueOf(oid));
        if (order == null || !order.getUid().equals(uid)) {
            return Result.failure(ResultCodeEnum.FAIL, "订单不存在或无权操作");
        }
        orderMapper.deleteOrderWithItems(oid);
        return Result.success();
    }

    @Override
    public Result updateOrderAddress(Integer uid, Integer oid, Integer aid) {
        // 归属校验
        Order order = orderMapper.findById(Long.valueOf(oid));
        if (order == null || !order.getUid().equals(uid)) {
            return Result.failure(ResultCodeEnum.FAIL, "订单不存在或无权操作");
        }
        // 校验地址归属
        Result addrRes = addressServiceFeignClient.getAddressDetail(uid, aid);
        if (addrRes.getCode() != 200) {
            return Result.failure(ResultCodeEnum.FAIL, "收货地址无效");
        }
        orderMapper.updateOrderAddress(uid, oid, aid);
        return Result.success();
    }
}
