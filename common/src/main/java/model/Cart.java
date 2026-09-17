package model;

import lombok.Data;

@Data
public class Cart {
    private Integer id;         // 购物车主键ID
    private String gname;       // 商品名称（快照）
    private Integer number;     // 购买数量
    private Integer price;      // 商品单价（快照，单位：元）
    private Integer gid;        // 商品ID
    private Integer uid;        // 用户ID
    private String gpic;        // 商品图片URL（快照）
}
