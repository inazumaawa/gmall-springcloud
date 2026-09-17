-- =====================================================================
-- 项目新增功能数据库迁移脚本
-- 库：account（address 服务 与 customer_ws 客服服务均连接该库）
-- 新增表：
--   1. logistics_track  物流轨迹表（并入 address 模块）
--   2. chat_session     客服会话表（customer_ws 模块）
--   3. chat_message     客服消息表（customer_ws 模块）
-- =====================================================================

USE account;

-- 物流轨迹表
CREATE TABLE IF NOT EXISTS logistics_track (
  id          INT          NOT NULL AUTO_INCREMENT COMMENT '物流轨迹ID',
  order_id    INT          NOT NULL                COMMENT '订单ID',
  address_id  INT          DEFAULT NULL            COMMENT '收货地址ID',
  status      VARCHAR(30)  NOT NULL                COMMENT '物流状态',
  location    VARCHAR(100) NOT NULL                COMMENT '当前位置',
  description VARCHAR(255) NOT NULL                COMMENT '轨迹描述',
  track_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '轨迹时间',
  PRIMARY KEY (id),
  KEY idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物流轨迹表';

-- 客服会话表
CREATE TABLE IF NOT EXISTS chat_session (
  id           INT         NOT NULL AUTO_INCREMENT COMMENT '会话ID',
  uid          INT         NOT NULL                COMMENT '用户ID',
  status       VARCHAR(20) NOT NULL DEFAULT 'OPEN' COMMENT '会话状态 OPEN=进行中 CLOSED=已结束',
  created_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_uid (uid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客服会话表';

-- 客服消息表
CREATE TABLE IF NOT EXISTS chat_message (
  id          INT          NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  session_id  INT          NOT NULL                COMMENT '会话ID',
  sender_id   INT          NOT NULL                COMMENT '发送人ID',
  sender_role VARCHAR(20)  NOT NULL                COMMENT '发送人角色 user=用户 admin=客服',
  content     VARCHAR(500) NOT NULL                COMMENT '消息内容',
  send_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  PRIMARY KEY (id),
  KEY idx_session_id (session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客服消息表';
