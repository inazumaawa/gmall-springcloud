package address.mapper;

import model.Address;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 收货地址数据访问层
 */
@Mapper
public interface AddressMapper {
    //新增地址
    @Insert("INSERT INTO address(uid, receiver_name, phone, province, city, district, detail, is_default) " +
            "VALUES(#{uid}, #{receiverName}, #{phone}, #{province}, #{city}, #{district}, #{detail}, #{isDefault})")
    void insertAddress(Address address);

    //根据id删除地址
    @Delete("DELETE FROM address WHERE id = #{id}")
    void deleteAddress(Integer id);

    //更新地址
    @Update("UPDATE address SET receiver_name=#{receiverName}, phone=#{phone}, province=#{province}, " +
            "city=#{city}, district=#{district}, detail=#{detail} WHERE id=#{id}")
    void updateAddress(Address address);

    //获取用户所有地址
    @Select("SELECT id, uid, receiver_name as receiverName, phone, province, city, district, detail, is_default as isDefault FROM address WHERE uid = #{uid}")
    List<Address> findByUid(Integer uid);

    //根据id查找地址
    @Select("SELECT id, uid, receiver_name as receiverName, phone, province, city, district, detail, is_default as isDefault FROM address WHERE id = #{id}")
    Address findById(Integer id);

    //取消用户所有默认地址
    @Update("UPDATE address SET is_default = 0 WHERE uid = #{uid}")
    void clearDefaultByUid(Integer uid);

    //设置默认地址
    @Update("UPDATE address SET is_default = 1 WHERE id = #{id}")
    void setDefault(Integer id);
}
