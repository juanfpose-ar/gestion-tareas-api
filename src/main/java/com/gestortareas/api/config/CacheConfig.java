package com.gestortareas.api.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.gestortareas.api.security.MembresiaTableroCacheService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    @Value("${app.cache.membresia-tablero.ttl-horas:6}")
    private long ttlHoras;

    @Bean
    public CaffeineCacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(MembresiaTableroCacheService.CACHE_NAME);
        cacheManager.setCaffeine(Caffeine.newBuilder().expireAfterWrite(ttlHoras, TimeUnit.HOURS));
        return cacheManager;
    }
}
