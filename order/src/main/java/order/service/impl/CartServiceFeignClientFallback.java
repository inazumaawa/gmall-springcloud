package order.service.impl;

import model.Result;
import model.ResultCodeEnum;
import order.service.CartServiceFeignClient;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CartServiceFeignClientFallback implements CartServiceFeignClient {

    @Override
    public Result subCartlist(int uid, List<Integer> cartIds) {
        return Result.failure(ResultCodeEnum.SERVER_ERROR, "购物车服务异常");
    }

    @Override
    public Result deleteCartlist(int uid, List<Integer> cartIds) {
        return Result.failure(ResultCodeEnum.FAIL, "购物车服务异常");
    }
}
