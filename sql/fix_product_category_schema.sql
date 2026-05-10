USE supermarket_inventory;

-- 修复商品分类标准化后的半迁移状态：
-- 当前代码使用 product.category_id 与 product.unit，不再写入旧 product.category 字符串列。
-- 执行前应确认 product.category_id 不存在 NULL。

ALTER TABLE product
    MODIFY COLUMN category_id BIGINT NOT NULL COMMENT '商品分类ID，关联 category.id';

ALTER TABLE product
    DROP COLUMN category;
