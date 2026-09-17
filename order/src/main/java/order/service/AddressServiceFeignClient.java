package order.service;

import model.Result;
import order.service.impl.AddressServiceFeignClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@Service
@FeignClient(name = "address-server", fallback = AddressServiceFeignClientFallback.class)
public interface AddressServiceFeignClient {
    @GetMapping("/address/detail")
    Result getAddressDetail(@RequestHeader("uid") Integer uid, @RequestParam("id") Integer id);
}
