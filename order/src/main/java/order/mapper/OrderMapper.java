package order.mapper;

import model.Order;
import model.OrderItem;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 订单数据访问层
 */
@Mapper
public interface OrderMapper {
    //增加订单(含收货地址+优惠卷)
    @Insert("INSERT INTO orders (uid, total_price, discount_amount, coupon_id, status, created_time, aid) " +
            "VALUES(#{uid}, #{totalPrice}, #{discountAmount}, #{couponId}, #{status}, #{createdTime}, #{aid})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertOrder(Order order);

    //根据id查找订单
    @Select("SELECT * FROM orders WHERE id = #{orderId}")
    Order findById(Long orderId);

    //插入订单对应的商品项
    @Insert("INSERT INTO order_item(oid, gid, quantity, price, gname, gpic) VALUES(#{oid}, #{gid}, #{quantity}, #{price},#{gname},#{gpic})")
    void insertOrderItem(OrderItem orderItem);

    //获得订单列表
    List<Order> findOrderByUid(Integer uid);

    //根据订单id删除商品项
    @Delete({"DELETE FROM order_item WHERE oid = #{oid}"})
    void deleteOrderItemsByOrderId(int oid);

    //根据订单id删除订单
    @Delete("DELETE FROM orders WHERE id = #{oid}")
    void deleteOrderById(int oid);

    //修改订单收货地址
    @org.apache.ibatis.annotations.Update("UPDATE orders SET aid = #{aid} WHERE id = #{id} AND uid = #{uid}")
    int updateOrderAddress(Integer uid, Integer id, Integer aid);

    //级联删除
    default  void deleteOrderWithItems(int oid){
        deleteOrderItemsByOrderId(oid);
        deleteOrderById(oid);
    }
}
