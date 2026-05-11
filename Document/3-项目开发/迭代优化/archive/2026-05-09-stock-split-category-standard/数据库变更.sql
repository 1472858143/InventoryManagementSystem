-- 任务名称：库存结构拆分与商品分类标准化
-- 任务编号：IT-001
-- 创建日期：2026-05-09
-- 说明：本文件为任务草稿，未经用户验收不得合并至 sql/market.sql
--       执行前请先对数据库做完整备份：mysqldump -u root -p supermarket_inventory > backup_before_IT001.sql
--       按阶段执行，每个阶段验证通过后再执行下一阶段。

USE supermarket_inventory;

-- ============================================================
-- 阶段一：新增 category 分类表
-- ============================================================

CREATE TABLE category (
    id          BIGINT       PRIMARY KEY AUTO_INCREMENT,
    category_name VARCHAR(50) NOT NULL COMMENT '分类名称',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '0-禁用 1-启用',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_category_name (category_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- ============================================================
-- 阶段二：product 表新增字段（先加列，后迁移，最后加约束）
-- ============================================================

-- 2.1 新增 category_id（先允许 NULL，迁移后再改为 NOT NULL）
ALTER TABLE product
    ADD COLUMN category_id BIGINT NULL COMMENT '商品分类ID，关联 category.id';

-- 2.2 新增 unit 计量单位（有默认值，可立即 NOT NULL）
ALTER TABLE product
    ADD COLUMN unit VARCHAR(20) NOT NULL DEFAULT '件'
        COMMENT '计量单位（件/箱/kg/g/L/mL/瓶/袋等）';

-- ============================================================
-- 阶段三：数据迁移 —— 从现有 category 字符串建立分类表
-- ============================================================

-- 3.1 将 product 表中已有分类字符串去重插入 category 表
INSERT IGNORE INTO category (category_name)
SELECT DISTINCT category
FROM product
WHERE category IS NOT NULL
  AND TRIM(category) != '';

-- 3.2 将 product.category_id 根据 category_name 匹配回填
UPDATE product p
    INNER JOIN category c ON p.category = c.category_name
SET p.category_id = c.id;

-- 3.3 检查是否有 product 未匹配到分类（应为 0 行，否则需手动处理）
-- SELECT id, product_code, category FROM product WHERE category_id IS NULL;

-- ============================================================
-- 阶段四：对 product.category_id 加外键约束（迁移完成后执行）
-- ============================================================

-- 4.1 确认无 NULL 后，将 category_id 改为 NOT NULL
ALTER TABLE product
    MODIFY COLUMN category_id BIGINT NOT NULL COMMENT '商品分类ID，关联 category.id';

-- 4.2 加外键约束
ALTER TABLE product
    ADD CONSTRAINT fk_product_category
        FOREIGN KEY (category_id) REFERENCES category(id);

-- 4.3 删除旧 category 字符串列（迁移验证通过后执行）
-- 注意：此操作不可逆，务必确认 category_id 全部正确后再执行
ALTER TABLE product DROP COLUMN category;

-- ============================================================
-- 阶段五：stock 表拆分库存字段
-- ============================================================

-- 5.1 新增 warehouse_quantity（仓库库存）和 shelf_quantity（上架库存）
--     先加列（初始值 0），后迁移数据
ALTER TABLE stock
    ADD COLUMN warehouse_quantity INT NOT NULL DEFAULT 0
        COMMENT '仓库库存：入库增加，补货减少，非负';

ALTER TABLE stock
    ADD COLUMN shelf_quantity INT NOT NULL DEFAULT 0
        COMMENT '上架库存：补货增加，出库/购买减少，非负';

-- 5.2 数据迁移：将现有 quantity 全部归入 warehouse_quantity
--     shelf_quantity 初始为 0（现实含义：系统启动前所有库存视为在仓库中）
--     如需将部分归为 shelf_quantity，请用户手动调整或在此修改比例
UPDATE stock SET warehouse_quantity = quantity;

-- 5.3 对新字段加 CHECK 约束（MySQL 8.0.16+ 才强制执行 CHECK）
ALTER TABLE stock
    ADD CONSTRAINT chk_warehouse_qty CHECK (warehouse_quantity >= 0);

ALTER TABLE stock
    ADD CONSTRAINT chk_shelf_qty CHECK (shelf_quantity >= 0);

-- 5.4 删除旧 quantity 列（验证通过后执行，不可逆）
ALTER TABLE stock DROP COLUMN quantity;

-- ============================================================
-- 阶段六：新增 restock_order 补货单表
-- ============================================================

CREATE TABLE restock_order (
    id          BIGINT       PRIMARY KEY AUTO_INCREMENT,
    product_id  BIGINT       NOT NULL COMMENT '补货商品ID',
    quantity    INT          NOT NULL COMMENT '补货数量（从仓库移至货架）',
    operator    VARCHAR(50)  NOT NULL COMMENT '操作人',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_restock_product
        FOREIGN KEY (product_id) REFERENCES product(id),
    CHECK (quantity > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='补货单：记录从仓库补货到货架的操作';

-- ============================================================
-- 阶段七：更新 stock_log 表以支持拆分后的日志记录
-- ============================================================

-- 说明：change_type 现有值为 'INBOUND' / 'OUTBOUND' / 'CHECK'
-- 新增类型：'RESTOCK'（补货）
-- 同时需要区分变更影响的是哪种库存，增加 stock_type 字段

ALTER TABLE stock_log
    ADD COLUMN stock_type VARCHAR(20) NOT NULL DEFAULT 'WAREHOUSE'
        COMMENT '变更影响的库存类型：WAREHOUSE=仓库库存 / SHELF=上架库存';

-- 补全历史数据（历史入库 → WAREHOUSE，历史出库 → 旧量不区分，标记为 WAREHOUSE 兼容）
UPDATE stock_log SET stock_type = 'WAREHOUSE' WHERE change_type = 'INBOUND';
UPDATE stock_log SET stock_type = 'SHELF'     WHERE change_type = 'OUTBOUND';
UPDATE stock_log SET stock_type = 'WAREHOUSE' WHERE change_type = 'CHECK';
-- RESTOCK 类型在后续新增补货时产生两条日志：一条 WAREHOUSE 减少，一条 SHELF 增加

-- ============================================================
-- 参考：补货业务逻辑（由后端 Service 在事务中执行）
-- ============================================================
-- BEGIN;
--   -- 扣减仓库库存
--   UPDATE stock
--   SET warehouse_quantity = warehouse_quantity - #{quantity}
--   WHERE product_id = #{productId} AND warehouse_quantity >= #{quantity};
--   -- 若 affected rows = 0，说明仓库库存不足，ROLLBACK
--
--   -- 增加上架库存
--   UPDATE stock
--   SET shelf_quantity = shelf_quantity + #{quantity}
--   WHERE product_id = #{productId};
--
--   -- 插入补货单记录
--   INSERT INTO restock_order (product_id, quantity, operator) VALUES (...);
--
--   -- 插入两条 stock_log
--   INSERT INTO stock_log (product_id, change_type, stock_type, change_quantity, before_quantity, after_quantity)
--   VALUES (#{productId}, 'RESTOCK', 'WAREHOUSE', -#{quantity}, #{before_warehouse}, #{after_warehouse});
--   INSERT INTO stock_log (product_id, change_type, stock_type, change_quantity, before_quantity, after_quantity)
--   VALUES (#{productId}, 'RESTOCK', 'SHELF', #{quantity}, #{before_shelf}, #{after_shelf});
-- COMMIT;
