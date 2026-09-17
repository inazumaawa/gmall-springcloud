package gateway.controller;

import model.Result;
import model.ResultCodeEnum;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Gateway 熔断降级控制器
 */
@RestController
public class fallback {
    @RequestMapping("/authfallback")
    public Result authfallback() {
        return Result.failure(ResultCodeEnum.SERVER_ERROR, "认证服务异常");
    }
    @RequestMapping("/goodsfallback")
    public Result goodsfallback() {
        return Result.failure(ResultCodeEnum.SERVER_ERROR,"商品服务异常");
    }
    @RequestMapping("/cartfallback")
    public Result cartfallback() {
        return Result.failure(ResultCodeEnum.SERVER_ERROR,"购物车服务异常");
    }
    @RequestMapping("/orderfallback")
    public Result orderfallback() {
        return Result.failure(ResultCodeEnum.SERVER_ERROR,"订单服务异常");
    }
    @RequestMapping("/payfallback")
    public Result payfallback() {
        return Result.failure(ResultCodeEnum.SERVER_ERROR,"支付服务异常");
    }
    @RequestMapping("/addressfallback")
    public Result addressfallback() {
        return Result.failure(ResultCodeEnum.SERVER_ERROR,"地址服务异常");
    }
    @RequestMapping("/favoritesfallback")
    public Result favoritesfallback() {
        return Result.failure(ResultCodeEnum.SERVER_ERROR,"收藏服务异常");
    }
    @RequestMapping("/reviewfallback")
    public Result reviewfallback() {
        return Result.failure(ResultCodeEnum.SERVER_ERROR,"评价服务异常");
    }
    @RequestMapping("/usercenterfallback")
    public Result usercenterfallback() {
        return Result.failure(ResultCodeEnum.SERVER_ERROR,"个人中心服务异常");
    }
    @RequestMapping("/adminfallback")
    public Result adminfallback() {
        return Result.failure(ResultCodeEnum.SERVER_ERROR,"管理服务异常");
    }
    @RequestMapping("/couponfallback")
    public Result couponfallback() {
        return Result.failure(ResultCodeEnum.SERVER_ERROR,"优惠卷服务异常");
    }
    @RequestMapping("/obsfallback")
    public Result obsfallback() {
        return Result.failure(ResultCodeEnum.SERVER_ERROR,"对象存储服务异常");
    }
    @RequestMapping("/aifallback")
    public Result aifallback() {
        return Result.failure(ResultCodeEnum.SERVER_ERROR,"AI客服服务异常");
    }
}
