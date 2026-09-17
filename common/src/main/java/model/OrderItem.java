package model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItem {
    private Integer id;             // 订单项主键ID
    private Integer oid;            // 订单ID（外键关联 orders.id）
    private Integer gid;            // 商品ID
    private Integer quantity;       // 购买数量
    private BigDecimal price;       // 商品单价快照（单位：元）
    private String gname;           // 商品名称快照
    private String gpic;            // 商品图片URL快照
}