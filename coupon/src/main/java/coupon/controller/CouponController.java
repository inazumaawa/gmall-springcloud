package coupon.controller;

import coupon.service.CouponService;
import model.Coupon;
import model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 优惠卷控制器
 */
@RestController
@RequestMapping("/coupon")
public class CouponController {
    @Autowired
    private CouponService couponService;

    @GetMapping("/list")
    public Result listAll() {
        return couponService.listAllCoupons();
    }

    @GetMapping("/available")
    public Result listAvailable() {
        return couponService.listAvailableCoupons();
    }

    @GetMapping("/detail")
    public Result detail(@RequestParam Integer id) {
        return couponService.getCouponById(id);
    }

    @PostMapping("/create")
    public Result create(@RequestBody Coupon coupon) {
        return couponService.createCoupon(coupon);
    }

    @PutMapping("/update")
    public Result update(@RequestBody Coupon coupon) {
        return couponService.updateCoupon(coupon);
    }

    @DeleteMapping("/delete")
    public Result delete(@RequestParam Integer id) {
        return couponService.deleteCoupon(id);
    }

    @PostMapping("/receive")
    public Result receive(@RequestHeader("uid") Integer uid, @RequestParam Integer cid) {
        return couponService.userReceiveCoupon(uid, cid);
    }

    @GetMapping("/my")
    public Result myCoupons(@RequestHeader("uid") Integer uid) {
        return couponService.listUserCoupons(uid);
    }

    @PostMapping("/use")
    public Result use(@RequestHeader("uid") Integer uid, @RequestBody Map<String, Object> request) {
        Integer ucId = request.get("ucId") != null ? Integer.valueOf(request.get("ucId").toString()) : null;
        BigDecimal orderAmount = request.get("orderAmount") != null ? new BigDecimal(request.get("orderAmount").toString()) : null;
        if (ucId == null || orderAmount == null) {
            return model.Result.failure(model.ResultCodeEnum.FAIL, "参数不能为空");
        }
        return couponService.useCoupon(uid, ucId, orderAmount);
    }

    @GetMapping("/validate-for-order")
    public Result validateForOrder(@RequestHeader("uid") Integer uid,
                                   @RequestParam Integer ucId,
                                   @RequestParam BigDecimal orderAmount) {
        return couponService.validateForOrder(uid, ucId, orderAmount);
    }
}
