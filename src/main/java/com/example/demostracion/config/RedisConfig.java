package com.example.demostracion.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Duration;

/**
 * Configuración de Redis para caché
 * 
 * ISO 25010: Rendimiento - Utilización de recursos
 * ✅ Mejora velocidad de acceso a datos frecuentes
 * ✅ Reduce carga de BD
 * 
 * @author Sistema
 * @since 1.0
 */
// @Configuration
@EnableCaching
@ConditionalOnProperty(name = "spring.redis.enabled", havingValue = "true", matchIfMissing = true)
public class RedisConfig {

    /**
     * Fábrica de conexión a Redis
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory();
    }

    /**
     * ObjectMapper para serialización JSON
     */
    private ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        /*
        mapper.activateDefaultTyping(
            mapper.getPolymorphicTypeTree(Object.class),
            ObjectMapper.DefaultTyping.NON_FINAL,
            JsonTypeInfo.As.PROPERTY
        );
        */
        /*
        mapper.setVisibility(
            mapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.ANY)
                .withSetterVisibility(JsonAutoDetect.Visibility.ANY)
                .withCreatorVisibility(JsonAutoDetect.Visibility.ANY)
        );
        */
        return mapper;
    }

    /**
     * Configuración de caché en Redis
     * TTL por defecto: 15 minutos
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(AppConstants.CACHE_TTL_MINUTES))
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new StringRedisSerializer()
                )
            )
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new Jackson2JsonRedisSerializer<>(Object.class)
                )
            )
            .disableCachingNullValues();

        return RedisCacheManager.builder(redisConnectionFactory)
            .cacheDefaults(config)
            // Cachés específicas con TTL personalizado
            .withCacheConfiguration(AppConstants.CACHE_VEHICULOS,
                config.entryTtl(Duration.ofMinutes(30)))
            .withCacheConfiguration(AppConstants.CACHE_USUARIOS,
                config.entryTtl(Duration.ofMinutes(10)))
            .withCacheConfiguration(AppConstants.CACHE_ROLES,
                config.entryTtl(Duration.ofHours(1)))
            .build();
    }
}
