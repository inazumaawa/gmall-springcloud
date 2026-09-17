package usercenter.controller;

import model.Result;
import model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import usercenter.service.AuthFeignClient;

import java.util.Map;

/**
 * 个人中心控制器
 * 通过Feign调用认证服务获取/更新用户信息
 */
@RestController
@RequestMapping("/usercenter")
public class UserCenterController {
    @Autowired
    private AuthFeignClient authFeignClient;

    //获取个人信息
    @GetMapping("/profile")
    public Result getUserProfile(@RequestHeader("uid") String uid) {
        User user = new User();
        user.setUaccount(uid);
        return authFeignClient.userInfo(user);
    }

    //更新个人信息
    @PutMapping("/profile")
    public Result updateProfile(@RequestHeader("uid") String uid, @RequestBody User user) {
        // 防止越权：校验body中的uaccount与Token中的uid一致
        if (user.getUaccount() != null && !user.getUaccount().equals(uid)) {
            return Result.failure(model.ResultCodeEnum.FAIL, "无权操作");
        }
        user.setUaccount(uid);
        return authFeignClient.updateUser(user);
    }

    //修改密码
    @PutMapping("/password")
    public Result changePassword(@RequestHeader("uid") String uid, @RequestBody Map<String, String> request) {
        // 防止越权：校验body中的uaccount与Token中的uid一致
        String uaccount = request.get("uaccount");
        if (uaccount != null && !uaccount.equals(uid)) {
            return Result.failure(model.ResultCodeEnum.FAIL, "无权操作");
        }
        request.put("uaccount", uid);
        return authFeignClient.changePassword(request);
    }

    /**
     * 更新用户头像 URL
     * 前端需先通过 POST /obs/upload 上传头像文件拿到 URL，再调此接口更新。
     * 上传 OBS 时建议 objectKey 使用前缀 "avatar/"。
     */
    @PutMapping("/avatar")
    public Result uploadAvatar(@RequestHeader("uid") String uid, @RequestBody Map<String, String> body) {
        String uavatar = body.get("uavatar");
        if (uavatar == null || uavatar.trim().isEmpty()) {
            return Result.failure(model.ResultCodeEnum.FAIL, "头像URL不能为空");
        }
        User user = new User();
        user.setUaccount(uid);
        user.setUavatar(uavatar);
        return authFeignClient.updateAvatar(user);
    }
}
