package net.togogo.client;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

@Component
public class TokenStore {

    private final AtomicReference<String> tokenRef = new AtomicReference<>();

    public void setToken(String token) {
        tokenRef.set(token);
    }

    public String getToken() {
        return tokenRef.get();
    }

    public void clearToken() {
        tokenRef.set(null);
    }

    public boolean hasToken() {
        String token = tokenRef.get();
        return token != null && !token.isEmpty();
    }
}