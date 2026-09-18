package order.service.impl;

import model.LogisticsTrack;
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

    @Override
    public Result addLogisticsTrack(LogisticsTrack track) {
        return Result.failure(ResultCodeEnum.SERVER_ERROR, "地址服务不可用");
    }
}
