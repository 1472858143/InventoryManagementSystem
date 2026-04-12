package com.supermarket.inventory.user.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class User {

    private Long id;
    private String username;
    private String password;
    private String realName;
    private Integer status;
    private LocalDateTime createTime;

}
