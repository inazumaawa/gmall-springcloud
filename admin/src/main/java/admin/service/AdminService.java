package admin.service;

import model.Goods;
import model.Order;
import model.Result;

import java.util.List;

/**
 * 管理员服务接口
 */
public interface AdminService {
    //商品管理-查询所有商品
    Result listGoods();
    //商品管理-新增商品
    Result addGoods(Goods goods);
    //商品管理-更新商品
    Result updateGoods(Goods goods);
    //商品管理-删除商品
    Result deleteGoods(Integer gid);
    //订单管理-查询所有订单
    Result listOrders();
    //订单管理-根据状态查询订单
    Result listOrdersByStatus(String status);
    //订单管理-更新订单状态
    Result updateOrderStatus(Integer id, String status);
    //订单管理-删除订单
    Result deleteOrder(Integer oid);
    //订单管理-查询订单详情(含订单项)
    Result getOrderDetail(Integer oid);
    //数据统计-综合统计
    Result stats();
}
