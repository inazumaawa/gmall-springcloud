package auth.service;

import model.Result;
import model.User;

import java.util.List;

/**
 * 用户服务接口
 */
public interface UserService {
    //注册接口
    void register(User user);
    //根据用户名查询接口
    User findByUsername(String username);
    //根据邮箱查询用户
    User findByEmail(String email);
    //根据账号查询用户
    User getUserByAccount(String uaccount);
    //获取用户列表
    List<User> listUsers();
    //更新用户信息
    void updateUser(User user);
    //更新用户头像
    void updateAvatar(User user);
    //修改密码
    Result changePassword(String uaccount, String oldPassword, String newPassword);
    //删除用户
    void deleteUser(String uaccount);
}
