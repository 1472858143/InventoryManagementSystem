-- 任务名称：IT-007 业务升级迭代 C：库存管理（报损报溢 + 库存报警）
-- 创建日期：2026-05-14
-- 说明：本文件为任务草案，未经用户验收不得合并至 sql/market.sql 或 Document/3-项目开发/market.sql

CREATE TABLE IF NOT EXISTS stock_adjustment_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    adjustment_no VARCHAR(32) NOT NULL COMMENT '报损/报溢单号',
    type VARCHAR(16) NOT NULL COMMENT 'LOSS 报损 / OVERFLOW 报溢',
    total_quantity INT NOT NULL DEFAULT 0,
    operator_id BIGINT NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'NORMAL' COMMENT 'NORMAL/CANCELLED',
    reason VARCHAR(255) DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_sao_no (adjustment_no),
    KEY idx_sao_type (type),
    KEY idx_sao_operator (operator_id),
    KEY idx_sao_time (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报损/报溢主表';

CREATE TABLE IF NOT EXISTS stock_adjustment_order_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    adjustment_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) DEFAULT NULL COMMENT '可空',
    subtotal DECIMAL(12,2) DEFAULT NULL COMMENT '可空',
    remark VARCHAR(255) DEFAULT NULL,
    KEY idx_saoi_adjustment (adjustment_id),
    KEY idx_saoi_product (product_id),
    CHECK (quantity > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报损/报溢明细';

-- 库存报警不新增表：
-- 低库存：stock.quantity < stock.min_stock
-- 高库存：stock.quantity > stock.max_stock
