package customerService.mapper;

import model.Cart;
import model.Goods;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;


@Mapper
public interface CartMapper {
    @Insert("INSERT INTO cart(gname, number, price, gid, uid, gpic) VALUES(#{gname}, #{number}, #{price}, #{gid}, #{uid}, #{gpic})")
    void insertCart(Cart cart);
    @Select("SELECT * FROM cart WHERE uid = #{uid}")
    List<Cart> findAllCartByUid(Integer uid);
    @Select("SELECT * FROM cart WHERE uid = #{uid} AND gid = #{gid}")
    Cart cartDuplicate(Integer uid, Integer gid);

    @Select("select * from goods where gid=#{gid}")
    Goods getGoodDetail(Integer gid);

    @Select("SELECT * FROM goods WHERE gname LIKE CONCAT('%',#{name},'%') LIMIT 1")
    Goods findGoodsByName(String name);

    @Select("SELECT * FROM goods")
    List<Goods> findAllGoods();

    @Update("UPDATE cart SET number = #{number} WHERE id = #{id}")
    void updateNumberById(Integer id, Integer number);
}
