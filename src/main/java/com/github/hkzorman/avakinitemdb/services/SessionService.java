package com.github.hkzorman.avakinitemdb.services;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.hkzorman.avakinitemdb.models.db.User;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class SessionService {
    private Cache<UUID, User> cache;

    public SessionService() {
        this.cache = Caffeine.newBuilder().expireAfterWrite(3, TimeUnit.DAYS).build();
    }

    public void addSession(UUID uuid, User user) {
        var cachedUser = this.cache.getIfPresent(uuid);
        if (cachedUser == null) {
            this.cache.put(uuid, user);
        }
    }

    public boolean confirmSession(UUID uuid) {
        var user = this.cache.getIfPresent(uuid);
        return user != null;
    }
}
