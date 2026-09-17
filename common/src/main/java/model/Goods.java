package model;

import lombok.Data;

@Data
public class Goods {
    public int gid;             // 商品主键ID
    public int gprice;          // 商品价格（单位：元）
    public String gname;        // 商品名称
    public String gdetails;     // 商品详情描述
    private Integer types;      // 商品分类ID
    private String gpic;        // 商品图片URL
}
