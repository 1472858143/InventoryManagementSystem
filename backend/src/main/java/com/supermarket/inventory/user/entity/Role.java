package com.supermarket.inventory.user.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class Role {

    private Long id;
    private String roleName;
    private String roleCode;
    private String remark;
    private LocalDateTime createTime;

}
