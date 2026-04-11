package com.supermarket.inventory.auth;

import com.supermarket.inventory.auth.password.PasswordService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class PasswordEncodeTest {

    @Autowired
    private PasswordService passwordService;

    @Test
    void printBcryptPassword() {
        String rawPassword = "123456";
        String encodedPassword = passwordService.encode(rawPassword);
        System.out.println("BCrypt hash: " + encodedPassword);
    }
}
