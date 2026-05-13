package com.supermarket.inventory.customer.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Customer {
    private Long id;
    private String code;
    private String name;
    private String contactPerson;
    private String phone;
    private String address;
    private String remark;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
