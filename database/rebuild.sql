-- ============================================================
-- HmH-combined 数据库重建脚本
-- 数据库: db_reimbursement
-- 生成时间: 2026-06-18
-- 说明: 根据项目实体类逆向生成，保留原始字段命名风格
-- ============================================================

-- 1. 创建数据库
CREATE DATABASE IF NOT EXISTS `hmh`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE `hmh`;

-- ============================================================
-- 2. 用户表 t_user
-- 注意: userName / trueName / roleName 使用驼峰命名（与实体类 @TableField 一致）
-- ============================================================
DROP TABLE IF EXISTS `t_user`;
CREATE TABLE `t_user` (
    `id`        INT          NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `userName`  VARCHAR(50)  NOT NULL COMMENT '用户名',
    `password`  VARCHAR(100) NOT NULL COMMENT '密码',
    `trueName`  VARCHAR(50)  DEFAULT NULL COMMENT '真实姓名',
    `email`     VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `phone`     VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    `roleName`  VARCHAR(20)  NOT NULL COMMENT '角色: system admin / manager / employee',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 插入默认管理员账号
INSERT INTO `t_user` (`userName`, `password`, `trueName`, `email`, `phone`, `roleName`)
VALUES ('admin', 'admin123', '系统管理员', 'admin@hmh.com', '13800000000', 'system admin');

-- ============================================================
-- 3. 报销类型表 type
-- 注意: 实体类无 @TableName 注解，默认表名为类名小写
-- ============================================================
DROP TABLE IF EXISTS `type`;
CREATE TABLE `type` (
    `id`   INT         NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `type` VARCHAR(50) NOT NULL COMMENT '类型名称',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报销类型表';

-- 插入常用报销类型
INSERT INTO `type` (`type`) VALUES
    ('差旅费'),
    ('交通费'),
    ('餐饮费'),
    ('办公用品'),
    ('通讯费'),
    ('招待费'),
    ('培训费'),
    ('其他');

-- ============================================================
-- 4. 报销申请表 reimburse
-- ============================================================
DROP TABLE IF EXISTS `reimburse`;
CREATE TABLE `reimburse` (
    `id`          INT           NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `sq_user_id`  INT           DEFAULT NULL COMMENT '申请人ID (FK -> t_user.id)',
    `sp_user_id`  INT           DEFAULT NULL COMMENT '审批人ID (FK -> t_user.id)',
    `c_type`      INT           DEFAULT NULL COMMENT '报销分类类型',
    `money`       DECIMAL(10,2) DEFAULT NULL COMMENT '报销金额',
    `reason`      VARCHAR(500)  DEFAULT NULL COMMENT '报销事由',
    `deny_reason` VARCHAR(500)  DEFAULT NULL COMMENT '驳回原因',
    `status`      INT           DEFAULT NULL COMMENT '报销状态',
    `type`        INT           DEFAULT NULL COMMENT '报销类型',
    `type_id`     INT           DEFAULT NULL COMMENT '费用类型ID (FK -> type.id)',
    `sp_name`     VARCHAR(50)   DEFAULT NULL COMMENT '审批人姓名(冗余)',
    `sq_name`     VARCHAR(50)   DEFAULT NULL COMMENT '申请人姓名(冗余)',
    `type_name`   VARCHAR(50)   DEFAULT NULL COMMENT '类型名称(冗余)',
    `create_time` DATETIME      DEFAULT NULL COMMENT '创建时间',
    `end_time`    DATETIME      DEFAULT NULL COMMENT '完成时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报销申请表';

-- ============================================================
-- 5. 发票表 t_invoice
-- ============================================================
DROP TABLE IF EXISTS `t_invoice`;
CREATE TABLE `t_invoice` (
    `id`            INT           NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `invoice_no`    VARCHAR(50)   DEFAULT NULL COMMENT '发票号码',
    `invoice_code`  VARCHAR(50)   DEFAULT NULL COMMENT '发票代码',
    `amount`        DECIMAL(10,2) DEFAULT NULL COMMENT '发票金额',
    `invoice_date`  DATETIME      DEFAULT NULL COMMENT '开票日期',
    `seller`        VARCHAR(200)  DEFAULT NULL COMMENT '销售方名称',
    `image_url`     VARCHAR(500)  DEFAULT NULL COMMENT '发票图片URL',
    `status`        INT           DEFAULT 0 COMMENT '状态: 0=未使用, 1=已关联',
    `reimburse_id`  INT           DEFAULT NULL COMMENT '关联报销单ID (FK -> reimburse.id)',
    `reimburse_no`  VARCHAR(50)   DEFAULT NULL COMMENT '报销单编号(冗余)',
    `create_user_id` INT          DEFAULT NULL COMMENT '上传用户ID (FK -> t_user.id)',
    `create_time`   DATETIME      DEFAULT NULL COMMENT '创建时间',
    `update_time`   DATETIME      DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发票表';

-- ============================================================
-- 6. 报销规则表 t_reimburse_rule
-- ============================================================
DROP TABLE IF EXISTS `t_reimburse_rule`;
CREATE TABLE `t_reimburse_rule` (
    `id`             INT           NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `type_id`        INT           DEFAULT NULL COMMENT '费用类型ID (FK -> type.id)',
    `type_name`      VARCHAR(50)   DEFAULT NULL COMMENT '类型名称(冗余)',
    `max_amount`     DECIMAL(10,2) DEFAULT NULL COMMENT '最大报销限额',
    `warning_amount` DECIMAL(10,2) DEFAULT NULL COMMENT '预警金额',
    `description`    VARCHAR(500)  DEFAULT NULL COMMENT '规则说明',
    `status`         INT           DEFAULT 1 COMMENT '状态: 0=禁用, 1=启用',
    `create_time`    DATETIME      DEFAULT NULL COMMENT '创建时间',
    `update_time`    DATETIME      DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报销规则表';

-- ============================================================
-- 7. 公告表 announcement
-- ============================================================
DROP TABLE IF EXISTS `announcement`;
CREATE TABLE `announcement` (
    `id`               BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `title`            VARCHAR(200)  DEFAULT NULL COMMENT '公告标题',
    `content`          TEXT          DEFAULT NULL COMMENT '公告内容',
    `create_time`      DATETIME      DEFAULT NULL COMMENT '创建时间',
    `create_user_id`   BIGINT        DEFAULT NULL COMMENT '创建人ID (FK -> t_user.id)',
    `create_user_name` VARCHAR(50)   DEFAULT NULL COMMENT '创建人姓名(冗余)',
    `update_time`      DATETIME      DEFAULT NULL COMMENT '更新时间',
    `status`           INT           DEFAULT 1 COMMENT '状态: 0=无效, 1=有效',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';

-- ============================================================
-- 完成
-- ============================================================
SELECT '数据库 hmh 重建完成，共 6 张表' AS result;
