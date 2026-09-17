package pay.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface OrderMapper {
    @Update("update orders set status='PAID' where id=#{oid}")
    int updateByPrimaryKey(int oid);

    @Select("select coupon_id from orders where id=#{id}")
    Integer findCouponIdById(int id);

    @Select("select uid from orders where id=#{id}")
    Integer findUidById(int id);
}