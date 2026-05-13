-- 数据库变更草案
-- 任务编号：IT-006
-- 创建日期：2026-05-13
-- 变更目的：业务升级迭代 B：进货 + 销售 + StockChangeService
-- 说明：
-- 1. 本文件只记录当前任务的数据库变更建议，不直接覆盖正式 SQL。
-- 2. DDL 蓝本来自 IT-004 设计稿 `数据库变更.sql` 的“迭代 B 落地”段。
-- 3. 经用户验收后，再决定是否同步到 sql/market.sql 和 Document/3-项目开发/market.sql。
-- 4. 执行 ALTER TABLE stock_log 前必须先用 INFORMATION_SCHEMA 检查字段是否已存在。
-- 5. 本轮不新增外键约束；供应商、客户、商品、库存存在性由后端 Service 校验。
-- 6. stock_log 新字段允许 NULL 以兼容旧日志；IT-006 新写入日志必须带 source_type 与 source_id。

-- ============================================================
-- 迭代 B 落地：进货 + 销售 + 退货 + stock_log 扩展
-- ============================================================

CREATE TABLE IF NOT EXISTS purchase_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(32) NOT NULL COMMENT '单据编号',
    supplier_id BIGINT NOT NULL,
    total_quantity INT NOT NULL DEFAULT 0,
    total_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    operator_id BIGINT NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'NORMAL' COMMENT 'NORMAL/CANCELLED',
    remark VARCHAR(255) DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_po_no (order_no),
    KEY idx_po_supplier (supplier_id),
    KEY idx_po_time (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='进货单主表';

CREATE TABLE IF NOT EXISTS purchase_order_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    subtotal DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    remark VARCHAR(255) DEFAULT NULL,
    KEY idx_poi_order (order_id),
    KEY idx_poi_product (product_id),
    CHECK (quantity > 0),
    CHECK (unit_price >= 0),
    CHECK (subtotal >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='进货单明细';

CREATE TABLE IF NOT EXISTS purchase_return_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    return_no VARCHAR(32) NOT NULL,
    supplier_id BIGINT NOT NULL,
    source_order_id BIGINT DEFAULT NULL COMMENT '原进货单ID 可空',
    total_quantity INT NOT NULL DEFAULT 0,
    total_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    operator_id BIGINT NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'NORMAL',
    reason VARCHAR(255) DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_pro_no (return_no),
    KEY idx_pro_supplier (supplier_id),
    KEY idx_pro_source (source_order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购退货单主表';

CREATE TABLE IF NOT EXISTS purchase_return_order_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    return_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    subtotal DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    remark VARCHAR(255) DEFAULT NULL,
    KEY idx_proi_return (return_id),
    KEY idx_proi_product (product_id),
    CHECK (quantity > 0),
    CHECK (unit_price >= 0),
    CHECK (subtotal >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购退货明细';

CREATE TABLE IF NOT EXISTS sales_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(32) NOT NULL,
    customer_id BIGINT NOT NULL,
    total_quantity INT NOT NULL DEFAULT 0,
    total_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    operator_id BIGINT NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'NORMAL',
    remark VARCHAR(255) DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_so_no (order_no),
    KEY idx_so_customer (customer_id),
    KEY idx_so_time (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售单主表';

CREATE TABLE IF NOT EXISTS sales_order_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    subtotal DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    remark VARCHAR(255) DEFAULT NULL,
    KEY idx_soi_order (order_id),
    KEY idx_soi_product (product_id),
    CHECK (quantity > 0),
    CHECK (unit_price >= 0),
    CHECK (subtotal >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售单明细';

CREATE TABLE IF NOT EXISTS sales_return_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    return_no VARCHAR(32) NOT NULL,
    customer_id BIGINT NOT NULL,
    source_order_id BIGINT DEFAULT NULL COMMENT '原销售单ID 可空',
    total_quantity INT NOT NULL DEFAULT 0,
    total_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    operator_id BIGINT NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'NORMAL',
    reason VARCHAR(255) DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_sro_no (return_no),
    KEY idx_sro_customer (customer_id),
    KEY idx_sro_source (source_order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户退货单主表';

CREATE TABLE IF NOT EXISTS sales_return_order_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    return_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    subtotal DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    remark VARCHAR(255) DEFAULT NULL,
    KEY idx_sroi_return (return_id),
    KEY idx_sroi_product (product_id),
    CHECK (quantity > 0),
    CHECK (unit_price >= 0),
    CHECK (subtotal >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户退货明细';

-- stock_log 扩展；执行前检查字段与索引是否已存在，避免重复 ALTER。
ALTER TABLE stock_log
    ADD COLUMN source_type VARCHAR(32) DEFAULT NULL COMMENT '来源单据类型 PURCHASE_IN/PURCHASE_RETURN_OUT/SALE_OUT/SALE_RETURN_IN/STOCK_LOSS/STOCK_OVERFLOW/STOCK_CHECK' AFTER stock_type,
    ADD COLUMN source_id BIGINT DEFAULT NULL COMMENT '来源单据ID' AFTER source_type,
    ADD COLUMN reason VARCHAR(255) DEFAULT NULL AFTER source_id,
    ADD COLUMN operator_id BIGINT DEFAULT NULL AFTER reason,
    ADD KEY idx_log_source (source_type, source_id);
