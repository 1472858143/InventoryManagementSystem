-- 数据库变更草案
-- 任务编号：IT-004
-- 创建日期：2026-05-13
-- 变更目的：业务升级第一阶段：业务模型与数据库设计
-- 说明：
-- 1. 本文件只记录草案，不直接覆盖 sql/market.sql 或 Document/3-项目开发/market.sql。
-- 2. 各表落地时机：随迭代 A / B / C / E 分批执行。
-- 3. 字段类型按 MySQL 8.x 撰写，与现有 market.sql 风格保持一致。
-- 4. 所有时间字段统一 DATETIME DEFAULT CURRENT_TIMESTAMP；更新时间统一 ON UPDATE CURRENT_TIMESTAMP。

-- ============================================================
-- 迭代 A 落地：基础资料 + 系统类表 DDL（仅建表，权限/日志业务逻辑留到 E）
-- ============================================================

-- 供应商主数据
CREATE TABLE IF NOT EXISTS `supplier` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `code` VARCHAR(32) NOT NULL COMMENT '供应商编码',
  `name` VARCHAR(100) NOT NULL COMMENT '供应商名称',
  `contact_person` VARCHAR(50) DEFAULT NULL COMMENT '联系人',
  `phone` VARCHAR(30) DEFAULT NULL COMMENT '联系电话',
  `address` VARCHAR(200) DEFAULT NULL COMMENT '地址',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_supplier_code` (`code`),
  KEY `idx_supplier_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商主数据';

-- 客户主数据
CREATE TABLE IF NOT EXISTS `customer` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `code` VARCHAR(32) NOT NULL COMMENT '客户编码',
  `name` VARCHAR(100) NOT NULL COMMENT '客户名称',
  `contact_person` VARCHAR(50) DEFAULT NULL,
  `phone` VARCHAR(30) DEFAULT NULL,
  `address` VARCHAR(200) DEFAULT NULL,
  `remark` VARCHAR(255) DEFAULT NULL,
  `status` TINYINT NOT NULL DEFAULT 1,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_customer_code` (`code`),
  KEY `idx_customer_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户主数据';

-- 系统日志
CREATE TABLE IF NOT EXISTS `system_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT DEFAULT NULL,
  `username` VARCHAR(50) DEFAULT NULL COMMENT '冗余便于查询',
  `action` VARCHAR(50) NOT NULL COMMENT '动作类型 LOGIN/LOGOUT/CREATE/UPDATE/DELETE/CANCEL ...',
  `target_type` VARCHAR(50) DEFAULT NULL COMMENT '目标类型 PURCHASE_ORDER/SALES_ORDER/USER ...',
  `target_id` BIGINT DEFAULT NULL,
  `content` VARCHAR(500) DEFAULT NULL COMMENT '描述',
  `ip` VARCHAR(45) DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_log_user` (`user_id`),
  KEY `idx_log_action` (`action`),
  KEY `idx_log_target` (`target_type`, `target_id`),
  KEY `idx_log_time` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统操作日志';

-- 权限
CREATE TABLE IF NOT EXISTS `permission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `code` VARCHAR(64) NOT NULL COMMENT '权限编码 如 purchase:inbound',
  `name` VARCHAR(64) NOT NULL,
  `parent_id` BIGINT DEFAULT NULL,
  `type` VARCHAR(16) NOT NULL DEFAULT 'MENU' COMMENT 'MENU/BUTTON',
  `path` VARCHAR(128) DEFAULT NULL COMMENT '前端路由',
  `sort` INT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_permission_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限点';

-- 角色-权限关联
CREATE TABLE IF NOT EXISTS `role_permission` (
  `role_id` BIGINT NOT NULL,
  `permission_id` BIGINT NOT NULL,
  PRIMARY KEY (`role_id`, `permission_id`),
  KEY `idx_rp_permission` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联';


-- ============================================================
-- 迭代 B 落地：进货 + 销售 + 退货 + stock_log 扩展
-- ============================================================

-- 进货单主表
CREATE TABLE IF NOT EXISTS `purchase_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_no` VARCHAR(32) NOT NULL COMMENT '单据编号',
  `supplier_id` BIGINT NOT NULL,
  `total_quantity` INT NOT NULL DEFAULT 0,
  `total_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  `operator_id` BIGINT NOT NULL,
  `status` VARCHAR(16) NOT NULL DEFAULT 'NORMAL' COMMENT 'NORMAL/CANCELLED',
  `remark` VARCHAR(255) DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_po_no` (`order_no`),
  KEY `idx_po_supplier` (`supplier_id`),
  KEY `idx_po_time` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='进货单主表';

-- 进货单明细
CREATE TABLE IF NOT EXISTS `purchase_order_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_id` BIGINT NOT NULL,
  `product_id` BIGINT NOT NULL,
  `quantity` INT NOT NULL,
  `unit_price` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `subtotal` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  `remark` VARCHAR(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_poi_order` (`order_id`),
  KEY `idx_poi_product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='进货单明细';

-- 采购退货单主表
CREATE TABLE IF NOT EXISTS `purchase_return_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `return_no` VARCHAR(32) NOT NULL,
  `supplier_id` BIGINT NOT NULL,
  `source_order_id` BIGINT DEFAULT NULL COMMENT '原进货单ID 可空',
  `total_quantity` INT NOT NULL DEFAULT 0,
  `total_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  `operator_id` BIGINT NOT NULL,
  `status` VARCHAR(16) NOT NULL DEFAULT 'NORMAL',
  `reason` VARCHAR(255) DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pro_no` (`return_no`),
  KEY `idx_pro_supplier` (`supplier_id`),
  KEY `idx_pro_source` (`source_order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购退货单主表';

-- 采购退货明细
CREATE TABLE IF NOT EXISTS `purchase_return_order_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `return_id` BIGINT NOT NULL,
  `product_id` BIGINT NOT NULL,
  `quantity` INT NOT NULL,
  `unit_price` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `subtotal` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  `remark` VARCHAR(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_proi_return` (`return_id`),
  KEY `idx_proi_product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购退货明细';

-- 销售单主表
CREATE TABLE IF NOT EXISTS `sales_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_no` VARCHAR(32) NOT NULL,
  `customer_id` BIGINT NOT NULL,
  `total_quantity` INT NOT NULL DEFAULT 0,
  `total_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  `operator_id` BIGINT NOT NULL,
  `status` VARCHAR(16) NOT NULL DEFAULT 'NORMAL',
  `remark` VARCHAR(255) DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_so_no` (`order_no`),
  KEY `idx_so_customer` (`customer_id`),
  KEY `idx_so_time` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售单主表';

-- 销售单明细
CREATE TABLE IF NOT EXISTS `sales_order_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_id` BIGINT NOT NULL,
  `product_id` BIGINT NOT NULL,
  `quantity` INT NOT NULL,
  `unit_price` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `subtotal` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  `remark` VARCHAR(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_soi_order` (`order_id`),
  KEY `idx_soi_product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售单明细';

-- 客户退货单主表
CREATE TABLE IF NOT EXISTS `sales_return_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `return_no` VARCHAR(32) NOT NULL,
  `customer_id` BIGINT NOT NULL,
  `source_order_id` BIGINT DEFAULT NULL COMMENT '原销售单ID 可空',
  `total_quantity` INT NOT NULL DEFAULT 0,
  `total_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  `operator_id` BIGINT NOT NULL,
  `status` VARCHAR(16) NOT NULL DEFAULT 'NORMAL',
  `reason` VARCHAR(255) DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sro_no` (`return_no`),
  KEY `idx_sro_customer` (`customer_id`),
  KEY `idx_sro_source` (`source_order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户退货单主表';

-- 客户退货明细
CREATE TABLE IF NOT EXISTS `sales_return_order_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `return_id` BIGINT NOT NULL,
  `product_id` BIGINT NOT NULL,
  `quantity` INT NOT NULL,
  `unit_price` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `subtotal` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  `remark` VARCHAR(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_sroi_return` (`return_id`),
  KEY `idx_sroi_product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户退货明细';

-- stock_log 扩展
-- 注意：执行前应先用 INFORMATION_SCHEMA 检查字段是否已存在
ALTER TABLE `stock_log`
  ADD COLUMN `source_type` VARCHAR(32) DEFAULT NULL COMMENT '来源单据类型 PURCHASE_IN/PURCHASE_RETURN_OUT/SALE_OUT/SALE_RETURN_IN/STOCK_LOSS/STOCK_OVERFLOW/STOCK_CHECK' AFTER `stock_type`,
  ADD COLUMN `source_id` BIGINT DEFAULT NULL COMMENT '来源单据ID' AFTER `source_type`,
  ADD COLUMN `reason` VARCHAR(255) DEFAULT NULL AFTER `source_id`,
  ADD COLUMN `operator_id` BIGINT DEFAULT NULL AFTER `reason`,
  ADD KEY `idx_log_source` (`source_type`, `source_id`);


-- ============================================================
-- 迭代 C 落地：报损报溢
-- ============================================================

CREATE TABLE IF NOT EXISTS `stock_adjustment_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `adjustment_no` VARCHAR(32) NOT NULL,
  `type` VARCHAR(16) NOT NULL COMMENT 'LOSS 报损 / OVERFLOW 报溢',
  `total_quantity` INT NOT NULL DEFAULT 0,
  `operator_id` BIGINT NOT NULL,
  `status` VARCHAR(16) NOT NULL DEFAULT 'NORMAL',
  `reason` VARCHAR(255) DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sao_no` (`adjustment_no`),
  KEY `idx_sao_type` (`type`),
  KEY `idx_sao_time` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报损/报溢主表';

CREATE TABLE IF NOT EXISTS `stock_adjustment_order_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `adjustment_id` BIGINT NOT NULL,
  `product_id` BIGINT NOT NULL,
  `quantity` INT NOT NULL,
  `unit_price` DECIMAL(10,2) DEFAULT NULL COMMENT '可空',
  `subtotal` DECIMAL(12,2) DEFAULT NULL COMMENT '可空',
  `remark` VARCHAR(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_saoi_adjustment` (`adjustment_id`),
  KEY `idx_saoi_product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报损/报溢明细';


-- ============================================================
-- 迭代 A 字段对账（详见 设计方案.md 风险登记）
-- 1. stock.shelf_status / stock_log.stock_type 等代码已用但 sql/market.sql 缺失的字段
--    -> 补齐到正式 SQL，类型与运行库一致
-- 2. restock_order Mapper 存在但表不存在
--    -> 由后端在迭代 A 内确认：删 Mapper 还是建表
-- 本对账动作不在本草案内书写，迭代 A 任务 README 会专列条目处理。

-- ============================================================
-- 迭代 E 落地：旧表归档
-- 决定是否物理删除 inbound_order / outbound_order，或仅菜单下线、数据保留
-- 本草案不预写 DROP 语句。
