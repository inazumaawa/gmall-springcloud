package favorites.mapper;

import model.Favorite;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 收藏数据访问层
 */
@Mapper
public interface FavoriteMapper {
    //添加收藏
    @Insert("INSERT INTO favorite(uid, gid, created_time) VALUES(#{uid}, #{gid}, NOW())")
    void insertFavorite(Favorite favorite);

    //取消收藏
    @Delete("DELETE FROM favorite WHERE uid = #{uid} AND gid = #{gid}")
    void deleteFavorite(Integer uid, Integer gid);

    //根据id删除收藏
    @Delete("DELETE FROM favorite WHERE id = #{id}")
    void deleteFavoriteById(Integer id);

    //根据id查询收藏（用于归属校验）
    @Select("SELECT * FROM favorite WHERE id = #{id}")
    Favorite findById(Integer id);

    //查询用户是否已收藏该商品
    @Select("SELECT * FROM favorite WHERE uid = #{uid} AND gid = #{gid}")
    Favorite checkFavorite(Integer uid, Integer gid);

    //获取用户收藏列表
    @Select("SELECT * FROM favorite WHERE uid = #{uid} ORDER BY created_time DESC")
    List<Favorite> findByUid(Integer uid);
}
