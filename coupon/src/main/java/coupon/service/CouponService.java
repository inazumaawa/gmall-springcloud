package coupon.service;

import model.Coupon;
import model.Result;

import java.math.BigDecimal;

/**
 * 优惠卷服务接口
 */
public interface CouponService {
    Result listAllCoupons();
    Result listAvailableCoupons();
    Result getCouponById(Integer id);
    Result createCoupon(Coupon coupon);
    Result updateCoupon(Coupon coupon);
    Result deleteCoupon(Integer id);
    Result userReceiveCoupon(Integer uid, Integer cid);
    Result listUserCoupons(Integer uid);
    Result useCoupon(Integer uid, Integer ucId, BigDecimal orderAmount);
    Result validateForOrder(Integer uid, Integer ucId, BigDecimal orderAmount);
}
