package goods.mapper;

import model.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CategoryMapper {
    @Select("SELECT cid, cname FROM category ORDER BY cid")
    List<Category> findAll();
}
