create database IF NOT EXISTS supermarket_inventory
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE supermarket_inventory;

CREATE TABLE user (
                      id BIGINT PRIMARY KEY AUTO_INCREMENT,
                      username VARCHAR(50) NOT NULL,
                      password VARCHAR(255) NOT NULL COMMENT '密码哈希摘要，不存储明文密码',
                      real_name VARCHAR(50),
                      status TINYINT NOT NULL DEFAULT 1 COMMENT '0-禁用 1-启用',
                      create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                      UNIQUE KEY uk_user_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE role (
                      id BIGINT PRIMARY KEY AUTO_INCREMENT,
                      role_name VARCHAR(50) NOT NULL,
                      role_code VARCHAR(50) NOT NULL,
                      remark VARCHAR(100),
                      create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                      UNIQUE KEY uk_role_name (role_name),
                      UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE category (
                          id BIGINT PRIMARY KEY AUTO_INCREMENT,
                          category_name VARCHAR(50) NOT NULL COMMENT '分类名称',
                          status TINYINT NOT NULL DEFAULT 1 COMMENT '0-禁用 1-启用',
                          create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          UNIQUE KEY uk_category_name (category_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

CREATE TABLE product (
                         id BIGINT PRIMARY KEY AUTO_INCREMENT,
                         product_code VARCHAR(50) NOT NULL,
                         product_name VARCHAR(100) NOT NULL,
                         category_id BIGINT NOT NULL COMMENT '商品分类ID，关联 category.id',
                         unit VARCHAR(20) NOT NULL DEFAULT '件' COMMENT '计量单位',
                         purchase_price DECIMAL(10,2) NOT NULL,
                         sale_price DECIMAL(10,2) NOT NULL,
                         status TINYINT NOT NULL DEFAULT 1 COMMENT '0-下架 1-上架',
                         create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         UNIQUE KEY uk_product_code (product_code),
                         CONSTRAINT fk_product_category
                             FOREIGN KEY (category_id) REFERENCES category(id),
                         CHECK (purchase_price >= 0),
                         CHECK (sale_price >= 0),
                         CHECK (sale_price >= purchase_price)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE user_role (
                           id BIGINT PRIMARY KEY AUTO_INCREMENT,
                           user_id BIGINT NOT NULL,
                           role_id BIGINT NOT NULL,
                           UNIQUE KEY uk_user_role (user_id, role_id),
                           CONSTRAINT fk_user_role_user
                               FOREIGN KEY (user_id) REFERENCES user(id),
                           CONSTRAINT fk_user_role_role
                               FOREIGN KEY (role_id) REFERENCES role(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE stock (
                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       product_id BIGINT NOT NULL,
                       quantity INT NOT NULL,
                       min_stock INT NOT NULL,
                       max_stock INT NOT NULL,
                       shelf_status VARCHAR(20) NOT NULL DEFAULT '正常' COMMENT '上架状态：正常/缺货',
                       update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                           ON UPDATE CURRENT_TIMESTAMP,
                       UNIQUE KEY uk_stock_product (product_id),
                       CONSTRAINT fk_stock_product
                           FOREIGN KEY (product_id) REFERENCES product(id),
                       CHECK (quantity >= 0),
                       CHECK (min_stock >= 0),
                       CHECK (max_stock >= min_stock)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE inbound_order (
                               id BIGINT PRIMARY KEY AUTO_INCREMENT,
                               product_id BIGINT NOT NULL,
                               quantity INT NOT NULL,
                               operator VARCHAR(50) NOT NULL,
                               create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               CONSTRAINT fk_inbound_product
                                   FOREIGN KEY (product_id) REFERENCES product(id),
                               CHECK (quantity > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE outbound_order (
                                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                product_id BIGINT NOT NULL,
                                quantity INT NOT NULL,
                                operator VARCHAR(50) NOT NULL,
                                create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                CONSTRAINT fk_outbound_product
                                    FOREIGN KEY (product_id) REFERENCES product(id),
                                CHECK (quantity > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE stock_check (
                             id BIGINT PRIMARY KEY AUTO_INCREMENT,
                             product_id BIGINT NOT NULL,
                             system_quantity INT NOT NULL,
                             actual_quantity INT NOT NULL,
                             difference INT NOT NULL,
                             check_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             CONSTRAINT fk_stock_check_product
                                 FOREIGN KEY (product_id) REFERENCES product(id),
                             CHECK (system_quantity >= 0),
                             CHECK (actual_quantity >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE stock_log (
                           id BIGINT PRIMARY KEY AUTO_INCREMENT,
                           product_id BIGINT NOT NULL,
                           change_type VARCHAR(20) NOT NULL COMMENT 'INBOUND / OUTBOUND / CHECK',
                           stock_type VARCHAR(20) NOT NULL DEFAULT 'WAREHOUSE' COMMENT '变更影响的库存类型：WAREHOUSE=仓库库存 / SHELF=上架库存',
                           change_quantity INT NOT NULL,
                           before_quantity INT NOT NULL,
                           after_quantity INT NOT NULL,
                           create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           CONSTRAINT fk_stock_log_product
                               FOREIGN KEY (product_id) REFERENCES product(id),
                           CHECK (after_quantity >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

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
