package com.supermarket.inventory.auth.context;

import com.supermarket.inventory.auth.model.AuthSession;

public final class AuthContext {

    private static final ThreadLocal<AuthSession> CURRENT_SESSION = new ThreadLocal<>();

    private AuthContext() {
    }

    public static void setCurrentSession(AuthSession session) {
        CURRENT_SESSION.set(session);
    }

    public static AuthSession getCurrentSession() {
        return CURRENT_SESSION.get();
    }

    public static void clear() {
        CURRENT_SESSION.remove();
    }
}
