package address.mapper;

import model.LogisticsTrack;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 物流轨迹数据访问层
 */
@Mapper
public interface LogisticsMapper {

    @Select("SELECT id, order_id AS orderId, address_id AS addressId, status, location, description, " +
            "track_time AS trackTime FROM logistics_track WHERE order_id = #{orderId} ORDER BY track_time ASC")
    List<LogisticsTrack> findByOrderId(Integer orderId);

    @Insert("INSERT INTO logistics_track(order_id, address_id, status, location, description, track_time) " +
            "VALUES(#{orderId}, #{addressId}, #{status}, #{location}, #{description}, #{trackTime})")
    void insert(LogisticsTrack track);
}
