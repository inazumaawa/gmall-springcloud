package carts.service.impl;

import carts.service.GoodsServiceFeignClient;
import model.Result;
import model.ResultCodeEnum;
import org.springframework.stereotype.Service;

@Service
public class GoodsServiceFeignClientFallback implements GoodsServiceFeignClient {
    @Override
    public Result goodsDetail(int id) {
        return Result.failure(ResultCodeEnum.SERVER_ERROR,"商品服务异常");
    }
}