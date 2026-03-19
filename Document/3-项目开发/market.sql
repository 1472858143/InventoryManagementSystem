create database IF NOT EXISTS supermarket_inventory
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE supermarket_inventory;

CREATE TABLE user (
                      id BIGINT PRIMARY KEY AUTO_INCREMENT,
                      username VARCHAR(50) NOT NULL,
                      password VARCHAR(100) NOT NULL,
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

CREATE TABLE product (
                         id BIGINT PRIMARY KEY AUTO_INCREMENT,
                         product_code VARCHAR(50) NOT NULL,
                         product_name VARCHAR(100) NOT NULL,
                         category VARCHAR(50) NOT NULL,
                         purchase_price DECIMAL(10,2) NOT NULL,
                         sale_price DECIMAL(10,2) NOT NULL,
                         status TINYINT NOT NULL DEFAULT 1 COMMENT '0-下架 1-上架',
                         create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         UNIQUE KEY uk_product_code (product_code),
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
                           change_quantity INT NOT NULL,
                           before_quantity INT NOT NULL,
                           after_quantity INT NOT NULL,
                           create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           CONSTRAINT fk_stock_log_product
                               FOREIGN KEY (product_id) REFERENCES product(id),
                           CHECK (after_quantity >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
