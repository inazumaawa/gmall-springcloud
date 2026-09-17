package pay.service;

import model.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(name = "coupon-server")
public interface CouponServiceFeignClient {
    @PostMapping("/coupon/use")
    Result useCoupon(@RequestHeader("uid") Integer uid, @RequestBody Map<String, Object> request);
}
