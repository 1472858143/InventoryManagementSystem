-- 数据库变更草案
-- 任务编号：IT-005
-- 创建日期：2026-05-13
-- 变更目的：业务升级迭代 A：基础资料
-- 说明：
-- 1. DDL 蓝本来自 IT-004 设计稿 `数据库变更.sql` "迭代 A 落地"段。
-- 2. 本文件在 IT-004 草案基础上落地为执行版本，可按需微调。
-- 3. 不直接覆盖 sql/market.sql 或 Document/3-项目开发/market.sql；验收后再同步。
-- 4. 本任务还包含字段对账（stock.shelf_status / stock_log.stock_type / restock_order）；
--    具体补齐脚本由开发执行时根据运行库实际偏差产出，写入本文件。

-- ============================================================
-- 本任务执行清单（开发开始时按顺序产出 SQL）
-- ============================================================

-- 1. 字段对账修复（依据运行库 vs 正式 SQL 差异）
--    a) stock.shelf_status 若代码已使用但正式 SQL 未定义 → 在 sql/market.sql 与
--       Document/3-项目开发/market.sql 补字段定义，类型与运行库一致。
--    b) stock_log.stock_type 同上。
--    c) restock_order 相关 Mapper 与表的偏差：决定删 Mapper 还是建表，写入开发记录决策段。
--    实际 SQL 在开发时填入。

-- 2. supplier
--    见 IT-004 数据库变更.sql 中 supplier 段，照搬即可。

-- 3. customer
--    见 IT-004 数据库变更.sql 中 customer 段，照搬即可。

-- 4. system_log
--    见 IT-004 数据库变更.sql 中 system_log 段，照搬即可。

-- 5. permission
--    见 IT-004 数据库变更.sql 中 permission 段，照搬即可。

-- 6. role_permission
--    见 IT-004 数据库变更.sql 中 role_permission 段，照搬即可。

-- ============================================================
-- 实际 SQL 内容（已执行，同步到 sql/market.sql）
-- ============================================================

-- 1. 字段对账修复（commit 43cb5b7 + cc7a034）
-- stock 表补 shelf_status（见 sql/market.sql — 已在 max_stock 之后）
-- stock_log 表补 stock_type（见 sql/market.sql — 已在 change_type 之后）
-- restock_order Mapper / Entity 为死代码，直接删除，不建表

-- 2-6. 新增 5 张表（commit 48d358f，已追加到 sql/market.sql 末尾）

CREATE TABLE supplier (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(32) NOT NULL COMMENT '供应商编码',
    name VARCHAR(100) NOT NULL COMMENT '供应商名称',
    contact_person VARCHAR(50) DEFAULT NULL COMMENT '联系人',
    phone VARCHAR(30) DEFAULT NULL COMMENT '联系电话',
    address VARCHAR(200) DEFAULT NULL COMMENT '地址',
    remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_supplier_code (code),
    KEY idx_supplier_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商主数据';

CREATE TABLE customer (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(32) NOT NULL COMMENT '客户编码',
    name VARCHAR(100) NOT NULL COMMENT '客户名称',
    contact_person VARCHAR(50) DEFAULT NULL,
    phone VARCHAR(30) DEFAULT NULL,
    address VARCHAR(200) DEFAULT NULL,
    remark VARCHAR(255) DEFAULT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_customer_code (code),
    KEY idx_customer_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户主数据';

CREATE TABLE system_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT DEFAULT NULL,
    username VARCHAR(50) DEFAULT NULL COMMENT '冗余便于查询',
    action VARCHAR(50) NOT NULL COMMENT '动作类型 LOGIN/LOGOUT/CREATE/UPDATE/DELETE/CANCEL',
    target_type VARCHAR(50) DEFAULT NULL COMMENT '目标类型 PURCHASE_ORDER/SALES_ORDER/USER',
    target_id BIGINT DEFAULT NULL,
    content VARCHAR(500) DEFAULT NULL COMMENT '描述',
    ip VARCHAR(45) DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_log_user (user_id),
    KEY idx_log_action (action),
    KEY idx_log_target (target_type, target_id),
    KEY idx_log_time (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统操作日志';

CREATE TABLE permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(64) NOT NULL COMMENT '权限编码 如 purchase:inbound',
    name VARCHAR(64) NOT NULL,
    parent_id BIGINT DEFAULT NULL,
    type VARCHAR(16) NOT NULL DEFAULT 'MENU' COMMENT 'MENU/BUTTON',
    path VARCHAR(128) DEFAULT NULL COMMENT '前端路由',
    sort INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_permission_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限点';

CREATE TABLE role_permission (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    KEY idx_rp_permission (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联';
