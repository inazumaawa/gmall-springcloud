package carts.service;

import carts.service.impl.GoodsServiceFeignClientFallback;
import model.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Service
@FeignClient(name="goods-server",fallback = GoodsServiceFeignClientFallback.class)
public interface GoodsServiceFeignClient {
    @RequestMapping("/goods/detail")
    Result goodsDetail(@RequestParam int id);
}
