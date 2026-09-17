package admin.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 数据统计访问层
 */
@Mapper
public interface StatsMapper {

    /** 总览：订单数、销售额 */
    @Select("SELECT COUNT(*) AS orderCount, IFNULL(SUM(total_price), 0) AS totalSales " +
            "FROM orders WHERE status IN ('PAID','SHIPPED','COMPLETED')")
    Map<String, Object> overview();

    /** 近7天销售趋势 */
    @Select("SELECT DATE_FORMAT(created_time, '%Y-%m-%d') AS day, COUNT(*) AS orderCount, " +
            "IFNULL(SUM(total_price), 0) AS sales FROM orders " +
            "WHERE status IN ('PAID','SHIPPED','COMPLETED') " +
            "AND created_time >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
            "GROUP BY DATE_FORMAT(created_time, '%Y-%m-%d') ORDER BY day ASC")
    List<Map<String, Object>> orderTrend();

    /** 订单状态分布 */
    @Select("SELECT status, COUNT(*) AS count FROM orders GROUP BY status")
    List<Map<String, Object>> orderStatus();

    /** 商品销量 TOP10 */
    @Select("SELECT gname AS name, SUM(quantity) AS sales, SUM(quantity * price) AS amount " +
            "FROM order_item GROUP BY gname ORDER BY sales DESC LIMIT 10")
    List<Map<String, Object>> topGoods();
}
