package order.service;

import model.Result;
import order.service.impl.CartServiceFeignClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Service
@FeignClient(name = "carts-server",fallback = CartServiceFeignClientFallback.class)
public interface CartServiceFeignClient {
    @RequestMapping("/cart/sublist")
    Result subCartlist(@RequestHeader("uid") int uid, @RequestBody List<Integer> cartIds);
    @RequestMapping("/cart/deletelist")
    Result deleteCartlist(@RequestHeader("uid") int uid, @RequestBody List<Integer> cartIds);
}
