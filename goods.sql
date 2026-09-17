



/*
Navicat MySQL Data Transfer

Source Server         : jinsai
Source Server Version : 80028
Source Host           : localhost:3306
Source Database       : goods

Target Server Type    : MYSQL
Target Server Version : 80028
File Encoding         : 65001

Date: 2026-06-20 11:35:28
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for cart
-- ----------------------------
DROP TABLE IF EXISTS `cart`;
CREATE TABLE `cart` (
  `id` int NOT NULL AUTO_INCREMENT,
  `gname` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `number` int NOT NULL,
  `price` int NOT NULL,
  `gid` int NOT NULL,
  `uid` int NOT NULL,
  `gpic` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=45 DEFAULT CHARSET=utf8mb3 COLLATE=utf8_bin;

-- ----------------------------
-- Records of cart
-- ----------------------------
INSERT INTO `cart` VALUES ('16', 'HUAWEI Mate 70 Pro', '1', '6999', '18', '4', 'http://127.0.0.1:88https://wfw-public.obs.cn-north-4.myhuaweicloud.com/goods/HUAWEI Mate 70 Pro.pong');

-- ----------------------------
-- Table structure for category
-- ----------------------------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
  `cid` int NOT NULL COMMENT '分类ID',
  `cname` varchar(50) NOT NULL COMMENT '分类名称',
  PRIMARY KEY (`cid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品分类表';

-- ----------------------------
-- Records of category
-- ----------------------------
INSERT INTO `category` VALUES ('0', '未分类');
INSERT INTO `category` VALUES ('1', '手机');
INSERT INTO `category` VALUES ('2', '平板');
INSERT INTO `category` VALUES ('3', '笔记本');
INSERT INTO `category` VALUES ('4', '穿戴');
INSERT INTO `category` VALUES ('5', '音频');
INSERT INTO `category` VALUES ('6', '显示器');
INSERT INTO `category` VALUES ('7', '智能家居');
INSERT INTO `category` VALUES ('8', '测试');

-- ----------------------------
-- Table structure for coupon
-- ----------------------------
DROP TABLE IF EXISTS `coupon`;
CREATE TABLE `coupon` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '优惠卷ID',
  `name` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '优惠卷名称',
  `type` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '类型:full_reduction=满减,discount=折扣',
  `condition_amount` decimal(10,2) DEFAULT NULL COMMENT '满减条件金额',
  `reduce_amount` decimal(10,2) NOT NULL COMMENT '优惠金额/折扣率',
  `total_count` int NOT NULL DEFAULT '0' COMMENT '发放总量',
  `received_count` int NOT NULL DEFAULT '0' COMMENT '已领取数量',
  `start_time` datetime NOT NULL COMMENT '有效期开始',
  `end_time` datetime NOT NULL COMMENT '有效期结束',
  `status` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT 'active' COMMENT '状态:active=有效,expired=过期',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb3 COLLATE=utf8_bin;

-- ----------------------------
-- Records of coupon
-- ----------------------------
INSERT INTO `coupon` VALUES ('1', '满100减15（调整）', '满减', '100.00', '15.00', '150', '6', '2025-06-01 08:00:00', '2026-07-01 07:59:59', 'active', '2026-06-04 09:18:52');

-- ----------------------------
-- Table structure for favorite
-- ----------------------------
DROP TABLE IF EXISTS `favorite`;
CREATE TABLE `favorite` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
  `uid` int NOT NULL COMMENT '用户账号',
  `gid` int NOT NULL COMMENT '商品ID',
  `created_time` datetime DEFAULT NULL COMMENT '收藏时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb3 COLLATE=utf8_bin;

-- ----------------------------
-- Records of favorite
-- ----------------------------
INSERT INTO `favorite` VALUES ('5', '2', '24', '2026-06-04 17:13:03');
INSERT INTO `favorite` VALUES ('6', '2', '27', '2026-06-04 17:13:05');
INSERT INTO `favorite` VALUES ('7', '2', '30', '2026-06-04 17:13:08');
INSERT INTO `favorite` VALUES ('10', '2', '20', '2026-06-12 09:35:22');
INSERT INTO `favorite` VALUES ('11', '1', '20', '2026-06-18 09:28:30');
INSERT INTO `favorite` VALUES ('12', '1', '23', '2026-06-18 09:28:36');
INSERT INTO `favorite` VALUES ('13', '1', '31', '2026-06-18 09:28:38');

-- ----------------------------
-- Table structure for goods
-- ----------------------------
DROP TABLE IF EXISTS `goods`;
CREATE TABLE `goods` (
  `gid` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `gname` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '商品名称',
  `gpic` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '商品图片地址',
  `gdetails` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '商品介绍',
  `gprice` int NOT NULL COMMENT '商品价格',
  `types` int NOT NULL COMMENT '商品类别',
  PRIMARY KEY (`gid`)
) ENGINE=InnoDB AUTO_INCREMENT=36 DEFAULT CHARSET=utf8mb3 COLLATE=utf8_bin;

-- ----------------------------
-- Records of goods
-- ----------------------------
INSERT INTO `goods` VALUES ('18', 'HUAWEI Mate 70 Pro', 'https://wfw-public.obs.cn-north-4.myhuaweicloud.com/goods/HUAWEI Mate 70 Pro.png', '华为 Mate 70 Pro 5G 智能手机 | 麒麟9100芯片 | 鸿蒙OS 5.0 | 6000万像素徕卡影像 | 5000mAh大电池', '6999', '1');
INSERT INTO `goods` VALUES ('19', 'HUAWEI Pura 70 Ultra', 'https://wfw-public.obs.cn-north-4.myhuaweicloud.com/goods/HUAWEI Pura 70 Ultra.png', '华为 Pura 70 Ultra | 超聚光伸缩摄像头 | 麒麟9010处理器 | 6.8英寸OLED屏幕 | IP68防水', '9999', '1');
INSERT INTO `goods` VALUES ('20', 'HUAWEI Mate X6', 'https://wfw-public.obs.cn-north-4.myhuaweicloud.com/goods/HUAWEI Mate X6.png', '华为 Mate X6 折叠屏 | 8英寸内屏+6.5英寸外屏 | 玄武水滴铰链 | 双向北斗卫星消息', '12999', '1');
INSERT INTO `goods` VALUES ('21', 'HUAWEI nova 13 Pro', 'https://wfw-public.obs.cn-north-4.myhuaweicloud.com/goods/HUAWEI nova 13 Pro.png', '华为 nova 13 Pro | 前置6000万人像镜头 | 麒麟8000芯片 | 100W超级快充 | 轻薄时尚设计', '3499', '1');
INSERT INTO `goods` VALUES ('22', 'HUAWEI MatePad Pro 13.2', 'https://wfw-public.obs.cn-north-4.myhuaweicloud.com/goods/HUAWEI MatePad Pro 13.2.png', '华为 MatePad Pro 13.2英寸 | OLED柔性屏 | 天生会画 | 多屏协同 | M-Pencil 3代', '5499', '2');
INSERT INTO `goods` VALUES ('23', 'HUAWEI MatePad Air', 'https://wfw-public.obs.cn-north-4.myhuaweicloud.com/goods/HUAWEI MatePad Air.png', '华为 MatePad Air 12英寸 | 轻薄金属机身 | 144Hz高刷屏 | PC级WPS | 学生办公利器', '2999', '2');
INSERT INTO `goods` VALUES ('24', 'HUAWEI MateBook X Pro', 'https://wfw-public.obs.cn-north-4.myhuaweicloud.com/goods/HUAWEI MateBook X Pro.png', '华为 MateBook X Pro 2024 | 3.1K OLED触控屏 | 酷睿Ultra 9 | 超级终端 | 1.26kg轻薄机身', '11999', '3');
INSERT INTO `goods` VALUES ('25', 'HUAWEI MateBook D 16', 'https://wfw-public.obs.cn-north-4.myhuaweicloud.com/goods/HUAWEI MateBook D 16.png', '华为 MateBook D 16 | 16英寸护眼全面屏 | 酷睿i5-13500H | 超级终端 | 多设备协同', '5499', '3');
INSERT INTO `goods` VALUES ('26', 'HUAWEI MateBook 14', 'https://wfw-public.obs.cn-north-4.myhuaweicloud.com/goods/HUAWEI MateBook 14.jpg', '华为 MateBook 14 2024 | 2.8K OLED触控屏 | 酷睿Ultra 7 | 金属机身 | 学生办公首选', '6799', '3');
INSERT INTO `goods` VALUES ('27', 'HUAWEI WATCH GT 5 Pro', 'https://wfw-public.obs.cn-north-4.myhuaweicloud.com/goods/HUAWEI WATCH GT 5 Pro.jpg', '华为 WATCH GT 5 Pro | 钛金属表壳 | 高尔夫/潜水模式 | 双频GPS | 14天超长续航', '2788', '4');
INSERT INTO `goods` VALUES ('28', 'HUAWEI WATCH Ultimate', 'https://wfw-public.obs.cn-north-4.myhuaweicloud.com/goods/HUAWEI WATCH Ultimate.jpg', '华为 WATCH Ultimate | 非晶锆合金表壳 | 100米潜水级防水 | 卫星通信 | 探险模式', '5999', '4');
INSERT INTO `goods` VALUES ('29', 'HUAWEI FreeBuds Pro 4', 'https://wfw-public.obs.cn-north-4.myhuaweicloud.com/goods/HUAWEI FreeBuds Pro 4.jpg', '华为 FreeBuds Pro 4 | 智慧动态降噪3.0 | 高清空间音频 | 星闪连接 | 36小时续航', '1449', '5');
INSERT INTO `goods` VALUES ('30', 'HUAWEI FreeBuds 6i', 'https://wfw-public.obs.cn-north-4.myhuaweicloud.com/goods/HUAWEI FreeBuds 6i.jpg', '华为 FreeBuds 6i | 主动降噪', '499', '0');
INSERT INTO `goods` VALUES ('31', 'HUAWEI Sound X 4', 'https://wfw-public.obs.cn-north-4.myhuaweicloud.com/goods/HUAWEI Sound X 4.jpg', '华为 Sound X 4 | Devialet联名 | 帝瓦雷8单元 | 空间音频 | 一碰传音 | 家居智能音箱', '2499', '5');
INSERT INTO `goods` VALUES ('32', 'HUAWEI Mate 60 Pro', 'https://wfw-public.obs.cn-north-4.myhuaweicloud.com/goods/HUAWEI Mate 60 Pro.jpg', '华为 Mate 60 Pro+ | 卫星通话/北斗卫星消息 | 玄武架构 | 昆仑玻璃 | 全焦段超清影像', '8999', '1');
INSERT INTO `goods` VALUES ('33', 'HUAWEI MateView SE', 'https://wfw-public.obs.cn-north-4.myhuaweicloud.com/goods/HUAWEI MateView SE.jpg', '华为 MateView SE 27英寸 | 2K IPS显示器 | 德国莱茵护眼认证 | Type-C 65W充电 | 超窄边框', '1399', '3');

-- ----------------------------
-- Table structure for orders
-- ----------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `uid` int NOT NULL COMMENT '用户ID',
  `total_price` decimal(10,2) NOT NULL COMMENT '实付金额',
  `discount_amount` decimal(10,2) DEFAULT '0.00' COMMENT '优惠卷抵扣金额',
  `coupon_id` int DEFAULT NULL COMMENT '使用的用户优惠卷记录ID(user_coupon.id)',
  `status` varchar(20) NOT NULL DEFAULT 'PAY' COMMENT 'PAY 待支付 / PAID 已支付 / DELIVERED 已发货 / DONE 已完成 / CANCEL 已取消',
  `created_time` datetime NOT NULL COMMENT '创建时间',
  `aid` int NOT NULL COMMENT '收货地址ID，关联address表',
  PRIMARY KEY (`id`),
  KEY `idx_uid` (`uid`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=35 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单表';

-- ----------------------------
-- Records of orders
-- ----------------------------
INSERT INTO `orders` VALUES ('3', '2', '26997.00', '0.00', null, 'CANCELLED', '2026-06-10 10:59:00', '5');
INSERT INTO `orders` VALUES ('5', '2', '77994.00', '0.00', null, 'PAY', '2026-06-12 09:35:59', '5');
INSERT INTO `orders` VALUES ('6', '2', '23998.00', '0.00', null, 'PAY', '2026-06-16 16:15:35', '5');
INSERT INTO `orders` VALUES ('7', '2', '11999.00', '0.00', null, 'PAY', '2026-06-16 16:15:41', '5');
INSERT INTO `orders` VALUES ('10', '2', '6999.00', '0.00', null, 'PAY', '2026-06-16 16:16:49', '6');
INSERT INTO `orders` VALUES ('11', '2', '6999.00', '0.00', null, 'PAY', '2026-06-16 16:16:54', '5');
INSERT INTO `orders` VALUES ('12', '2', '13998.00', '0.00', null, 'PAY', '2026-06-16 16:16:57', '5');
INSERT INTO `orders` VALUES ('14', '2', '6999.00', '0.00', null, 'COMPLETED', '2026-06-16 16:17:04', '5');
INSERT INTO `orders` VALUES ('15', '2', '6999.00', '0.00', null, 'PAID', '2026-06-16 16:17:04', '5');
INSERT INTO `orders` VALUES ('16', '2', '6999.00', '0.00', null, 'PAY', '2026-06-16 16:17:04', '5');
INSERT INTO `orders` VALUES ('26', '1', '11999.00', '15.00', '1', 'COMPLETED', '2026-06-18 10:17:48', '8');
INSERT INTO `orders` VALUES ('32', '1', '5499.00', '15.00', '5', 'SHIPPED', '2026-06-18 11:06:16', '8');
INSERT INTO `orders` VALUES ('33', '2', '12999.00', '0.00', null, 'PAY', '2026-06-18 11:37:16', '5');
INSERT INTO `orders` VALUES ('34', '2', '9999.00', '15.00', '6', 'PAID', '2026-06-18 11:37:29', '5');

-- ----------------------------
-- Table structure for order_item
-- ----------------------------
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `oid` bigint NOT NULL,
  `gid` int NOT NULL,
  `quantity` int NOT NULL,
  `price` decimal(10,2) NOT NULL,
  `gname` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `gpic` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=43 DEFAULT CHARSET=utf8mb3 COLLATE=utf8_bin;

-- ----------------------------
-- Records of order_item
-- ----------------------------
INSERT INTO `order_item` VALUES ('10', '3', '19', '2', '9999.00', 'HUAWEI Pura 70 Ultra', 'http://127.0.0.1:88https://img.alicdn.com/imgextra/i1/2216903275609/O1CN01A9jL3P1vVTVgZRQTy_!!2216903275609.jpg');
INSERT INTO `order_item` VALUES ('11', '3', '18', '1', '6999.00', 'HUAWEI Mate 70 Pro', 'http://127.0.0.1:88https://wfw-public.obs.cn-north-4.myhuaweicloud.com/goods/HUAWEI Mate 70 Pro.pong');
INSERT INTO `order_item` VALUES ('13', '5', '20', '6', '12999.00', 'HUAWEI Mate X6', 'http://127.0.0.1:88https://wfw-public.obs.cn-north-4.myhuaweicloud.com/goods/HUAWEI Mate X6.png');
INSERT INTO `order_item` VALUES ('14', '6', '24', '2', '11999.00', 'HUAWEI MateBook X Pro', 'http://127.0.0.1:88https://wfw-public.obs.cn-north-4.myhuaweicloud.com/goods/HUAWEI MateBook X Pro.png');
INSERT INTO `order_item` VALUES ('15', '7', '24', '1', '11999.00', 'HUAWEI MateBook X Pro', 'http://127.0.0.1:88https://wfw-public.obs.cn-north-4.myhuaweicloud.com/goods/HUAWEI MateBook X Pro.png');
INSERT INTO `order_item` VALUES ('18', '10', '18', '1', '6999.00', 'HUAWEI Mate 70 Pro', 'http://127.0.0.1:88https://wfw-public.obs.cn-north-4.myhuaweicloud.com/goods/HUAWEI Mate 70 Pro.png');
INSERT INTO `order_item` VALUES ('19', '11', '18', '1', '6999.00', 'HUAWEI Mate 70 Pro', 'http://127.0.0.1:88https://wfw-public.obs.cn-north-4.myhuaweicloud.com/goods/HUAWEI Mate 70 Pro.png');
INSERT INTO `order_item` VALUES ('20', '12', '18', '2', '6999.00', 'HUAWEI Mate 70 Pro', 'http://127.0.0.1:88https://wfw-public.obs.cn-north-4.myhuaweicloud.com/goods/HUAWEI Mate 70 Pro.png');
INSERT INTO `order_item` VALUES ('22', '14', '18', '1', '6999.00', 'HUAWEI Mate 70 Pro', 'http://127.0.0.1:88https://wfw-public.obs.cn-north-4.myhuaweicloud.com/goods/HUAWEI Mate 70 Pro.png');
INSERT INTO `order_item` VALUES ('23', '15', '18', '1', '6999.00', 'HUAWEI Mate 70 Pro', 'http://127.0.0.1:88https://wfw-public.obs.cn-north-4.myhuaweicloud.com/goods/HUAWEI Mate 70 Pro.png');
INSERT INTO `order_item` VALUES ('24', '16', '18', '1', '6999.00', 'HUAWEI Mate 70 Pro', 'http://127.0.0.1:88https://wfw-public.obs.cn-north-4.myhuaweicloud.com/goods/HUAWEI Mate 70 Pro.png');
INSERT INTO `order_item` VALUES ('34', '26', '24', '1', '11999.00', 'HUAWEI MateBook X Pro', 'http://127.0.0.1:88https://wfw-public.obs.cn-north-4.myhuaweicloud.com/goods/HUAWEI MateBook X Pro.png');
INSERT INTO `order_item` VALUES ('40', '32', '22', '1', '5499.00', 'HUAWEI MatePad Pro 13.2', 'http://127.0.0.1:88https://wfw-public.obs.cn-north-4.myhuaweicloud.com/goods/HUAWEI MatePad Pro 13.2.png');
INSERT INTO `order_item` VALUES ('41', '33', '20', '1', '12999.00', 'HUAWEI Mate X6', 'http://127.0.0.1:88https://wfw-public.obs.cn-north-4.myhuaweicloud.com/goods/HUAWEI Mate X6.png');
INSERT INTO `order_item` VALUES ('42', '34', '19', '1', '9999.00', 'HUAWEI Pura 70 Ultra', 'http://127.0.0.1:88https://wfw-public.obs.cn-north-4.myhuaweicloud.com/goods/HUAWEI Pura 70 Ultra.png');

-- ----------------------------
-- Table structure for review
-- ----------------------------
DROP TABLE IF EXISTS `review`;
CREATE TABLE `review` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '评价ID',
  `uid` int NOT NULL COMMENT '用户账号',
  `gid` int NOT NULL COMMENT '商品ID',
  `oid` int DEFAULT NULL COMMENT '订单ID',
  `content` varchar(500) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT '评价内容',
  `rating` int DEFAULT '5' COMMENT '评分(1-5)',
  `created_time` datetime DEFAULT NULL COMMENT '评价时间',
  `uname` varchar(50) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT '评价用户昵称',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb3 COLLATE=utf8_bin;

-- ----------------------------
-- Records of review
-- ----------------------------
INSERT INTO `review` VALUES ('4', '2', '20', null, '12312321', '5', '2026-06-10 15:44:12', null);

-- ----------------------------
-- Table structure for user_coupon
-- ----------------------------
DROP TABLE IF EXISTS `user_coupon`;
CREATE TABLE `user_coupon` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `uid` int NOT NULL COMMENT '用户账号',
  `cid` int NOT NULL COMMENT '优惠卷ID',
  `status` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT 'unused' COMMENT '状态:unused=未使用,used=已使用,expired=已过期',
  `get_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
  `use_time` datetime DEFAULT NULL COMMENT '使用时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb3 COLLATE=utf8_bin;

-- ----------------------------
-- Records of user_coupon
-- ----------------------------
INSERT INTO `user_coupon` VALUES ('1', '1', '1', 'used', '2026-06-18 09:26:39', '2026-06-18 09:29:23');
INSERT INTO `user_coupon` VALUES ('2', '1', '1', 'used', '2026-06-18 09:29:42', '2026-06-18 09:46:11');
INSERT INTO `user_coupon` VALUES ('3', '1', '1', 'used', '2026-06-18 09:49:06', '2026-06-18 09:49:17');
INSERT INTO `user_coupon` VALUES ('4', '1', '1', 'used', '2026-06-18 10:17:24', '2026-06-18 10:52:10');
INSERT INTO `user_coupon` VALUES ('5', '1', '1', 'used', '2026-06-18 10:52:18', '2026-06-18 11:06:48');
INSERT INTO `user_coupon` VALUES ('6', '2', '1', 'used', '2026-06-18 11:37:02', '2026-06-18 11:39:05');
