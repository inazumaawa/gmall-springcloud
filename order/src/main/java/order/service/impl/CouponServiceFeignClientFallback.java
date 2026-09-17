package order.service.impl;

import model.Result;
import model.ResultCodeEnum;
import order.service.CouponServiceFeignClient;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class CouponServiceFeignClientFallback implements CouponServiceFeignClient {
    @Override
    public Result useCoupon(Integer uid, Map<String, Object> request) {
        return Result.failure(ResultCodeEnum.SERVER_ERROR, "优惠卷服务不可用");
    }

    @Override
    public Result validateForOrder(Integer uid, Integer ucId, BigDecimal orderAmount) {
        return Result.failure(ResultCodeEnum.SERVER_ERROR, "优惠卷服务不可用");
    }
}
