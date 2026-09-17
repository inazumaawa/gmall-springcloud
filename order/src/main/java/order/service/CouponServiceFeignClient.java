package order.service;

import model.Result;
import order.service.impl.CouponServiceFeignClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.Map;

@Service
@FeignClient(name = "coupon-server", fallback = CouponServiceFeignClientFallback.class)
public interface CouponServiceFeignClient {
    @PostMapping("/coupon/use")
    Result useCoupon(@RequestHeader("uid") Integer uid, @RequestBody Map<String, Object> request);

    @GetMapping("/coupon/validate-for-order")
    Result validateForOrder(@RequestHeader("uid") Integer uid,
                            @RequestParam("ucId") Integer ucId,
                            @RequestParam("orderAmount") BigDecimal orderAmount);
}
