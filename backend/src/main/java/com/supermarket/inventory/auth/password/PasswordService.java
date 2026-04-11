package com.supermarket.inventory.auth.password;

public interface PasswordService {

    String encode(String rawPassword);

    boolean matches(String rawPassword, String passwordHash);
}
