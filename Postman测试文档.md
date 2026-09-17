# Postman 接口测试文档

## 项目概述

- **项目类型**：Spring Cloud 微服务电商系统
- **网关地址**：`http://localhost:8080`
- **认证方式**：JWT (Bearer Token)，登录后需在 Header 中携带 `Authorization: Bearer {token}`
- **Token 有效期**：12 小时（Redis 缓存 1 小时，token 本身 12 小时）
- **uid 来源**：网关 AuthFilter 从 Token 解析后注入 `uid` 请求头，接口通过 `@RequestHeader("uid")` 获取，**前端无需手动传 uid**
- **统一响应格式**：

```json
{
  "success": true/false,
  "code": 200,          // success=true 时
  "data": {},           // 响应数据
  "message": "xxx"      // success=false 时
}
```

---

## 前置准备

### 1. Postman 环境变量设置
在 Postman 中创建 Environment，添加以下变量：

| 变量名 | 初始值 | 说明 |
|--------|--------|------|
| `baseurl` | `http://localhost:8080` | 网关地址 |
| `token` | (登录后自动填充) | JWT Token |

### 2. Pre-request Script (需认证接口通用)
在请求的 Pre-request Script 中添加：
```javascript
pm.request.headers.add({
    key: "Authorization",
    value: "Bearer " + pm.environment.get("token")
});
```

---

## 一、认证模块 (auth-server)

所有接口路径前缀：`/auth`

### 白名单接口（无需 Token）

---

#### 1.1 用户注册
- **Method**: `POST`
- **URL**: `{{baseurl}}/auth/register`
- **Headers**: `Content-Type: application/json`
- **Body** (raw JSON):
```json
{
  "uname": "testuser",
  "upassword": "123456",
  "usex": "男",
  "uemail": "test@example.com"
}
```
- **成功响应**:
```json
{
  "success": true,
  "code": 200,
  "data": "注册成功"
}
```
- **失败响应**:
```json
{
  "success": false,
  "code": 500,
  "message": "用户名已存在！"
}
```

---

#### 1.2 账号密码登录
- **Method**: `POST`
- **URL**: `{{baseurl}}/auth/login`
- **Headers**: `Content-Type: application/json`
- **Body** (raw JSON):
```json
{
  "uname": "testuser",
  "upassword": "123456"
}
```
- **成功响应**:
```json
{
  "success": true,
  "code": 200,
  "data": {
    "uaccount": "xxx",
    "uname": "testuser",
    "usex": "男",
    "urole": "user",
    "uavatar": "/avatar/default.png",
    "uemail": "test@example.com",
    "token": "eyJhbGciOiJI..."
  }
}
```
- **Postman 后置脚本**（自动保存 token）:
```javascript
var jsonData = pm.response.json();
if (jsonData.success) {
    pm.environment.set("token", jsonData.data.token);
}
```
- **失败响应**:
```json
{
  "success": false,
  "code": 500,
  "message": "用户名错误！"    // 或 "密码错误"
}
```

---

#### 1.3 发送邮箱验证码
- **Method**: `POST`
- **URL**: `{{baseurl}}/auth/sendcode`
- **Headers**: `Content-Type: application/json`
- **Body** (raw JSON):
```json
{
  "email": "test@example.com"
}
```
- **成功响应**:
```json
{
  "success": true,
  "code": 200,
  "data": "验证码已发送"
}
```
- **说明**：验证码有效期 5 分钟，存储在 Redis 中

---

#### 1.4 邮箱验证码登录
- **Method**: `POST`
- **URL**: `{{baseurl}}/auth/emaillogin`
- **Headers**: `Content-Type: application/json`
- **Body** (raw JSON):
```json
{
  "email": "test@example.com",
  "code": "123456"
}
```
- **成功响应**：同 [1.2 账号密码登录](#12-账号密码登录) 的响应
- **Postman 后置脚本**：同 [1.2](#12-账号密码登录) 的后置脚本
- **失败响应**:
```json
{
  "success": false,
  "code": 500,
  "message": "验证码已过期，请重新获取"   // 或 "验证码错误" 或 "该邮箱未绑定账号"
}
```

---

### 需认证接口（需要 Token）

以下接口需要在 Header 中携带 `Authorization: Bearer {token}`

---

#### 1.5 退出登录
- **Method**: `POST`
- **URL**: `{{baseurl}}/auth/logout`
- **Headers**:
  - `Authorization: Bearer {{token}}`
- **成功响应**:
```json
{
  "success": true,
  "code": 200
}
```

---

#### 1.6 获取用户信息（内部调用）
- **Method**: `POST`
- **URL**: `{{baseurl}}/auth/userinfo`
- **Headers**: `Content-Type: application/json`
- **Body** (raw JSON):
```json
{
  "uaccount": "xxx"
}
```
- **说明**：此接口主要供 Feign 内部调用，前端请使用 [9.1 获取个人信息](#91-获取个人信息)
- **成功响应**:
```json
{
  "success": true,
  "code": 200,
  "data": {
    "uaccount": "xxx",
    "uname": "testuser",
    "usex": "男",
    "urole": "user",
    "uavatar": "/avatar/default.png",
    "uemail": "test@example.com"
  }
}
```

---

#### 1.7 更新用户信息
- **Method**: `POST`
- **URL**: `{{baseurl}}/auth/update`
- **Headers**:
  - `Authorization: Bearer {{token}}`
  - `Content-Type: application/json`
- **说明**：uid 由网关从 Token 解析注入，前端无需传。若 body 中 `uaccount` 与 Token 中的 uid 不一致，将返回"无权操作"
- **Body** (raw JSON):
```json
{
  "uname": "新用户名",
  "usex": "女",
  "uemail": "newemail@example.com"
}
```
- **成功响应**:
```json
{
  "success": true,
  "code": 200,
  "data": "更新成功"
}
```
- **失败响应**（越权）:
```json
{
  "success": false,
  "code": 500,
  "message": "无权操作"
}
```

---

#### 1.8 更新用户头像
- **Method**: `POST`
- **URL**: `{{baseurl}}/auth/updateavatar`
- **Headers**:
  - `Authorization: Bearer {{token}}`
  - `Content-Type: application/json`
- **说明**：uid 由网关从 Token 解析注入，前端无需传。若 body 中 `uaccount` 与 Token 中的 uid 不一致，将返回"无权操作"
- **Body** (raw JSON):
```json
{
  "uavatar": "/avatar/new.png"
}
```
- **成功响应**:
```json
{
  "success": true,
  "code": 200,
  "data": "头像更新成功"
}
```

---

#### 1.9 修改密码
- **Method**: `POST`
- **URL**: `{{baseurl}}/auth/changepwd`
- **Headers**:
  - `Authorization: Bearer {{token}}`
  - `Content-Type: application/json`
- **说明**：uid 由网关从 Token 解析注入。需传旧密码验证，通过后才更新为新密码
- **Body** (raw JSON):
```json
{
  "oldPassword": "当前密码",
  "newPassword": "新密码"
}
```
- **成功响应**:
```json
{
  "success": true,
  "code": 200,
  "data": "密码修改成功"
}
```
- **失败响应**:
```json
{
  "success": false,
  "code": 500,
  "message": "旧密码错误"
}
```

---

#### 1.10 获取用户列表（管理端用）
- **Method**: `POST`
- **URL**: `{{baseurl}}/auth/userlist`
- **Headers**: `Authorization: Bearer {{token}}`（需 admin 角色 token）
- **说明**：网关 AuthFilter 已校验 admin 角色，普通用户访问返回 403
- **成功响应**:
```json
{
  "success": true,
  "code": 200,
  "data": [
    {
      "uaccount": "xxx",
      "uname": "testuser",
      ...
    }
  ]
}
```

---

#### 1.12 根据账号查询用户（管理端 Feign 内部调用）
- **Method**: `POST`
- **URL**: `{{baseurl}}/auth/findbyusername`
- **Headers**:
  - `Authorization: Bearer {{token}}`（需 admin 角色 token）
  - `Content-Type: application/json`
- **Body** (raw JSON):
```json
{
  "uaccount": "xxx"
}
```
- **成功响应**:
```json
{
  "success": true,
  "code": 200,
  "data": {
    "uaccount": "xxx",
    "uname": "testuser",
    "usex": "男",
    "urole": "user",
    "uavatar": "/avatar/default.png",
    "uemail": "test@example.com"
  }
}
```
- **说明**：此接口主要供 admin-server 更新/删除用户前校验用户是否存在，密码字段已脱敏

---

#### 1.13 删除用户（管理端用）
- **Method**: `POST`
- **URL**: `{{baseurl}}/auth/deleteuser`
- **Headers**:
  - `Authorization: Bearer {{token}}`（需 admin 角色 token）
  - `Content-Type: application/json`
- **说明**：网关 AuthFilter 已校验 admin 角色，普通用户访问返回 403
- **Body** (raw JSON):
```json
{
  "uaccount": "xxx"
}
```
- **成功响应**:
```json
{
  "success": true,
  "code": 200,
  "data": "删除用户成功"
}
```

---

## 二、商品模块 (goods-server)

所有接口路径前缀：`/goods`，**全部为白名单，无需 Token**

---

#### 2.1 商品首页列表
- **Method**: `GET`
- **URL**: `{{baseurl}}/goods/list`
- **成功响应**:
```json
{
  "success": true,
  "code": 200,
  "data": [
    {
      "gid": 1,
      "gname": "商品名称",
      "gprice": 9900,
      "gdetails": "商品描述",
      "types": 1,
      "gpic": "http://nginx地址:端口/images/goods/1.jpg"
    }
  ]
}
```

---

#### 2.2 商品详情
- **Method**: `GET`
- **URL**: `{{baseurl}}/goods/detail?id=1`
- **参数说明**:
  | 参数 | 类型 | 必填 | 说明 |
  |------|------|------|------|
  | id | int | 是 | 商品ID (gid) |
- **成功响应**:
```json
{
  "success": true,
  "code": 200,
  "data": {
    "gid": 1,
    "gname": "商品名称",
    "gprice": 9900,
    "gdetails": "商品描述...",
    "types": 1,
    "gpic": "http://nginx地址:端口/images/goods/1.jpg"
  }
}
```

---

#### 2.3 商品搜索
- **Method**: `GET`
- **URL**: `{{baseurl}}/goods/search?keyword=手机`
- **参数说明**:
  | 参数 | 类型 | 必填 | 说明 |
  |------|------|------|------|
  | keyword | string | 是 | 搜索关键词 |
- **成功响应**:
```json
{
  "success": true,
  "code": 200,
  "data": [
    {
      "gid": 1,
      "gname": "手机",
      ...
    }
  ]
}
```

---

#### 2.4 商品分类列表
- **Method**: `GET`
- **URL**: `{{baseurl}}/goods/category/list`
- **成功响应**:
```json
{
  "success": true,
  "code": 200,
  "data": [
    { "cid": 0, "cname": "未分类" },
    { "cid": 1, "cname": "手机" },
    { "cid": 2, "cname": "平板" },
    { "cid": 3, "cname": "笔记本" },
    { "cid": 4, "cname": "穿戴" },
    { "cid": 5, "cname": "音频" },
    { "cid": 6, "cname": "显示器" },
    { "cid": 7, "cname": "智能家居" }
  ]
}
```
- **说明**：数据来源 `goods.category` 表，与商品 `types` 字段关联

---

## 三、购物车模块 (carts-server)

所有接口路径前缀：`/cart`，**全部需要 Token**

---

#### 3.1 添加购物车
- **Method**: `POST`
- **URL**: `{{baseurl}}/cart/add?gid=1&number=2`
- **Headers**:
  - `Authorization: Bearer {{token}}`
- **参数说明**:
  | 参数 | 类型 | 必填 | 位置 | 说明 |
  |------|------|------|------|------|
  | gid | Integer | 是 | Query | 商品ID |
  | number | Integer | 是 | Query | 数量 |
- **成功响应**:
```json
{
  "success": true,
  "code": 200,
  "data": null
}
```

---

#### 3.2 购物车列表
- **Method**: `GET`
- **URL**: `{{baseurl}}/cart/list`
- **Headers**:
  - `Authorization: Bearer {{token}}`
- **成功响应**:
```json
{
  "success": true,
  "code": 200,
  "data": [
    {
      "id": 1,
      "gid": 1,
      "gname": "商品名称",
      "gpic": "/images/goods/1.jpg",
      "price": 9900,
      "number": 2,
      "uid": 1
    }
  ]
}
```

---

#### 3.3 购物车子集列表（下单时获取选中项）
- **Method**: `POST`
- **URL**: `{{baseurl}}/cart/sublist`
- **Headers**:
  - `Authorization: Bearer {{token}}`
  - `Content-Type: application/json`
- **说明**：uid 由网关从 Token 解析注入请求头，前端无需传参
- **Body** (raw JSON):
```json
[1, 2, 3]
```
- **说明**：Body 为购物车项 ID 数组
- **成功响应**: 同购物车列表格式

---

#### 3.4 更新购物车数量
- **Method**: `POST`
- **URL**: `{{baseurl}}/cart/update?id=1&number=3`
- **Headers**:
  - `Authorization: Bearer {{token}}`
- **说明**：Service 层会校验购物车项归属，非当前用户购物车将忽略操作
- **参数说明**:
  | 参数 | 类型 | 必填 | 位置 | 说明 |
  |------|------|------|------|------|
  | id | Integer | 是 | Query | 购物车项ID |
  | number | Integer | 是 | Query | 新数量 |
- **成功响应**:
```json
{
  "success": true,
  "code": 200
}
```

---

#### 3.5 删除单个购物车项
- **Method**: `DELETE`
- **URL**: `{{baseurl}}/cart/delete?id=1`
- **Headers**:
  - `Authorization: Bearer {{token}}`
- **说明**：Service 层会校验购物车项归属，非当前用户购物车将忽略操作
- **参数说明**:
  | 参数 | 类型 | 必填 | 位置 | 说明 |
  |------|------|------|------|------|
  | id | Integer | 是 | Query | 购物车项ID |
- **成功响应**:
```json
{
  "success": true,
  "code": 200,
  "data": "成功删除购物车"
}
```

---

#### 3.6 批量删除购物车项
- **Method**: `POST`
- **URL**: `{{baseurl}}/cart/deletelist`
- **Headers**:
  - `Authorization: Bearer {{token}}`
  - `Content-Type: application/json`
- **说明**：uid 由网关从 Token 解析注入请求头，前端无需传参
- **Body** (raw JSON):
```json
[1, 2, 3]
```
- **成功响应**:
```json
{
  "success": true,
  "code": 200
}
```

---

## 四、订单模块 (order-server)

所有接口路径前缀：`/order`，**全部需要 Token**

---

#### 4.1 创建订单
- **Method**: `POST`
- **URL**: `{{baseurl}}/order/create`
- **Headers**:
  - `Authorization: Bearer {{token}}`
  - `Content-Type: application/json`
- **Body** (raw JSON):
```json
{
  "selectedCarts": [1, 2, 3],
  "aid": 1,
  "userCouponId": 10
}
```
- **参数说明**:
  | 字段 | 类型 | 必填 | 说明 |
  |------|------|------|------|
  | selectedCarts | Integer[] | 是 | 选中的购物车项ID数组 |
  | aid | Integer | 是 | 收货地址ID（来自 `/address/list`），服务端通过 Feign 校验归属 |
  | userCouponId | Integer | 否 | 用户优惠券ID，不传为不使用优惠券 |
- **成功响应**:
```json
{
  "success": true,
  "code": 200,
  "data": null
}
```
- **失败响应**:
```json
{
  "success": false,
  "code": 500,
  "message": "参数为空"
}
```

---

#### 4.2 订单列表
- **Method**: `POST`
- **URL**: `{{baseurl}}/order/list`
- **Headers**:
  - `Authorization: Bearer {{token}}`
- **成功响应**:
```json
{
  "success": true,
  "code": 200,
  "data": [
    {
      "id": 1,
      "uid": 1,
      "totalPrice": 198.00,
      "discountAmount": 10.00,
      "couponId": 10,
      "status": "待付款",
      "createdTime": "2025-01-01T12:00:00",
      "aid": 1,
      "address": "广东省广州市天河区xxx路xxx号 (张三 13800138000)",
      "orderItems": [
        {
          "gid": 1,
          "gname": "商品名",
          "price": 99.00,
          "number": 2
        }
      ]
    }
  ]
}
```
- **说明**：`address` 字段由后端 `LEFT JOIN address` 表实时生成（格式：`省市+区+详细地址 (收货人 手机号)`），用户修改地址后订单列表会自动联动。`aid` 为收货地址ID。

---

#### 4.3 删除订单
- **Method**: `DELETE`
- **URL**: `{{baseurl}}/order/delete?oid=1`
- **Headers**:
  - `Authorization: Bearer {{token}}`
- **说明**：Service 层会校验订单归属，非当前用户订单将返回"订单不存在或无权操作"
- **参数说明**:
  | 参数 | 类型 | 必填 | 说明 |
  |------|------|------|------|
  | oid | Integer | 是 | 订单ID |
- **成功响应**:
```json
{
  "success": true,
  "code": 200
}
```
- **失败响应**（越权）:
```json
{
  "success": false,
  "code": 500,
  "message": "订单不存在或无权操作"
}
```

---

## 五、收货地址模块 (address-server)

所有接口路径前缀：`/address`，**全部需要 Token**

---

#### 5.1 获取地址列表
- **Method**: `GET`
- **URL**: `{{baseurl}}/address/list`
- **Headers**:
  - `Authorization: Bearer {{token}}`
- **成功响应**:
```json
{
  "success": true,
  "code": 200,
  "data": [
    {
      "id": 1,
      "uid": 1,
      "receiverName": "张三",
      "phone": "13800138000",
      "province": "广东省",
      "city": "广州市",
      "district": "天河区",
      "detail": "xxx路xxx号",
      "isDefault": 1
    }
  ]
}
```

---

#### 5.2 新增地址
- **Method**: `POST`
- **URL**: `{{baseurl}}/address/add`
- **Headers**:
  - `Authorization: Bearer {{token}}`
  - `Content-Type: application/json`
- **说明**：手机号需为 11 位中国大陆手机号（`1[3-9]xxxxxxxxx`），首个地址自动设为默认地址
- **Body** (raw JSON):
```json
{
  "receiverName": "张三",
  "phone": "13800138000",
  "province": "广东省",
  "city": "广州市",
  "district": "天河区",
  "detail": "xxx路xxx号"
}
```
- **成功响应**:
```json
{
  "success": true,
  "code": 200
}
```
- **失败响应**（手机号格式错误）:
```json
{
  "success": false,
  "code": 500,
  "message": "手机号格式不正确"
}
```

---

#### 5.3 更新地址
- **Method**: `PUT`
- **URL**: `{{baseurl}}/address/update`
- **Headers**:
  - `Authorization: Bearer {{token}}`
  - `Content-Type: application/json`
- **说明**：Service 层会校验地址归属，非当前用户地址将返回"地址不存在或无权操作"。手机号同样需要校验（`1[3-9]xxxxxxxxx`）。
- **Body** (raw JSON):
```json
{
  "id": 1,
  "receiverName": "李四",
  "phone": "13900139000",
  "province": "北京市",
  "city": "北京市",
  "district": "朝阳区",
  "detail": "xxx路xxx号"
}
```
- **成功响应**:
```json
{
  "success": true,
  "code": 200
}
```
- **失败响应**（越权）:
```json
{
  "success": false,
  "code": 500,
  "message": "地址不存在或无权操作"
}
```

---

#### 5.4 删除地址
- **Method**: `DELETE`
- **URL**: `{{baseurl}}/address/delete?id=1`
- **Headers**:
  - `Authorization: Bearer {{token}}`
- **说明**：Service 层会校验地址归属，非当前用户地址将返回"地址不存在或无权操作"
- **参数说明**:
  | 参数 | 类型 | 必填 | 说明 |
  |------|------|------|------|
  | id | Integer | 是 | 地址ID |
- **成功响应**:
```json
{
  "success": true,
  "code": 200
}
```

---

#### 5.5 设置默认地址
- **Method**: `PUT`
- **URL**: `{{baseurl}}/address/setdefault?id=2`
- **Headers**:
  - `Authorization: Bearer {{token}}`
- **说明**：Service 层会校验地址归属，非当前用户地址将返回"地址不存在或无权操作"
- **参数说明**:
  | 参数 | 类型 | 必填 | 说明 |
  |------|------|------|------|
  | id | Integer | 是 | 要设为默认的地址ID |
- **成功响应**:
```json
{
  "success": true,
  "code": 200
}
```

---

#### 5.6 地址详情
- **Method**: `GET`
- **URL**: `{{baseurl}}/address/detail?id=1`
- **Headers**:
  - `Authorization: Bearer {{token}}`
- **说明**：主要用于 order-server 下单时 Feign 校验地址归属。Service 层同样做归属校验，非当前用户地址返回"地址不存在或无权操作"。
- **参数说明**:
  | 参数 | 类型 | 必填 | 说明 |
  |------|------|------|------|
  | id | Integer | 是 | 地址ID |
- **成功响应**:
```json
{
  "success": true,
  "code": 200,
  "data": {
    "id": 1,
    "uid": 1,
    "receiverName": "张三",
    "phone": "13800138000",
    "province": "广东省",
    "city": "广州市",
    "district": "天河区",
    "detail": "xxx路xxx号",
    "isDefault": 1
  }
}
```

---

## 六、收藏模块 (favorites-server)

所有接口路径前缀：`/favorite`，**全部需要 Token**

---

#### 6.1 添加收藏
- **Method**: `POST`
- **URL**: `{{baseurl}}/favorite/add?gid=1`
- **Headers**:
  - `Authorization: Bearer {{token}}`
- **参数说明**:
  | 参数 | 类型 | 必填 | 说明 |
  |------|------|------|------|
  | gid | Integer | 是 | 商品ID |
- **成功响应**:
```json
{
  "success": true,
  "code": 200
}
```

---

#### 6.2 获取收藏列表
- **Method**: `GET`
- **URL**: `{{baseurl}}/favorite/list`
- **Headers**:
  - `Authorization: Bearer {{token}}`
- **成功响应**:
```json
{
  "success": true,
  "code": 200,
  "data": [
    {
      "id": 1,
      "gid": 1,
      "uid": 1,
      ...
    }
  ]
}
```

---

#### 6.3 检查是否已收藏
- **Method**: `GET`
- **URL**: `{{baseurl}}/favorite/check?gid=1`
- **Headers**:
  - `Authorization: Bearer {{token}}`
- **参数说明**:
  | 参数 | 类型 | 必填 | 说明 |
  |------|------|------|------|
  | gid | Integer | 是 | 商品ID |
- **成功响应**:
```json
{
  "success": true,
  "code": 200,
  "data": true    // true=已收藏, false=未收藏
}
```

---

#### 6.4 取消收藏（通过商品ID）
- **Method**: `DELETE`
- **URL**: `{{baseurl}}/favorite/delete?gid=1`
- **Headers**:
  - `Authorization: Bearer {{token}}`
- **参数说明**:
  | 参数 | 类型 | 必填 | 说明 |
  |------|------|------|------|
  | gid | Integer | 是 | 商品ID |
- **成功响应**:
```json
{
  "success": true,
  "code": 200
}
```

---

#### 6.5 取消收藏（通过收藏ID）
- **Method**: `DELETE`
- **URL**: `{{baseurl}}/favorite/deleteById?id=1`
- **Headers**:
  - `Authorization: Bearer {{token}}`
- **说明**：Service 层会校验收藏归属，非当前用户收藏将返回"收藏不存在或无权操作"
- **参数说明**:
  | 参数 | 类型 | 必填 | 说明 |
  |------|------|------|------|
  | id | Integer | 是 | 收藏记录ID |
- **成功响应**:
```json
{
  "success": true,
  "code": 200
}
```
- **失败响应**（越权）:
```json
{
  "success": false,
  "code": 500,
  "message": "收藏不存在或无权操作"
}
```

---

## 七、评价模块 (review-server)

路径前缀：`/review`

---

#### 7.1 获取商品评价列表（白名单）
- **Method**: `GET`
- **URL**: `{{baseurl}}/review/goods?gid=1`
- **参数说明**:
  | 参数 | 类型 | 必填 | 说明 |
  |------|------|------|------|
  | gid | Integer | 是 | 商品ID |
- **成功响应**:
```json
{
  "success": true,
  "code": 200,
  "data": [
    {
      "id": 1,
      "uid": 1,
      "gid": 1,
      "oid": 10,
      "content": "好评！",
      "rating": 5,
      "createdTime": "2025-01-01T12:00:00",
      "uname": "testuser"
    }
  ]
}
```

---

#### 7.2 新增评价（需 Token）
- **Method**: `POST`
- **URL**: `{{baseurl}}/review/add`
- **Headers**:
  - `Authorization: Bearer {{token}}`
  - `Content-Type: application/json`
- **Body** (raw JSON):
```json
{
  "gid": 1,
  "oid": 10,
  "content": "非常好用！",
  "rating": 5
}
```
- **成功响应**:
```json
{
  "success": true,
  "code": 200
}
```

---

#### 7.3 获取用户评价列表（需 Token）
- **Method**: `GET`
- **URL**: `{{baseurl}}/review/user`
- **Headers**:
  - `Authorization: Bearer {{token}}`
- **成功响应**: 同 [7.1](#71-获取商品评价列表白名单) 格式

---

#### 7.4 删除评价（需 Token）
- **Method**: `DELETE`
- **URL**: `{{baseurl}}/review/delete?id=1`
- **Headers**:
  - `Authorization: Bearer {{token}}`
- **说明**：Service 层会校验评价归属，非当前用户评价将返回"评价不存在或无权操作"
- **参数说明**:
  | 参数 | 类型 | 必填 | 说明 |
  |------|------|------|------|
  | id | Integer | 是 | 评价ID |
- **成功响应**:
```json
{
  "success": true,
  "code": 200
}
```
- **失败响应**（越权）:
```json
{
  "success": false,
  "code": 500,
  "message": "评价不存在或无权操作"
}
```

---

## 八、优惠券模块 (coupon-server)

路径前缀：`/coupon`

---

### 白名单接口

#### 8.1 全部优惠券列表
- **Method**: `GET`
- **URL**: `{{baseurl}}/coupon/list`
- **成功响应**:
```json
{
  "success": true,
  "code": 200,
  "data": [
    {
      "id": 1,
      "name": "满100减10",
      "type": "满减",
      "conditionAmount": 100.00,
      "reduceAmount": 10.00,
      "totalCount": 100,
      "receivedCount": 30,
      "startTime": "2025-01-01T00:00:00",
      "endTime": "2025-12-31T23:59:59",
      "status": "正常",
      "createdTime": "2025-01-01T00:00:00"
    }
  ]
}
```

---

#### 8.2 可用优惠券列表
- **Method**: `GET`
- **URL**: `{{baseurl}}/coupon/available`
- **成功响应**: 同 [8.1](#81-全部优惠券列表) 格式

---

#### 8.3 优惠券详情
- **Method**: `GET`
- **URL**: `{{baseurl}}/coupon/detail?id=1`
- **参数说明**:
  | 参数 | 类型 | 必填 | 说明 |
  |------|------|------|------|
  | id | Integer | 是 | 优惠券ID |
- **成功响应**: 单个 Coupon 对象

---

### 需认证接口

---

#### 8.4 领取优惠券
- **Method**: `POST`
- **URL**: `{{baseurl}}/coupon/receive?cid=1`
- **Headers**:
  - `Authorization: Bearer {{token}}`
- **参数说明**:
  | 参数 | 类型 | 必填 | 说明 |
  |------|------|------|------|
  | cid | Integer | 是 | 优惠券ID |
- **成功响应**:
```json
{
  "success": true,
  "code": 200
}
```

---

#### 8.5 我的优惠券
- **Method**: `GET`
- **URL**: `{{baseurl}}/coupon/my`
- **Headers**:
  - `Authorization: Bearer {{token}}`
- **成功响应**:
```json
{
  "success": true,
  "code": 200,
  "data": [
    {
      "id": 5,
      "uid": 1,
      "couponId": 1,
      "status": "未使用",
      ...
    }
  ]
}
```

---

#### 8.6 使用优惠券
- **Method**: `POST`
- **URL**: `{{baseurl}}/coupon/use`
- **Headers**:
  - `Authorization: Bearer {{token}}`
  - `Content-Type: application/json`
- **说明**：Service 层会校验用户优惠券归属，非当前用户优惠券将返回"该优惠卷不属于当前用户"
- **Body** (raw JSON):
```json
{
  "ucId": 5,
  "orderAmount": 150.00
}
```
- **参数说明**:
  | 字段 | 类型 | 必填 | 说明 |
  |------|------|------|------|
  | ucId | Integer | 是 | 用户优惠券ID |
  | orderAmount | BigDecimal | 是 | 订单金额 |
- **成功响应**:
```json
{
  "success": true,
  "code": 200,
  "data": 10.00    // 优惠金额
}
```

---

### 管理员接口（需 admin 角色）

---

#### 8.7 创建优惠券
- **Method**: `POST`
- **URL**: `{{baseurl}}/coupon/create`
- **Headers**:
  - `Authorization: Bearer {{token}}`（需管理员 token）
  - `Content-Type: application/json`
- **Body** (raw JSON):
```json
{
  "name": "新用户专享券",
  "type": "满减",
  "conditionAmount": 50.00,
  "reduceAmount": 10.00,
  "totalCount": 200,
  "startTime": "2025-06-01T00:00:00",
  "endTime": "2025-06-30T23:59:59",
  "status": "正常"
}
```
- **成功响应**:
```json
{
  "success": true,
  "code": 200
}
```

---

#### 8.8 更新优惠券
- **Method**: `PUT`
- **URL**: `{{baseurl}}/coupon/update`
- **Headers**:
  - `Authorization: Bearer {{token}}`（需管理员 token）
  - `Content-Type: application/json`
- **说明**：支持部分更新，仅更新传了的字段，未传字段保留原值。`id` 为必填。
- **Body** (raw JSON):
```json
{
  "id": 1,
  "name": "满100减15（调整）",
  "conditionAmount": 100.00,
  "reduceAmount": 15.00,
  "totalCount": 150
}
```
- **成功响应**:
```json
{
  "success": true,
  "code": 200
}
```

---

#### 8.9 删除优惠券
- **Method**: `DELETE`
- **URL**: `{{baseurl}}/coupon/delete?id=1`
- **Headers**:
  - `Authorization: Bearer {{token}}`（需管理员 token）
- **成功响应**:
```json
{
  "success": true,
  "code": 200
}
```

---

## 九、个人中心模块 (user-center-server)

路径前缀：`/usercenter`，**全部需要 Token**

---

#### 9.1 获取个人信息
- **Method**: `GET`
- **URL**: `{{baseurl}}/usercenter/profile`
- **Headers**:
  - `Authorization: Bearer {{token}}`
- **成功响应**:
```json
{
  "success": true,
  "code": 200,
  "data": {
    "uaccount": "xxx",
    "uname": "testuser",
    "usex": "男",
    "urole": "user",
    "uavatar": "/avatar/default.png",
    "uemail": "test@example.com"
  }
}
```

---

#### 9.2 更新个人信息
- **Method**: `PUT`
- **URL**: `{{baseurl}}/usercenter/profile`
- **Headers**:
  - `Authorization: Bearer {{token}}`
  - `Content-Type: application/json`
- **说明**：uid 由网关从 Token 解析注入。若 body 中 `uaccount` 与 Token 中的 uid 不一致，将返回"无权操作"
- **Body** (raw JSON):
```json
{
  "uname": "新昵称",
  "usex": "女",
  "uemail": "newemail@example.com"
}
```
- **成功响应**:
```json
{
  "success": true,
  "code": 200,
  "data": "更新成功"
}
```
- **失败响应**（越权）:
```json
{
  "success": false,
  "code": 500,
  "message": "无权操作"
}
```

---

#### 9.3 修改密码
- **Method**: `PUT`
- **URL**: `{{baseurl}}/usercenter/password`
- **Headers**:
  - `Authorization: Bearer {{token}}`
  - `Content-Type: application/json`
- **说明**：uid 由网关从 Token 解析注入。需传旧密码验证，通过后才更新为新密码
- **Body** (raw JSON):
```json
{
  "oldPassword": "当前密码",
  "newPassword": "新密码"
}
```
- **成功响应**:
```json
{
  "success": true,
  "code": 200,
  "data": "密码修改成功"
}
```
- **失败响应**:
```json
{
  "success": false,
  "code": 500,
  "message": "旧密码错误"
}
```

---

#### 9.4 上传头像
- **Method**: `PUT`
- **URL**: `{{baseurl}}/usercenter/avatar`
- **Headers**:
  - `Authorization: Bearer {{token}}`
  - `Content-Type: application/json`
- **说明**：uid 由网关从 Token 解析注入，无需手动传。**前端需先调用 `POST /obs/upload` 上传头像文件拿到 OBS URL，再调用此接口更新头像字段。** 通过 OBS 上传时 `objectKey` 建议使用 `avatar/` 前缀，如 `avatar/uid_时间戳.jpg`。
- **Body** (raw JSON):
```json
{
  "uavatar": "https://wfw-public.obs.cn-north-4.myhuaweicloud.com/avatar/xxx_timestamp.jpg"
}
```
- **成功响应**:
```json
{
  "success": true,
  "code": 200,
  "data": "头像更新成功"
}
```
- **失败响应**（URL 为空）:
```json
{
  "success": false,
  "code": 500,
  "message": "头像URL不能为空"
}
```

> **前端上传头像完整流程**：
> 1. `POST {{baseurl}}/obs/upload`（form-data: `file=@头像.jpg`, `objectKey=avatar/uid_时间戳.jpg`）→ 拿到 `data.url`
> 2. `PUT {{baseurl}}/usercenter/avatar`（JSON: `{"uavatar": "上一步的url"}`）→ 更新头像成功

---

## 十、管理端模块 (admin-server)

路径前缀：`/admin`，**全部需要 Token 且角色必须为 admin**

> **新增**：所有写操作（新增/修改/删除）均已添加参数校验和数据库存在性校验，校验失败返回 `code: 400`。

---

### 10.1 商品管理

#### 10.1.1 查询所有商品
- **Method**: `GET`
- **URL**: `{{baseurl}}/admin/goods/list`
- **Headers**:
  - `Authorization: Bearer {{token}}`（admin token）
- **成功响应**: 商品列表数组

---

#### 10.1.2 新增商品
- **Method**: `POST`
- **URL**: `{{baseurl}}/admin/goods/add`
- **Headers**:
  - `Authorization: Bearer {{token}}`（admin token）
  - `Content-Type: application/json`
- **Body** (raw JSON):
```json
{
  "gname": "新商品",
  "gprice": 29900,
  "gdetails": "商品描述内容",
  "types": 1,
  "gpic": "/images/goods/new.jpg"
}
```
- **成功响应**:
```json
{
  "success": true,
  "code": 200,
  "data": "添加商品成功"
}
```
- **失败响应**（校验）:
```json
{
  "success": false,
  "code": 400,
  "message": "商品信息不能为空"    // 或 "商品名称不能为空"、"商品价格必须大于0"
}
```

---

#### 10.1.3 更新商品
- **Method**: `PUT`
- **URL**: `{{baseurl}}/admin/goods/update`
- **Headers**:
  - `Authorization: Bearer {{token}}`（admin token）
  - `Content-Type: application/json`
- **Body** (raw JSON):
```json
{
  "gid": 1,
  "gname": "更新后的商品名",
  "gprice": 19900,
  "gdetails": "更新后的描述",
  "types": 2,
  "gpic": "/images/goods/updated.jpg"
}
```
- **成功响应**:
```json
{
  "success": true,
  "code": 200,
  "data": "更新商品成功"
}
```
- **失败响应**（校验）:
```json
{
  "success": false,
  "code": 400,
  "message": "商品信息不能为空"    // 或 "商品ID无效"、"商品名称不能为空"、"商品价格必须大于0"、"商品不存在"
}
```

---

#### 10.1.4 删除商品
- **Method**: `DELETE`
- **URL**: `{{baseurl}}/admin/goods/delete?gid=1`
- **Headers**:
  - `Authorization: Bearer {{token}}`（admin token）
- **成功响应**:
```json
{
  "success": true,
  "code": 200,
  "data": "删除商品成功"
}
```
- **失败响应**（校验）:
```json
{
  "success": false,
  "code": 400,
  "message": "商品ID无效"    // 或 "商品不存在"
}
```

---

### 10.2 订单管理

#### 10.2.1 查询所有订单
- **Method**: `GET`
- **URL**: `{{baseurl}}/admin/order/list`
- **Headers**:
  - `Authorization: Bearer {{token}}`（admin token）
- **成功响应**:
```json
{
  "success": true,
  "code": 200,
  "data": [
    {
      "id": 14,
      "uid": 2,
      "totalPrice": 6999,
      "discountAmount": 0,
      "couponId": null,
      "status": "PAY",
      "createdTime": "2026-06-16T08:17:04.000+00:00"
    }
  ]
}
```
- **说明**：`status` 可选值见下表

---

#### 10.2.2 查询订单详情（含订单项）
- **Method**: `GET`
- **URL**: `{{baseurl}}/admin/order/detail?oid=3`
- **Headers**:
  - `Authorization: Bearer {{token}}`（admin token）
- **参数说明**:
  | 参数 | 类型 | 必填 | 说明 |
  |------|------|------|------|
  | oid | Integer | 是 | 订单ID |
- **成功响应**:
```json
{
  "success": true,
  "code": 200,
  "data": {
    "id": 3,
    "uid": 2,
    "totalPrice": 26997,
    "discountAmount": 0,
    "couponId": null,
    "status": "PAY",
    "createdTime": "2026-06-16T08:17:04.000+00:00",
    "orderItems": [
      { "id": 10, "gid": 19, "gname": "HUAWEI Pura 70 Ultra", "quantity": 2, "price": 9999, "gpic": "..." },
      { "id": 11, "gid": 18, "gname": "HUAWEI Mate 70 Pro", "quantity": 1, "price": 6999, "gpic": "..." }
    ]
  }
}
```
- **失败响应**（校验）:
```json
{
  "success": false,
  "code": 400,
  "message": "订单ID无效"    // 或 "订单不存在"
}
```

---

#### 10.2.3 根据状态查询订单
- **Method**: `GET`
- **URL**: `{{baseurl}}/admin/order/listByStatus?status=PAY`
- **Headers**:
  - `Authorization: Bearer {{token}}`（admin token）
- **参数说明**:
  | 参数 | 类型 | 必填 | 说明 |
  |------|------|------|------|
  | status | String | 是 | 订单状态（英文码见下表） |
- **成功响应**: 订单列表数组
- **失败响应**（校验）:
```json
{
  "success": false,
  "code": 400,
  "message": "订单状态不能为空"
}
```

---

#### 10.2.4 更新订单状态
- **Method**: `PUT`
- **URL**: `{{baseurl}}/admin/order/status?id=14&status=SHIPPED`
- **Headers**:
  - `Authorization: Bearer {{token}}`（admin token）
- **参数说明**:
  | 参数 | 类型 | 必填 | 说明 |
  |------|------|------|------|
  | id | Integer | 是 | 订单ID |
  | status | String | 是 | 新状态（英文码） |
- **成功响应**:
```json
{
  "success": true,
  "code": 200,
  "data": "更新订单状态成功"
}
```
- **失败响应**（校验）:
```json
{
  "success": false,
  "code": 400,
  "message": "订单ID无效"    // 或 "订单状态不能为空"、"订单不存在"
}
```

**订单状态码表**:

| 状态码 | 中文 | 说明 |
|--------|------|------|
| `PAY` | 待付款 | 订单已创建，等待用户支付 |
| `PAID` | 待发货 | 已支付，等待管理员发货 |
| `SHIPPED` | 已发货 | 已出库配送中 |
| `COMPLETED` | 已完成 | 交易完成 |
| `CANCELLED` | 已取消 | 订单已取消 |

---

#### 10.2.5 删除订单
- **Method**: `DELETE`
- **URL**: `{{baseurl}}/admin/order/delete?oid=1`
- **Headers**:
  - `Authorization: Bearer {{token}}`（admin token）
- **成功响应**:
```json
{
  "success": true,
  "code": 200,
  "data": "删除订单成功"
}
```
- **失败响应**（校验）:
```json
{
  "success": false,
  "code": 400,
  "message": "订单ID无效"    // 或 "订单不存在"
}
```

---

### 10.3 用户管理（通过 Feign 调用 auth-server）

#### 10.3.1 获取用户列表
- **Method**: `GET`
- **URL**: `{{baseurl}}/admin/user/list`
- **Headers**:
  - `Authorization: Bearer {{token}}`（admin token）
- **成功响应**: 用户列表数组

---

#### 10.3.2 更新用户信息
- **Method**: `PUT`
- **URL**: `{{baseurl}}/admin/user/update`
- **Headers**:
  - `Authorization: Bearer {{token}}`（admin token）
  - `Content-Type: application/json`
- **Body** (raw JSON):
```json
{
  "uaccount": "2",
  "uname": "新昵称",
  "usex": "男",
  "uemail": "new@example.com",
  "urole": "ADMIN"
}
```
- **参数说明**:
  | 字段 | 类型 | 必填 | 说明 |
  |------|------|------|------|
  | uaccount | String | 是 | 用户账号（用于定位用户） |
  | uname | String | 否 | 新用户名 |
  | usex | String | 否 | 性别（男/女） |
  | uemail | String | 否 | 邮箱 |
  | urole | String | 否 | 角色（admin/user/merchant） |
- **成功响应**:
```json
{
  "success": true,
  "code": 200,
  "data": "更新成功"
}
```
- **失败响应**（校验）:
```json
{
  "success": false,
  "code": 400,
  "message": "用户名不能为空"    // 或 "用户不存在"
}
```
- **说明**：修改前会先通过 Feign 调用 `/auth/findbyusername` 校验用户是否存在

---

#### 10.3.3 删除用户
- **Method**: `DELETE`
- **URL**: `{{baseurl}}/admin/user/delete`
- **Headers**:
  - `Authorization: Bearer {{token}}`（admin token）
  - `Content-Type: application/json`
- **Body** (raw JSON):
```json
{
  "uaccount": "user_account_to_delete"
}
```
- **成功响应**:
```json
{
  "success": true,
  "code": 200,
  "data": "删除用户成功"
}
```
- **失败响应**（校验）:
```json
{
  "success": false,
  "code": 400,
  "message": "用户名不能为空"    // 或 "用户不存在"
}
```

---

## 十一、支付宝支付模块 (pay-server)

路径前缀：`/alipay`，**全部为白名单**

---

#### 11.1 发起支付
- **Method**: `POST`
- **URL**: `{{baseurl}}/alipay/pay`
- **Content-Type**: `application/x-www-form-urlencoded`
- **Body** (form-data / x-www-form-urlencoded):
  | 参数 | 类型 | 必填 | 说明 |
  |------|------|------|------|
  | traceNo | String | 是 | 商户订单号 |
  | totalAmount | double | 是 | 支付金额 |
  | subject | String | 是 | 商品名称/订单标题 |
- **成功响应**:
```json
{
  "success": true,
  "code": 200,
  "data": "<form>支付宝支付表单HTML...</form>"
}
```
- **说明**：返回的 data 是一段支付宝支付表单 HTML，前端渲染后可跳转支付宝沙箱支付页面

---

#### 11.2 支付异步通知回调
- **Method**: `POST`
- **URL**: `{{baseurl}}/alipay/notify`
- **说明**：此接口由支付宝服务器异步回调，不需要手动测试。当支付成功后会更新订单状态为已支付。

---

## 十二、统一响应码说明

| 状态码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 参数校验失败（参数为空/无效/记录不存在等） |
| 500 | 业务失败（如用户名已存在、无权操作等） |
| 401 | 未认证（Token 缺失/无效/过期） |
| 403 | 无权限（非管理员访问 /admin/** 或 /auth/userlist、/auth/deleteuser） |

---

## 十三、越权防护说明

本项目已在 Gateway AuthFilter、Controller、Service 三层实现越权防护：

| 防护层 | 机制 | 说明 |
|--------|------|------|
| Gateway | 角色校验 | `/admin/**`、`/auth/userlist`、`/auth/deleteuser` 仅允许 admin 角色 |
| Gateway | Token 解析 | 从 JWT 解析出 uid 和 role，注入请求头传给下游 |
| Controller | uid 校验 | 若 body 中有 `uaccount` 字段且与 header uid 不一致，拒绝操作 |
| Service | 归属校验 | 写操作执行前查询记录归属，非当前用户直接拒绝 |

---

## 十四、Postman Collection 导入建议

建议按以下文件夹结构组织 Postman Collection：

```
PTU-Mall API Tests
├── 0. 环境变量配置（Pre-request）
├── 1. 认证模块 (Auth)
│   ├── [白名单] POST 注册
│   ├── [白名单] POST 登录
│   ├── [白名单] POST 发送验证码
│   ├── [白名单] POST 邮箱登录
│   ├── POST 退出登录
│   ├── POST 获取用户信息(内部)
│   ├── POST 更新用户信息
│   ├── POST 更新头像
│   ├── POST 修改密码
│   ├── [admin] POST 用户列表
│   ├── [admin] POST 查询用户(内部)
│   └── [admin] POST 删除用户
├── 2. 商品模块 (Goods) [白名单]
│   ├── GET 商品列表
│   ├── GET 商品详情
│   ├── GET 商品搜索
│   └── GET 分类列表
├── 3. 购物车模块 (Cart) [需认证]
│   ├── POST 添加购物车
│   ├── GET 购物车列表
│   ├── POST 子集列表
│   ├── POST 更新数量
│   ├── DELETE 删除单项
│   └── POST 批量删除
├── 4. 订单模块 (Order) [需认证]
│   ├── POST 创建订单
│   ├── GET 订单列表
│   └── DELETE 删除订单
├── 5. 地址模块 (Address) [需认证]
│   ├── GET 地址列表
│   ├── POST 新增地址
│   ├── PUT 更新地址
│   ├── DELETE 删除地址
│   └── PUT 设置默认
├── 6. 收藏模块 (Favorite) [需认证]
│   ├── POST 添加收藏
│   ├── GET 收藏列表
│   ├── GET 检查收藏
│   ├── DELETE 取消收藏(商品ID)
│   └── DELETE 取消收藏(收藏ID)
├── 7. 评价模块 (Review)
│   ├── [白名单] GET 商品评价
│   ├── POST 新增评价
│   ├── GET 用户评价
│   └── DELETE 删除评价
├── 8. 优惠券模块 (Coupon)
│   ├── [白名单] GET 全部列表
│   ├── [白名单] GET 可用列表
│   ├── [白名单] GET 详情
│   ├── POST 领取
│   ├── GET 我的优惠券
│   ├── POST 使用
│   ├── [管理员] POST 创建
│   ├── [管理员] PUT 更新
│   └── [管理员] DELETE 删除
├── 9. 个人中心 (UserCenter) [需认证]
│   ├── GET 个人信息
│   ├── PUT 更新信息
│   ├── PUT 修改密码
│   └── PUT 上传头像
├── 10. 管理端 (Admin) [需admin角色]
│   ├── 商品管理
│   │   ├── GET 列表
│   │   ├── POST 新增
│   │   ├── PUT 更新
│   │   └── DELETE 删除
│   ├── 订单管理
│   │   ├── GET 列表
│   │   ├── GET 详情(含订单项)
│   │   ├── GET 按状态查询
│   │   ├── PUT 更新状态
│   │   └── DELETE 删除
│   └── 用户管理
│       ├── GET 列表
│       ├── PUT 更新
│       └── DELETE 删除
└── 11. 支付模块 (Pay) [白名单]
    ├── POST 发起支付
    └── (POST 异步回调-服务端)
```
