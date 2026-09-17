package model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 优惠卷实体类
 */
@Data
public class Coupon {
    private Integer id;                 // 优惠券模板ID
    private String name;                // 优惠券名称（如"618满减"）
    private String type;                // 优惠券类型
    private BigDecimal conditionAmount; // 满减门槛金额（null表示无门槛）
    private BigDecimal reduceAmount;    // 减免金额
    private Integer totalCount;         // 发行总量
    private Integer receivedCount;      // 已领取数量
    private Date startTime;             // 有效期开始时间
    private Date endTime;               // 有效期结束时间
    private String status;              // 模板状态
    private Date createdTime;           // 创建时间
}
