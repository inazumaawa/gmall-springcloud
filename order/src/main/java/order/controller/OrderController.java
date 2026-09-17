package order.controller;

import model.Order;
import model.Result;
import model.ResultCodeEnum;
import order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 订单控制器
 */
@RestController
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    //创建订单(前端需传入收货地址id,可选优惠卷)
    @RequestMapping("/create")
    public Result orderCreate(@RequestHeader("uid") Integer uid, @RequestBody Map<String, Object> request) {
        List<Integer> selectedItems = (List<Integer>) request.get("selectedCarts");
        if (selectedItems == null || selectedItems.size() == 0) {
            return Result.failure(ResultCodeEnum.FAIL, "参数为空");
        }
        Integer aid = request.get("aid") != null ? Integer.valueOf(request.get("aid").toString()) : null;
        if (aid == null) {
            return Result.failure(ResultCodeEnum.FAIL, "收货地址id不能为空");
        }
        Integer userCouponId = request.get("userCouponId") != null ? Integer.valueOf(request.get("userCouponId").toString()) : null;
        return orderService.createOrder(uid, selectedItems, aid, userCouponId);
    }

    @RequestMapping("/list")
    public Result orderList(@RequestHeader("uid") Integer uid) {
        List<Order> orders = orderService.getOrders(uid);
        return Result.success(orders);
    }

    @DeleteMapping("/delete")
    public Result orderDelete(@RequestHeader("uid") Integer uid, @RequestParam("oid") Integer oid) {
        orderService.deleteOrder(uid, oid);
        return Result.success();
    }

    @PutMapping("/update-address")
    public Result updateAddress(@RequestHeader("uid") Integer uid,
                                @RequestParam Integer oid,
                                @RequestParam Integer aid) {
        return orderService.updateOrderAddress(uid, oid, aid);
    }
}
