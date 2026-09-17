package goods.mapper;

import model.Goods;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface GoodsMapper {
    //查询所有商品
    @Select("select * from goods")
    List<Goods> goodsList();
    //根据id查询商品
    @Select("select * from goods where gid=#{id}")
    Goods goodsDetail(int id);
    //根据商品名称模糊搜索
    @Select("select * from goods where gname LIKE CONCAT('%', #{keyword}, '%')")
    List<Goods> goodsSerach(String keyword);
}
