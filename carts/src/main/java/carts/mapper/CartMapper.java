package carts.mapper;

import model.Cart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface CartMapper {
    //加入购物车
    @Insert("INSERT INTO cart(gname, number, price, gid, uid, gpic) VALUES(#{gname}, #{number}, #{price}, #{gid}, #{uid}, #{gpic})")
    void insertCart(Cart cart);

    //查询是否已经在购物车
    @Select("SELECT * FROM cart WHERE uid = #{uid} AND gid = #{gid}")
    Cart cartDuplicate(Integer uid, Integer gid);

    //更新购物车数量
    @Update("UPDATE cart SET number = #{number} WHERE id = #{id}")
    void updateNumberById(Integer id, Integer number);

    //移出购物车
    @Delete("DELETE FROM cart WHERE id=#{id}")
    void deleteCart(Integer id);

    //根据id查询购物车项（用于归属校验）
    @Select("SELECT * FROM cart WHERE id = #{id}")
    Cart findById(Integer id);

    //购物车列表
    @Select("SELECT * FROM cart WHERE uid = #{uid}")
    List<Cart> findAllCartByUid(Integer uid);

    //根据前端选中的id集合获取购物车列表
    @Select("<script>" +
            "SELECT * FROM cart WHERE uid = #{uid} AND id IN " +
            "<foreach item='id' collection='cartIds' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    List<Cart> findCartsByUidAndCartIds(@Param("uid") Integer uid, @Param("cartIds") List<Integer> cartIds);

    //根据前端选中的id集合移出购物车
    @Delete("<script>" +
            "DELETE FROM cart WHERE uid = #{uid} AND id IN " +
            "<foreach item='id' collection='cartIds' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    void deleteCartsByUidAndCartIds(@Param("uid") Integer uid, @Param("cartIds") List<Integer> cartIds);
}
