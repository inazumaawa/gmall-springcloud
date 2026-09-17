package model;

import lombok.Data;

/**
 * 收货地址实体类
 */
@Data
public class Address {
    private Integer id;             // 地址主键ID
    private Integer uid;            // 所属用户ID
    private String receiverName;    // 收货人姓名
    private String phone;           // 收货人手机号
    private String province;        // 省份
    private String city;            // 城市
    private String district;        // 区/县
    private String detail;          // 详细地址
    private Integer isDefault;      // 是否默认地址 1=是 0=否
}
