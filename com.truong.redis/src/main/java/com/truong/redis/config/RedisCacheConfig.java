package com.truong.redis.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;


@Configuration
@EnableCaching
public class RedisCacheConfig {
	
	
	@Value("${spring.redis.host}")
	private String redisCacheHost;
	
	@Value("${spring.redis.port}")
	private int redisCachePort;
	
	@Value("${spring.redis.password}")
	private String redisCachePassword;
	
	@Value("${spring.redis.database}")
	private int redisCacheDatabase;
	
	@Primary
	@Bean(name = "redisCacheConnectionFactory")
	RedisConnectionFactory redisCacheConnectionFactory() {
		RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration(this.redisCacheHost, this.redisCachePort);
		redisConfig.setPassword(this.redisCachePassword);
		redisConfig.setDatabase(this.redisCacheDatabase);
		return new LettuceConnectionFactory(redisConfig);
	}
	
	@Bean(value ="redisTemplate")
	RedisTemplate<String, Object> redisTemplate(@Qualifier("redisCacheConnectionFactory") RedisConnectionFactory redisCacheConnectionFactory) {
	    RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
	    redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new JsonRedisSerializer());
        redisTemplate.setHashValueSerializer(new JsonRedisSerializer());
	    redisTemplate.setConnectionFactory(redisCacheConnectionFactory);
	    return redisTemplate;
	}


	
	@Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration cacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(120)) // Thời gian sống của cache
                .serializeValuesWith(
                    RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.json())
                );

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(cacheConfiguration)
                .build();
    }
}