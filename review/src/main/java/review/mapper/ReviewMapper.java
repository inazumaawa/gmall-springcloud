package review.mapper;

import model.Review;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 评价数据访问层
 */
@Mapper
public interface ReviewMapper {
    //新增评价
    @Insert("INSERT INTO review(uid, gid, oid, content, rating, created_time, uname) " +
            "VALUES(#{uid}, #{gid}, #{oid}, #{content}, #{rating}, NOW(), #{uname})")
    void insertReview(Review review);

    //删除评价
    @Delete("DELETE FROM review WHERE id = #{id}")
    void deleteReview(Integer id);

    //根据id查询评价（用于归属校验）
    @Select("SELECT * FROM review WHERE id = #{id}")
    Review findById(Integer id);

    //根据商品id获取评价列表
    @Select("SELECT * FROM review WHERE gid = #{gid} ORDER BY created_time DESC")
    List<Review> findByGid(Integer gid);

    //根据用户id获取评价列表
    @Select("SELECT * FROM review WHERE uid = #{uid} ORDER BY created_time DESC")
    List<Review> findByUid(Integer uid);

    //根据订单id查询评价
    @Select("SELECT * FROM review WHERE oid = #{oid}")
    Review findByOid(Integer oid);
}
