package coupon.service.impl;

import coupon.mapper.CouponMapper;
import coupon.service.CouponService;
import model.Coupon;
import model.Result;
import model.ResultCodeEnum;
import model.UserCoupon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 优惠卷服务实现类
 */
@Service
public class CouponServiceImpl implements CouponService {
    @Autowired
    private CouponMapper couponMapper;

    @Override
    public Result listAllCoupons() {
        List<Coupon> coupons = couponMapper.findAllCoupons();
        return Result.success(coupons);
    }

    @Override
    public Result listAvailableCoupons() {
        List<Coupon> coupons = couponMapper.findAvailableCoupons();
        return Result.success(coupons);
    }

    @Override
    public Result getCouponById(Integer id) {
        Coupon coupon = couponMapper.findById(id);
        if (coupon == null) {
            return Result.failure(ResultCodeEnum.FAIL, "优惠卷不存在");
        }
        return Result.success(coupon);
    }

    @Override
    public Result createCoupon(Coupon coupon) {
        couponMapper.insertCoupon(coupon);
        return Result.success("创建成功");
    }

    @Override
    public Result updateCoupon(Coupon coupon) {
        Coupon exist = couponMapper.findById(coupon.getId());
        if (exist == null) {
            return Result.failure(ResultCodeEnum.FAIL, "优惠卷不存在");
        }
        // 仅覆盖请求中传入的非空字段
        if (coupon.getName() != null) {
            exist.setName(coupon.getName());
        }
        if (coupon.getType() != null) {
            exist.setType(coupon.getType());
        }
        if (coupon.getConditionAmount() != null) {
            exist.setConditionAmount(coupon.getConditionAmount());
        }
        if (coupon.getReduceAmount() != null) {
            exist.setReduceAmount(coupon.getReduceAmount());
        }
        if (coupon.getTotalCount() != null) {
            exist.setTotalCount(coupon.getTotalCount());
        }
        if (coupon.getStartTime() != null) {
            exist.setStartTime(coupon.getStartTime());
        }
        if (coupon.getEndTime() != null) {
            exist.setEndTime(coupon.getEndTime());
        }
        couponMapper.updateCoupon(exist);
        return Result.success("更新成功");
    }

    @Override
    public Result deleteCoupon(Integer id) {
        couponMapper.deleteCoupon(id);
        return Result.success("删除成功");
    }

    @Override
    public Result userReceiveCoupon(Integer uid, Integer cid) {
        Coupon coupon = couponMapper.findById(cid);
        if (coupon == null) {
            return Result.failure(ResultCodeEnum.FAIL, "优惠卷不存在");
        }
        if (!"active".equals(coupon.getStatus())) {
            return Result.failure(ResultCodeEnum.FAIL, "优惠卷已失效");
        }
        Date now = new Date();
        if (now.before(coupon.getStartTime()) || now.after(coupon.getEndTime())) {
            return Result.failure(ResultCodeEnum.FAIL, "不在优惠卷有效期内");
        }
        UserCoupon exist = couponMapper.findUserCouponByCid(uid, cid);
        if (exist != null) {
            return Result.failure(ResultCodeEnum.FAIL, "已领取过该优惠卷");
        }
        int rows = couponMapper.incrReceivedCount(cid);
        if (rows == 0) {
            return Result.failure(ResultCodeEnum.FAIL, "优惠卷已被抢光");
        }
        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setUid(uid);
        userCoupon.setCid(cid);
        couponMapper.insertUserCoupon(userCoupon);
        return Result.success("领取成功");
    }

    @Override
    public Result listUserCoupons(Integer uid) {
        List<UserCoupon> userCoupons = couponMapper.findUserCoupons(uid);
        return Result.success(userCoupons);
    }

    @Override
    public Result useCoupon(Integer uid, Integer ucId, BigDecimal orderAmount) {
        UserCoupon userCoupon = couponMapper.findUserCouponById(ucId);
        if (userCoupon == null) {
            return Result.failure(ResultCodeEnum.FAIL, "优惠卷不存在");
        }
        if (!userCoupon.getUid().equals(uid)) {
            return Result.failure(ResultCodeEnum.FAIL, "该优惠卷不属于当前用户");
        }
        if (!"unused".equals(userCoupon.getStatus())) {
            return Result.failure(ResultCodeEnum.FAIL, "优惠卷不可用");
        }
        Coupon coupon = couponMapper.findById(userCoupon.getCid());
        if (coupon == null) {
            return Result.failure(ResultCodeEnum.FAIL, "优惠卷模板已失效");
        }
        Date now = new Date();
        if (now.before(coupon.getStartTime()) || now.after(coupon.getEndTime())) {
            return Result.failure(ResultCodeEnum.FAIL, "优惠卷不在有效期内");
        }
        if (coupon.getConditionAmount() != null && orderAmount.compareTo(coupon.getConditionAmount()) < 0) {
            return Result.failure(ResultCodeEnum.FAIL,
                    "未满" + coupon.getConditionAmount() + "元，无法使用该优惠卷");
        }
        BigDecimal discount = coupon.getReduceAmount();
        userCoupon.setStatus("used");
        userCoupon.setUseTime(new Date());
        couponMapper.updateUserCoupon(userCoupon);
        // 组装返回结果，告知调用方核销详情
        Map<String, Object> result = new HashMap<>();
        result.put("discount", discount);             
        result.put("couponName", coupon.getName());   
        result.put("couponId", coupon.getId());       
        return Result.success(result);
    }

    @Override
    public Result validateForOrder(Integer uid, Integer ucId, BigDecimal orderAmount) {
        UserCoupon userCoupon = couponMapper.findUserCouponById(ucId);
        if (userCoupon == null) {
            return Result.failure(ResultCodeEnum.FAIL, "优惠卷不存在");
        }
        if (!userCoupon.getUid().equals(uid)) {
            return Result.failure(ResultCodeEnum.FAIL, "该优惠卷不属于当前用户");
        }
        if (!"unused".equals(userCoupon.getStatus())) {
            return Result.failure(ResultCodeEnum.FAIL, "优惠卷不可用");
        }
        Coupon coupon = couponMapper.findById(userCoupon.getCid());
        if (coupon == null) {
            return Result.failure(ResultCodeEnum.FAIL, "优惠卷模板已失效");
        }
        Date now = new Date();
        if (now.before(coupon.getStartTime()) || now.after(coupon.getEndTime())) {
            return Result.failure(ResultCodeEnum.FAIL, "优惠卷不在有效期内");
        }
        if (coupon.getConditionAmount() != null && orderAmount.compareTo(coupon.getConditionAmount()) < 0) {
            return Result.failure(ResultCodeEnum.FAIL,
                    "未满" + coupon.getConditionAmount() + "元，无法使用该优惠卷");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("discount", coupon.getReduceAmount());
        result.put("couponName", coupon.getName());
        result.put("couponId", coupon.getId());
        result.put("ucId", userCoupon.getId());
        return Result.success(result);
    }
}
