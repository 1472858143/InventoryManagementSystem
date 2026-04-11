package com.supermarket.inventory.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.supermarket.inventory.**.mapper")
public class MybatisConfig {
}
