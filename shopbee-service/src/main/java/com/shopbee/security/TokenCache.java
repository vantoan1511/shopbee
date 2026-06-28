package com.shopbee.security;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class TokenCache {

    private final Map<String, CachedToken> cache = new ConcurrentHashMap<>();

    public String get(String key) {
        CachedToken token = cache.get(key);
        if (token == null || token.isExpired()) {
            return null;
        }
        return token.value;
    }

    public void put(String key, String token, long expiresInSeconds) {
        cache.put(key, new CachedToken(token, System.currentTimeMillis() + (expiresInSeconds - 30) * 1000));
    }


    record CachedToken(String value, long expiredAt) {
        boolean isExpired() {
            return System.currentTimeMillis() >= expiredAt;
        }
    }
}
