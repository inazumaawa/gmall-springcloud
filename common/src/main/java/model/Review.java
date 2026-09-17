package model;

import lombok.Data;

import java.util.Date;

/**
 * 商品评价实体类
 */
@Data
public class Review {
    private Integer id;             // 评价主键ID
    private Integer uid;            // 评价用户ID
    private Integer gid;            // 商品ID
    private Integer oid;            // 关联订单ID
    private String content;         // 评价内容
    private Integer rating;         // 评分（1-5星）
    private Date createdTime;       // 评价时间
    private String uname;           // 评价人用户名
}
