package admin.mapper;

import model.Goods;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 管理员-商品管理数据访问层
 */
@Mapper
public interface AdminGoodsMapper {
    //查询所有商品
    @Select("SELECT * FROM goods")
    List<Goods> findAllGoods();

    //根据id查询商品
    @Select("SELECT * FROM goods WHERE gid = #{gid}")
    Goods findGoodsById(Integer gid);

    //新增商品
    @Insert("INSERT INTO goods(gname, gprice, gdetails, types, gpic) VALUES(#{gname}, #{gprice}, #{gdetails}, #{types}, #{gpic})")
    void insertGoods(Goods goods);

    //更新商品
    @Update("UPDATE goods SET gname=#{gname}, gprice=#{gprice}, gdetails=#{gdetails}, types=#{types}, gpic=#{gpic} WHERE gid=#{gid}")
    void updateGoods(Goods goods);

    //删除商品
    @Delete("DELETE FROM goods WHERE gid = #{gid}")
    void deleteGoods(Integer gid);
}
