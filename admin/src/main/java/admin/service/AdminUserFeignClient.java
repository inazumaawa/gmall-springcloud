package admin.service;

import model.Result;
import model.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 管理员-用户管理Feign客户端(调用auth-server)
 */
@Service
@FeignClient(name = "auth-server")
public interface AdminUserFeignClient {
    //获取用户列表
    @RequestMapping("/auth/userlist")
    Result userList();

    //删除用户
    @RequestMapping("/auth/deleteuser")
    Result deleteUser(@RequestBody User user);

    //根据用户名查询用户
    @RequestMapping("/auth/findbyusername")
    Result findByUsername(@RequestBody User user);

    //更新用户信息
    @RequestMapping("/auth/update")
    Result updateUser(@RequestBody User user);
}
