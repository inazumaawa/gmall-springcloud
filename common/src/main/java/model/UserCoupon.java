package model;

import lombok.Data;

import java.util.Date;

/**
 * 用户优惠卷实体类
 */
@Data
public class UserCoupon {
    private Integer id;         // 用户优惠券记录ID
    private Integer uid;        // 用户ID
    private Integer cid;        // 关联的优惠券模板ID
    private String status;      // 状态 unused=未使用 used=已使用
    private Date getTime;       // 领取时间
    private Date useTime;       // 使用时间（核销时填写）
}
