package admin.service.impl;

import admin.mapper.AdminGoodsMapper;
import admin.mapper.AdminOrderMapper;
import admin.mapper.StatsMapper;
import admin.service.AddressServiceFeignClient;
import admin.service.AdminService;
import model.Goods;
import model.LogisticsTrack;
import model.Order;
import model.Result;
import model.ResultCodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 管理员服务实现类
 */
@Service
public class AdminServiceImpl implements AdminService {
    @Autowired
    private AdminGoodsMapper adminGoodsMapper;
    @Autowired
    private AdminOrderMapper adminOrderMapper;
    @Autowired
    private StatsMapper statsMapper;
    @Autowired
    private AddressServiceFeignClient addressServiceFeignClient;

    @Override
    public Result listGoods() {
        return Result.success(adminGoodsMapper.findAllGoods());
    }

    @Override
    public Result addGoods(Goods goods) {
        if (goods == null) {
            return Result.failure(ResultCodeEnum.BAD_REQUEST, "商品信息不能为空");
        }
        if (!StringUtils.hasText(goods.gname)) {
            return Result.failure(ResultCodeEnum.BAD_REQUEST, "商品名称不能为空");
        }
        if (goods.gprice <= 0) {
            return Result.failure(ResultCodeEnum.BAD_REQUEST, "商品价格必须大于0");
        }
        adminGoodsMapper.insertGoods(goods);
        return Result.success("添加商品成功");
    }

    @Override
    public Result updateGoods(Goods goods) {
        if (goods == null) {
            return Result.failure(ResultCodeEnum.BAD_REQUEST, "商品信息不能为空");
        }
        if (goods.gid <= 0) {
            return Result.failure(ResultCodeEnum.BAD_REQUEST, "商品ID无效");
        }
        if (!StringUtils.hasText(goods.gname)) {
            return Result.failure(ResultCodeEnum.BAD_REQUEST, "商品名称不能为空");
        }
        if (goods.gprice <= 0) {
            return Result.failure(ResultCodeEnum.BAD_REQUEST, "商品价格必须大于0");
        }
        if (adminGoodsMapper.findGoodsById(goods.gid) == null) {
            return Result.failure(ResultCodeEnum.BAD_REQUEST, "商品不存在");
        }
        adminGoodsMapper.updateGoods(goods);
        return Result.success("更新商品成功");
    }

    @Override
    public Result deleteGoods(Integer gid) {
        if (gid == null || gid <= 0) {
            return Result.failure(ResultCodeEnum.BAD_REQUEST, "商品ID无效");
        }
        if (adminGoodsMapper.findGoodsById(gid) == null) {
            return Result.failure(ResultCodeEnum.BAD_REQUEST, "商品不存在");
        }
        adminGoodsMapper.deleteGoods(gid);
        return Result.success("删除商品成功");
    }

    @Override
    public Result listOrders() {
        return Result.success(adminOrderMapper.findAllOrders());
    }

    @Override
    public Result listOrdersByStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return Result.failure(ResultCodeEnum.BAD_REQUEST, "订单状态不能为空");
        }
        return Result.success(adminOrderMapper.findOrdersByStatus(status));
    }

    @Override
    public Result updateOrderStatus(Integer id, String status) {
        if (id == null || id <= 0) {
            return Result.failure(ResultCodeEnum.BAD_REQUEST, "订单ID无效");
        }
        if (!StringUtils.hasText(status)) {
            return Result.failure(ResultCodeEnum.BAD_REQUEST, "订单状态不能为空");
        }
        Order order = adminOrderMapper.findOrderById(id);
        if (order == null) {
            return Result.failure(ResultCodeEnum.BAD_REQUEST, "订单不存在");
        }
        adminOrderMapper.updateOrderStatus(id, status);
        // 订单状态变更后追加对应物流轨迹节点（失败不影响状态更新）
        try {
            addLogisticsNode(order, status);
        } catch (Exception e) {
            // 忽略物流节点写入异常
        }
        return Result.success("更新订单状态成功");
    }

    /**
     * 根据订单状态追加物流轨迹节点
     * PAID 待发货 -> 已付款；SHIPPED 已发货 -> 已发货；COMPLETED 已完成 -> 已签收
     */
    private void addLogisticsNode(Order order, String status) {
        String nodeStatus;
        String location;
        String description;
        switch (status.toUpperCase()) {
            case "PAID":
                nodeStatus = "已付款";
                location = "商家仓库";
                description = "支付成功，等待商家发货";
                break;
            case "SHIPPED":
                nodeStatus = "已发货";
                location = "运输中";
                description = "商家已发货，快件运输中";
                break;
            case "COMPLETED":
                nodeStatus = "已签收";
                location = "收货地址";
                description = "快件已签收，感谢使用";
                break;
            default:
                // 待付款、已取消等状态不生成物流节点
                return;
        }
        LogisticsTrack track = new LogisticsTrack();
        track.setOrderId(order.getId());
        track.setAddressId(order.getAid());
        track.setStatus(nodeStatus);
        track.setLocation(location);
        track.setDescription(description);
        track.setTrackTime(new Date());
        addressServiceFeignClient.addLogisticsTrack(track);
    }

    @Override
    @Transactional
    public Result deleteOrder(Integer oid) {
        if (oid == null || oid <= 0) {
            return Result.failure(ResultCodeEnum.BAD_REQUEST, "订单ID无效");
        }
        if (adminOrderMapper.findOrderById(oid) == null) {
            return Result.failure(ResultCodeEnum.BAD_REQUEST, "订单不存在");
        }
        adminOrderMapper.deleteOrderItems(oid);
        adminOrderMapper.deleteOrder(oid);
        return Result.success("删除订单成功");
    }

    @Override
    public Result getOrderDetail(Integer oid) {
        if (oid == null || oid <= 0) {
            return Result.failure(ResultCodeEnum.BAD_REQUEST, "订单ID无效");
        }
        var order = adminOrderMapper.findOrderById(oid);
        if (order == null) {
            return Result.failure(ResultCodeEnum.BAD_REQUEST, "订单不存在");
        }
        order.setOrderItems(adminOrderMapper.findOrderItemsByOid(oid));
        return Result.success(order);
    }

    @Override
    public Result stats() {
        Map<String, Object> data = new HashMap<>();
        data.put("overview", statsMapper.overview());
        data.put("orderTrend", statsMapper.orderTrend());
        data.put("orderStatus", statsMapper.orderStatus());
        data.put("topGoods", statsMapper.topGoods());
        return Result.success(data);
    }
}
