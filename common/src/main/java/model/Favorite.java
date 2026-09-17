package model;

import lombok.Data;

import java.util.Date;

/**
 * 商品收藏实体类
 */
@Data
public class Favorite {
    private Integer id;             // 收藏记录ID
    private Integer uid;            // 用户ID
    private Integer gid;            // 商品ID
    private Date createdTime;       // 收藏时间
}
