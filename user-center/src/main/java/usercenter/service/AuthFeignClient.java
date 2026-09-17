package usercenter.service;

import model.Result;
import model.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

/**
 * 认证服务Feign客户端
 */
@Service
@FeignClient(name = "auth-server", fallback = usercenter.service.impl.AuthFeignClientFallback.class)
public interface AuthFeignClient {
    //获取用户信息
    @RequestMapping("/auth/userinfo")
    Result userInfo(@RequestBody User user);

    //更新用户信息
    @RequestMapping("/auth/update")
    Result updateUser(@RequestBody User user);

    //修改密码
    @RequestMapping("/auth/changepwd")
    Result changePassword(@RequestBody Map<String, String> request);

    //更新用户头像
    @RequestMapping("/auth/updateavatar")
    Result updateAvatar(@RequestBody User user);
}
