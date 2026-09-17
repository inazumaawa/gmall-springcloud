package model;

import lombok.Data;

import java.util.Date;

/**
 * 物流轨迹实体类（并入 address 模块）
 */
@Data
public class LogisticsTrack {
    private Integer id;          // 物流轨迹ID
    private Integer orderId;     // 订单ID
    private Integer addressId;   // 收货地址ID
    private String status;       // 物流状态
    private String location;     // 当前位置
    private String description;  // 轨迹描述
    private Date trackTime;      // 轨迹时间
}
