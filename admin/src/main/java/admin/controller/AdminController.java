package admin.controller;

import admin.service.AdminService;
import admin.service.AdminUserFeignClient;
import model.Goods;
import model.Result;
import model.ResultCodeEnum;
import model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员控制器
 * 提供商品管理、订单管理、用户管理功能
 */
@RestController
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private AdminService adminService;
    @Autowired
    private AdminUserFeignClient adminUserFeignClient;

    //========== 商品管理 ==========
    //查询所有商品
    @GetMapping("/goods/list")
    public Result listGoods() {
        return adminService.listGoods();
    }

    //新增商品
    @PostMapping("/goods/add")
    public Result addGoods(@RequestBody Goods goods) {
        return adminService.addGoods(goods);
    }

    //更新商品
    @PutMapping("/goods/update")
    public Result updateGoods(@RequestBody Goods goods) {
        return adminService.updateGoods(goods);
    }

    //删除商品
    @DeleteMapping("/goods/delete")
    public Result deleteGoods(@RequestParam Integer gid) {
        return adminService.deleteGoods(gid);
    }

    //========== 订单管理 ==========
    //查询所有订单
    @GetMapping("/order/list")
    public Result listOrders() {
        return adminService.listOrders();
    }

    //根据状态查询订单(管理端用)
    @GetMapping("/order/listByStatus")
    public Result listOrdersByStatus(@RequestParam String status) {
        return adminService.listOrdersByStatus(status);
    }

    //更新订单状态(发货/退款/完成售后等)
    @PutMapping("/order/status")
    public Result updateOrderStatus(@RequestParam Integer id, @RequestParam String status) {
        return adminService.updateOrderStatus(id, status);
    }

    //删除订单
    @DeleteMapping("/order/delete")
    public Result deleteOrder(@RequestParam Integer oid) {
        return adminService.deleteOrder(oid);
    }

    //查询订单详情(含订单项)
    @GetMapping("/order/detail")
    public Result getOrderDetail(@RequestParam Integer oid) {
        return adminService.getOrderDetail(oid);
    }

    //========== 用户管理(通过Feign调用auth-server) ==========
    //获取用户列表
    @GetMapping("/user/list")
    public Result userList() {
        return adminUserFeignClient.userList();
    }

    //更新用户信息
    @PutMapping("/user/update")
    public Result updateUser(@RequestBody User user) {
        if (user == null || !StringUtils.hasText(user.getUaccount())) {
            return Result.failure(ResultCodeEnum.BAD_REQUEST, "用户名不能为空");
        }
        var checkResult = adminUserFeignClient.findByUsername(user);
        if (checkResult == null || !Boolean.TRUE.equals(checkResult.getSuccess())) {
            return Result.failure(ResultCodeEnum.BAD_REQUEST, "用户不存在");
        }
        return adminUserFeignClient.updateUser(user);
    }

    //删除用户
    @DeleteMapping("/user/delete")
    public Result deleteUser(@RequestBody User user) {
        if (user == null || !StringUtils.hasText(user.getUaccount())) {
            return Result.failure(ResultCodeEnum.BAD_REQUEST, "用户名不能为空");
        }
        var checkResult = adminUserFeignClient.findByUsername(user);
        if (checkResult == null || !Boolean.TRUE.equals(checkResult.getSuccess())) {
            return Result.failure(ResultCodeEnum.BAD_REQUEST, "用户不存在");
        }
        return adminUserFeignClient.deleteUser(user);
    }

    //========== 数据统计 ==========
    //综合统计（总览/趋势/状态分布/商品排行）
    @GetMapping("/stats")
    public Result stats() {
        return adminService.stats();
    }
}
