package auth.controller;

import auth.service.UserService;
import model.Result;
import model.ResultCodeEnum;
import model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;
import utils.JwtUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/auth")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private JavaMailSender mailSender;
    @Value("${spring.mail.username}")
    private String mailFrom;

    //注册(默认注册为普通用户)
    @RequestMapping("/register")
    public Result register(@RequestBody User user) {
        User dbuser = userService.findByUsername(user.getUname());
        if(dbuser != null) {
            return Result.failure(ResultCodeEnum.FAIL,"用户名已存在！");
        }
        userService.register(user);
        return Result.success("注册成功");
    }

    //发送邮箱验证码
    @RequestMapping("/sendcode")
    public Result sendCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.trim().isEmpty()) {
            return Result.failure(ResultCodeEnum.FAIL, "邮箱不能为空");
        }
        String code = String.format("%06d", new Random().nextInt(999999));
        ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
        String key = "email:code:" + email;
        operations.set(key, code, 5, TimeUnit.MINUTES);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailFrom);
            message.setTo(email);
            message.setSubject("商城验证码");
            message.setText("您的验证码为：" + code + "，有效期5分钟，请尽快使用。");
            mailSender.send(message);
        } catch (Exception e) {
            stringRedisTemplate.delete(key);
            return Result.failure(ResultCodeEnum.SERVER_ERROR, "验证码发送失败，请检查邮箱地址");
        }
        return Result.success("验证码已发送");
    }

    //邮箱验证码登录
    @RequestMapping("/emaillogin")
    public Result emailLogin(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");
        if (email == null || email.trim().isEmpty()) {
            return Result.failure(ResultCodeEnum.FAIL, "邮箱不能为空");
        }
        if (code == null || code.trim().isEmpty()) {
            return Result.failure(ResultCodeEnum.FAIL, "验证码不能为空");
        }
        String key = "email:code:" + email;
        String storedCode = stringRedisTemplate.opsForValue().get(key);
        if (storedCode == null) {
            return Result.failure(ResultCodeEnum.FAIL, "验证码已过期，请重新获取");
        }
        if (!storedCode.equals(code)) {
            return Result.failure(ResultCodeEnum.FAIL, "验证码错误");
        }
        stringRedisTemplate.delete(key);
        User dbuser = userService.findByEmail(email);
        if (dbuser == null) {
            return Result.failure(ResultCodeEnum.FAIL, "该邮箱未绑定账号");
        }
        Map<String, Object> claims = new HashMap<>();
        String userId = dbuser.getUaccount();
        claims.put("id", userId);
        claims.put("role", dbuser.getUrole() != null ? dbuser.getUrole() : "user");
        String uuid = UUID.randomUUID().toString();
        claims.put("uuid", uuid);
        String token = JwtUtil.genToken(claims);
        // 生成token后，删除旧token，防止重复登录
        if (stringRedisTemplate.hasKey(userId)) {
            stringRedisTemplate.delete(userId);
        }
        ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
        operations.set(userId, token, 1, TimeUnit.HOURS);
        dbuser.setToken(token);
        dbuser.setUpassword(null);
        return Result.success(dbuser);
    }

    //生成图形验证码
    @GetMapping("/captcha")
    public Result captcha() {
        // 生成4位随机验证码
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        var random = new Random();
        var code = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        String captchaCode = code.toString();

        // 存入Redis，2分钟有效
        String captchaKey = UUID.randomUUID().toString().replace("-", "");
        stringRedisTemplate.opsForValue().set("captcha:" + captchaKey, captchaCode, 2, TimeUnit.MINUTES);

        // 生成图片
        int width = 120, height = 44;
        var image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        var g = image.createGraphics();

        // 背景
        g.setColor(new Color(240, 240, 240));
        g.fillRect(0, 0, width, height);

        // 干扰线
        for (int i = 0; i < 5; i++) {
            g.setColor(new Color(180 + random.nextInt(75), 180 + random.nextInt(75), 180 + random.nextInt(75)));
            g.drawLine(random.nextInt(width), random.nextInt(height), random.nextInt(width), random.nextInt(height));
        }

        // 写文字
        g.setFont(new Font("Arial", Font.BOLD, 26));
        for (int i = 0; i < 4; i++) {
            g.setColor(new Color(40 + random.nextInt(80), 40 + random.nextInt(80), 40 + random.nextInt(80)));
            g.drawString(String.valueOf(captchaCode.charAt(i)), 18 + i * 24, 28 + random.nextInt(6));
        }
        g.dispose();

        // 转Base64
        try (var bos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", bos);
            String base64 = Base64.getEncoder().encodeToString(bos.toByteArray());
            Map<String, String> data = new HashMap<>();
            data.put("captchaKey", captchaKey);
            data.put("captchaImage", "data:image/png;base64," + base64);
            return Result.success(data);
        } catch (IOException e) {
            return Result.failure(ResultCodeEnum.SERVER_ERROR, "验证码生成失败");
        }
    }

    //登录(返回角色信息)
    @RequestMapping("/login")
    public Result login(@RequestBody Map<String, String> request) {
        String uname = request.get("uname");
        String upassword = request.get("upassword");
        String captchaKey = request.get("captchaKey");
        String captchaCode = request.get("captchaCode");

        // 验证码校验
        if (captchaKey == null || captchaCode == null || captchaKey.isEmpty() || captchaCode.isEmpty()) {
            return Result.failure(ResultCodeEnum.FAIL, "请输入图形验证码");
        }
        String storedCode = stringRedisTemplate.opsForValue().get("captcha:" + captchaKey);
        if (storedCode == null) {
            return Result.failure(ResultCodeEnum.FAIL, "验证码已过期，请刷新");
        }
        if (!storedCode.equalsIgnoreCase(captchaCode)) {
            return Result.failure(ResultCodeEnum.FAIL, "验证码错误");
        }
        // 验证通过，删除验证码（一次性）
        stringRedisTemplate.delete("captcha:" + captchaKey);

        User dbuser = userService.findByUsername(uname);
        if(dbuser == null) {
            return Result.failure(ResultCodeEnum.FAIL,"用户名错误！");
        }
        if(dbuser.getUpassword().equals(upassword)) {
            Map<String,Object> claims= new HashMap<>();
            String userId = dbuser.getUaccount();
            claims.put("id",userId);
            //将角色写入token用于权限判定
            claims.put("role", dbuser.getUrole() != null ? dbuser.getUrole() : "user");
            //生成唯一id加入到token加密中用于判断是否多点登录
            String uuid = UUID.randomUUID().toString();
            claims.put("uuid", uuid);
            //生成token
            String token = JwtUtil.genToken(claims);
            //如果redis存在该用户的token则进行删除来避免多点登录
            if(stringRedisTemplate.hasKey(userId))
            {
                stringRedisTemplate.delete(userId);
            }
            //新的token存放到redis
            ValueOperations<String,String> operations = stringRedisTemplate.opsForValue();
            operations.set(userId,token,1, TimeUnit.HOURS);
            //给前端返回token+角色+头像
            dbuser.setToken(token);
            dbuser.setUpassword(null);
            return Result.success(dbuser);
        }
        return Result.failure(ResultCodeEnum.FAIL,"密码错误");
    }

    //退出登录
    @RequestMapping("/logout")
    public Result logout(@RequestHeader(value = "uid") String userId) {
        if(userId != null) {
            stringRedisTemplate.delete(userId);
            return Result.success();
        }
        return Result.failure(ResultCodeEnum.FAIL,"未知用户");
    }

    //获取用户信息(供个人中心/管理端调用)
    @RequestMapping("/userinfo")
    public Result getUserInfo(@RequestBody User user) {
        User dbuser = userService.getUserByAccount(user.getUaccount());
        if(dbuser == null) {
            return Result.failure(ResultCodeEnum.FAIL, "用户不存在");
        }
        dbuser.setUpassword(null);
        return Result.success(dbuser);
    }

    //更新用户信息(供个人中心调用)
    @RequestMapping("/update")
    public Result updateUser(@RequestHeader(value = "uid", required = false) String uid, @RequestBody User user) {
        // 防止越权：网关调用时校验body中的uaccount与Token中的uid一致；Feign内部调用时uid为null则放行
        if (uid != null) {
            if (user.getUaccount() != null && !user.getUaccount().equals(uid)) {
                return Result.failure(ResultCodeEnum.FAIL, "无权操作");
            }
            user.setUaccount(uid);
        }
        userService.updateUser(user);
        return Result.success("更新成功");
    }

    //更新用户头像(供个人中心调用)
    @RequestMapping("/updateavatar")
    public Result updateAvatar(@RequestHeader(value = "uid", required = false) String uid, @RequestBody User user) {
        // 防止越权：网关调用时校验body中的uaccount与Token中的uid一致；Feign内部调用时uid为null则放行
        if (uid != null) {
            if (user.getUaccount() != null && !user.getUaccount().equals(uid)) {
                return Result.failure(ResultCodeEnum.FAIL, "无权操作");
            }
            user.setUaccount(uid);
        }
        userService.updateAvatar(user);
        return Result.success("头像更新成功");
    }

    //修改密码(供个人中心调用)
    @RequestMapping("/changepwd")
    public Result changePassword(@RequestHeader(value = "uid", required = false) String uid, @RequestBody Map<String, String> request) {
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");
        if (oldPassword == null || oldPassword.trim().isEmpty()) {
            return Result.failure(ResultCodeEnum.FAIL, "旧密码不能为空");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            return Result.failure(ResultCodeEnum.FAIL, "新密码不能为空");
        }
        String uaccount = request.get("uaccount"); 
        // 防止越权：网关调用时校验body中的uaccount与Token中的uid一致；Feign内部调用时uid为null则放行
        if (uid != null) {
            if (uaccount != null && !uaccount.equals(uid)) {
                return Result.failure(ResultCodeEnum.FAIL, "无权操作");
            }
            uaccount = uid;
        }
        if (uaccount == null || uaccount.trim().isEmpty()) {
            return Result.failure(ResultCodeEnum.FAIL, "用户账号不能为空");
        }
        return userService.changePassword(uaccount, oldPassword, newPassword);
    }

    //获取用户列表(供管理端调用)
    @RequestMapping("/userlist")
    public Result userList() {
        return Result.success(userService.listUsers());
    }

    //根据用户名查询用户(供管理端调用)
    @RequestMapping("/findbyusername")
    public Result findByUsername(@RequestBody User user) {
        User dbuser = userService.getUserByAccount(user.getUaccount());
        if (dbuser == null) {
            return Result.failure(ResultCodeEnum.FAIL, "用户不存在");
        }
        dbuser.setUpassword(null);
        return Result.success(dbuser);
    }

    //删除用户(供管理端调用)
    @RequestMapping("/deleteuser")
    public Result deleteUser(@RequestBody User user) {
        userService.deleteUser(user.getUaccount());
        return Result.success("删除用户成功");
    }
}
