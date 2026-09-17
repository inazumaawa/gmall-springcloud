package usercenter.service.impl;

import model.User;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 认证服务降级处理类
 */
@Service
public class AuthFeignClientFallback implements usercenter.service.AuthFeignClient {

    @Override
    public model.Result userInfo(User user) {
        return model.Result.failure(model.ResultCodeEnum.SERVER_ERROR, "认证服务异常");
    }

    @Override
    public model.Result updateUser(User user) {
        return model.Result.failure(model.ResultCodeEnum.SERVER_ERROR, "认证服务异常");
    }

    @Override
    public model.Result changePassword(Map<String, String> request) {
        return model.Result.failure(model.ResultCodeEnum.SERVER_ERROR, "认证服务异常");
    }

    @Override
    public model.Result updateAvatar(User user) {
        return model.Result.failure(model.ResultCodeEnum.SERVER_ERROR, "认证服务异常");
    }
}
