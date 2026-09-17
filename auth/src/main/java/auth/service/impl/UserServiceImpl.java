package auth.service.impl;

import auth.mapper.UserMapper;
import auth.service.UserService;
import model.Result;
import model.ResultCodeEnum;
import model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Override
    public void register(User user) {
        userMapper.insertUser(user);
    }

    @Override
    public User findByUsername(String username) {
        return userMapper.findUserByName(username);
    }

    @Override
    public User findByEmail(String email) {
        return userMapper.findByEmail(email);
    }

    @Override
    public User getUserByAccount(String uaccount) {
        return userMapper.findByUaccount(uaccount);
    }

    @Override
    public List<User> listUsers() {
        return userMapper.findAllUsers();
    }

    @Override
    public void updateUser(User user) {
        userMapper.updateUser(user);
    }

    @Override
    public void updateAvatar(User user) {
        userMapper.updateAvatar(user);
    }

    @Override
    public Result changePassword(String uaccount, String oldPassword, String newPassword) {
        // 校验旧密码
        User dbUser = userMapper.findByUaccount(uaccount);
        if (dbUser == null) {
            return Result.failure(ResultCodeEnum.FAIL, "用户不存在");
        }
        if (!dbUser.getUpassword().equals(oldPassword)) {
            return Result.failure(ResultCodeEnum.FAIL, "旧密码错误");
        }
        // 更新为新密码
        dbUser.setUpassword(newPassword);
        userMapper.updatePassword(dbUser);
        return Result.success("密码修改成功");
    }

    @Override
    public void deleteUser(String uaccount) {
        userMapper.deleteByUaccount(uaccount);
    }
}
