package admin.mapper;

import model.Order;
import model.OrderItem;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 管理员-订单管理数据访问层
 */
@Mapper
public interface AdminOrderMapper {
    //查询所有订单
    @Select("SELECT id, uid, total_price AS totalPrice, discount_amount AS discountAmount, coupon_id AS couponId, status, created_time AS createdTime, aid FROM orders ORDER BY created_time DESC")
    List<Order> findAllOrders();

    //根据id查询订单
    @Select("SELECT id, uid, total_price AS totalPrice, discount_amount AS discountAmount, coupon_id AS couponId, status, created_time AS createdTime, aid FROM orders WHERE id = #{id}")
    Order findOrderById(Integer id);

    //根据订单id查询订单项
    @Select("SELECT * FROM order_item WHERE oid = #{oid}")
    List<OrderItem> findOrderItemsByOid(Integer oid);

    //根据状态查询订单
    @Select("SELECT id, uid, total_price AS totalPrice, discount_amount AS discountAmount, coupon_id AS couponId, status, created_time AS createdTime, aid FROM orders WHERE status = #{status} ORDER BY created_time DESC")
    List<Order> findOrdersByStatus(String status);

    //更新订单状态
    @Update("UPDATE orders SET status = #{status} WHERE id = #{id}")
    void updateOrderStatus(Integer id, String status);

    //删除订单(连带删除订单项)
    @Delete("DELETE FROM order_item WHERE oid = #{oid}")
    void deleteOrderItems(Integer oid);

    @Delete("DELETE FROM orders WHERE id = #{oid}")
    void deleteOrder(Integer oid);
}
