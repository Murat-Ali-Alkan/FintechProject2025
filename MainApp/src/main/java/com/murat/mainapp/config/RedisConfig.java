package com.murat.mainapp.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Configuration class for setting up Redis caching in the application.
 * <p>
 * This class enables caching via {@link EnableCaching} and provides necessary beans
 * for connecting to a Redis server, serializing data, and managing caches.
 * </p>
 * @see EnableCaching
 */
@Configuration
@EnableCaching
public class RedisConfig {


    /**
     * Creates a {@link RedisConnectionFactory} using Lettuce.
     *
     * @return the Redis connection factory
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory();
    }

    /**
     * Configures a {@link RedisTemplate} for Redis operations.
     * <p>
     * Keys are serialized as plain strings and values are serialized using
     * Jackson JSON format.
     * </p>
     *
     * @return a configured {@link RedisTemplate} instance
     */
    @Bean
    RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return redisTemplate;
    }

    /**
     * Configures the Spring {@link CacheManager} to use Redis as the backing cache store.
     * <p>
     * Caches are configured with a default time-to-live (TTL) of 10 minutes and null values are not cached.
     * </p>
     *
     * @param redisConnectionFactory the Redis connection factory used by the cache manager
     * @return a configured Redis {@link CacheManager}
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues();

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(redisCacheConfiguration)
                .build();

    }
}
