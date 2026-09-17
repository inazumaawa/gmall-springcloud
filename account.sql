/*
Navicat MySQL Data Transfer

Source Server         : jinsai
Source Server Version : 80028
Source Host           : localhost:3306
Source Database       : account

Target Server Type    : MYSQL
Target Server Version : 80028
File Encoding         : 65001

Date: 2026-06-20 11:35:41
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for address
-- ----------------------------
DROP TABLE IF EXISTS `address`;
CREATE TABLE `address` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '地址ID',
  `uid` int NOT NULL COMMENT '用户账号',
  `receiver_name` varchar(50) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT '收件人姓名',
  `phone` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT '联系电话',
  `province` varchar(50) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT '省',
  `city` varchar(50) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT '市',
  `district` varchar(50) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT '区',
  `detail` varchar(200) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT '详细地址',
  `is_default` tinyint DEFAULT '0' COMMENT '是否默认地址:1=默认,0=非默认',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb3 COLLATE=utf8_bin;

-- ----------------------------
-- Records of address
-- ----------------------------
INSERT INTO `address` VALUES ('5', '2', '李四', '13900139000', '北京市', '北京市', '朝阳区', '1路1号', '1');
INSERT INTO `address` VALUES ('6', '2', '张三', '13800138001', '广东省', '广州市', '天河区', 'x天河路1号', '0');
INSERT INTO `address` VALUES ('7', '2', '', '13800138088', '广东省', '深圳市', '南山区', '高新路1号华为大厦', '0');
INSERT INTO `address` VALUES ('8', '1', 'a', '13461572312', '1', '1', '1', '1', '0');
INSERT INTO `address` VALUES ('9', '1', '2', '13461572312', '2', '2', '2', '22222', '1');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `uaccount` int NOT NULL AUTO_INCREMENT COMMENT '账号',
  `upassword` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '密码',
  `uname` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '姓名',
  `usex` varchar(50) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '性别',
  `urole` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT 'user' COMMENT '角色:user=普通用户,admin=管理员',
  `uavatar` varchar(500) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT '头像URL(华为云OBS)',
  `uemail` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT '邮箱(用于验证码登录)',
  PRIMARY KEY (`uaccount`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb3 COLLATE=utf8_bin;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES ('1', 'admin', 'admin', '女', 'admin', 'https://wfw-public.obs.cn-north-4.myhuaweicloud.com/avatar/1.jpg', '1464639636@qq.com');
INSERT INTO `user` VALUES ('2', 'test', 'test', '男', 'user', 'https://wfw-public.obs.cn-north-4.myhuaweicloud.com/avatar/2.jpg', 'test_updated@example.com');
INSERT INTO `user` VALUES ('4', '123456', 'testuser2', '男', 'user', null, 'test2@example.com');
