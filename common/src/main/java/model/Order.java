package model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 订单实体类
 */
@Data
public class Order {
    private Integer id;                 // 订单主键ID
    private Integer uid;                // 下单用户ID
    private BigDecimal totalPrice;      // 订单总金额
    private BigDecimal discountAmount;  // 优惠券抵扣金额
    private Integer couponId;           // 使用的用户优惠券ID
    private String status;              // 订单状态 PAY=待支付 PAID=已支付
    private Date createdTime;           // 创建时间
    private String address;             // 收货地址拼接字符串（查询时动态生成）
    private Integer aid;                // 收货地址ID
    List<OrderItem> orderItems;         // 订单明细列表
}
