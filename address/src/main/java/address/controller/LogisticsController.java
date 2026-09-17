package address.controller;

import address.service.LogisticsService;
import model.LogisticsTrack;
import model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 物流轨迹控制器（并入 address 模块）
 */
@RestController
@RequestMapping("/address/logistics")
public class LogisticsController {

    @Autowired
    private LogisticsService logisticsService;

    /** 查询订单物流轨迹 */
    @GetMapping("/track")
    public Result track(@RequestParam Integer orderId) {
        return logisticsService.track(orderId);
    }

    /** 后台新增物流节点 */
    @PostMapping("/add")
    public Result add(@RequestBody LogisticsTrack track) {
        return logisticsService.add(track);
    }
}
