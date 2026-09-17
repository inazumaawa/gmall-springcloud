package order.service.impl;

import model.Result;
import model.ResultCodeEnum;
import order.service.AddressServiceFeignClient;
import org.springframework.stereotype.Component;

@Component
public class AddressServiceFeignClientFallback implements AddressServiceFeignClient {
    @Override
    public Result getAddressDetail(Integer uid, Integer id) {
        return Result.failure(ResultCodeEnum.SERVER_ERROR, "地址服务不可用");
    }
}
