-- 数据库变更草案
-- 说明：
-- 1. 本文件只记录当前任务的数据库变更建议。
-- 2. 不得直接覆盖正式 SQL。
-- 3. 经用户验收后，再决定是否同步到 sql/market.sql 和 Document/3-项目开发/market.sql。

-- 任务编号：IT-002
-- 创建日期：2026-05-10
-- 变更目的：
--   1. 修正 IT-001 的库存双数量字段，改为"总库存数量 + 陈列状态枚举"。
--   2. 删除 IT-001 遗留的旧 quantity 字段（如尚未删除）。
-- 前置条件：
--   - 已执行 IT-001 的 DDL（warehouse_quantity / shelf_quantity / stock_log.stock_type / product.category_id / product.unit）。
--   - 数据库为 MySQL 8.0+，使用 InnoDB 引擎，字符集 utf8mb4。
-- 执行顺序：UP 脚本按 1→6 顺序执行；DOWN 脚本按 6→1 顺序回滚。

-- =====================================================================
-- UP：升级到 IT-002 后的 schema
-- =====================================================================

-- 1. 删除 IT-001 遗留的旧 quantity 字段（如果还存在）
ALTER TABLE stock
DROP COLUMN IF EXISTS quantity;

-- 2. 将 warehouse_quantity 重命名为 quantity（同时合并 shelf_quantity 数据）
--    先把合计值写到 warehouse_quantity，再重命名
UPDATE stock
SET warehouse_quantity = COALESCE(warehouse_quantity, 0) + COALESCE(shelf_quantity, 0);

ALTER TABLE stock
CHANGE COLUMN warehouse_quantity quantity INT NOT NULL DEFAULT 0 COMMENT '总库存数量';

-- 3. 删除 shelf_quantity 字段
ALTER TABLE stock
DROP COLUMN shelf_quantity;

-- 4. 新增 shelf_status 字段（枚举：未上架 / 缺货 / 较少 / 充足）
ALTER TABLE stock
ADD COLUMN shelf_status VARCHAR(16) NOT NULL DEFAULT '未上架'
  COMMENT '陈列状态：未上架/缺货/较少/充足，由理货员手动标记'
AFTER quantity;

-- 5. 初始数据回填：quantity > 0 的库存默认设为"充足"，便于演示
UPDATE stock
SET shelf_status = '充足'
WHERE quantity > 0;

-- 6. 添加索引以支持工作台缺货预警和仪表盘查询
CREATE INDEX idx_stock_shelf_status ON stock(shelf_status);

-- =====================================================================
-- DOWN：回退到 IT-001 后的 schema（应急用，不建议执行）
-- =====================================================================
-- 注意：执行 DOWN 后，shelf_status 数据完全丢失，shelf_quantity 无法精确还原。
-- 仅在 UP 失败且需立即回退时使用。

-- DROP INDEX idx_stock_shelf_status ON stock;
-- ALTER TABLE stock DROP COLUMN shelf_status;
-- ALTER TABLE stock ADD COLUMN shelf_quantity INT NOT NULL DEFAULT 0 COMMENT '货架数量';
-- ALTER TABLE stock CHANGE COLUMN quantity warehouse_quantity INT NOT NULL DEFAULT 0 COMMENT '仓库数量';
-- ALTER TABLE stock ADD COLUMN quantity INT NOT NULL DEFAULT 0 COMMENT '旧总库存（IT-001 前）';

-- =====================================================================
-- 验证查询
-- =====================================================================
-- 执行 UP 后运行以下查询确认结构和数据正确：

-- DESC stock;
-- 期望字段：id, product_id, quantity, shelf_status, min_stock, max_stock, ...

-- SELECT shelf_status, COUNT(*) FROM stock GROUP BY shelf_status;
-- 期望：除 quantity=0 的记录外，其他默认 '充足'

-- SHOW INDEX FROM stock WHERE Key_name = 'idx_stock_shelf_status';
-- 期望：返回 1 条索引记录
