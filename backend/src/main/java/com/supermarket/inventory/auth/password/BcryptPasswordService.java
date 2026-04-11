package com.supermarket.inventory.auth.password;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class BcryptPasswordService implements PasswordService {

    private final PasswordEncoder passwordEncoder;

    public BcryptPasswordService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String passwordHash) {
        return passwordEncoder.matches(rawPassword, passwordHash);
    }
}
