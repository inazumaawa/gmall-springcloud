package coupon.mapper;

import model.Coupon;
import model.UserCoupon;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 优惠卷数据访问层
 */
@Mapper
public interface CouponMapper {

    @Select("SELECT * FROM coupon ORDER BY created_time DESC")
    List<Coupon> findAllCoupons();

    @Select("SELECT * FROM coupon WHERE id = #{id}")
    Coupon findById(Integer id);

    @Select("SELECT * FROM coupon WHERE status = 'active' AND start_time <= NOW() AND end_time >= NOW() ORDER BY created_time DESC")
    List<Coupon> findAvailableCoupons();

    @Insert("INSERT INTO coupon(name, type, condition_amount, reduce_amount, total_count, received_count, start_time, end_time, status, created_time) " +
            "VALUES(#{name}, #{type}, #{conditionAmount}, #{reduceAmount}, #{totalCount}, 0, #{startTime}, #{endTime}, 'active', NOW())")
    void insertCoupon(Coupon coupon);

    @Update("UPDATE coupon SET name=#{name}, type=#{type}, condition_amount=#{conditionAmount}, " +
            "reduce_amount=#{reduceAmount}, total_count=#{totalCount}, start_time=#{startTime}, end_time=#{endTime} WHERE id=#{id}")
    void updateCoupon(Coupon coupon);

    @Delete("DELETE FROM coupon WHERE id = #{id}")
    void deleteCoupon(Integer id);

    @Select("SELECT * FROM user_coupon WHERE uid = #{uid} ORDER BY get_time DESC")
    List<UserCoupon> findUserCoupons(Integer uid);

    @Select("SELECT * FROM user_coupon WHERE uid = #{uid} AND cid = #{cid} AND status = 'unused'")
    UserCoupon findUserCouponByCid(Integer uid, Integer cid);

    @Select("SELECT * FROM user_coupon WHERE id = #{id}")
    UserCoupon findUserCouponById(Integer id);

    @Insert("INSERT INTO user_coupon(uid, cid, status, get_time) VALUES(#{uid}, #{cid}, 'unused', NOW())")
    void insertUserCoupon(UserCoupon userCoupon);

    @Update("UPDATE user_coupon SET status = #{status}, use_time = #{useTime} WHERE id = #{id}")
    void updateUserCoupon(UserCoupon userCoupon);

    @Update("UPDATE coupon SET received_count = received_count + 1 WHERE id = #{id} AND received_count < total_count")
    int incrReceivedCount(Integer id);
}
