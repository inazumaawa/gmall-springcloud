package admin.service.impl;

import admin.service.AdminUserFeignClient;
import model.Result;
import model.ResultCodeEnum;
import model.User;
import org.springframework.stereotype.Service;

/**
 * 管理员-用户管理Feign降级处理类
 */
@Service
public class AdminUserFeignClientFallback implements AdminUserFeignClient {

    @Override
    public Result userList() {
        return Result.failure(ResultCodeEnum.SERVER_ERROR, "认证服务异常");
    }

    @Override
    public Result deleteUser(User user) {
        return Result.failure(ResultCodeEnum.SERVER_ERROR, "认证服务异常");
    }

    @Override
    public Result findByUsername(User user) {
        return Result.failure(ResultCodeEnum.SERVER_ERROR, "认证服务异常");
    }

    @Override
    public Result updateUser(User user) {
        return Result.failure(ResultCodeEnum.SERVER_ERROR, "认证服务异常");
    }
}
