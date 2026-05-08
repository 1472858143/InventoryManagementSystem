package com.supermarket.inventory.stock.dto;

public record RestockRequest(Integer quantity, String operator) {}
