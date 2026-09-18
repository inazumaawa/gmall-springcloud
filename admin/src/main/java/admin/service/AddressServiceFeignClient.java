package admin.service;

import model.LogisticsTrack;
import model.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 管理员-物流轨迹Feign客户端(调用address-server)
 */
@Service
@FeignClient(name = "address-server")
public interface AddressServiceFeignClient {
    //新增物流轨迹节点
    @PostMapping("/address/logistics/add")
    Result addLogisticsTrack(@RequestBody LogisticsTrack track);
}
