package org.yht.config;

import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.yht.util.EncryptionUtils;

@Configuration
public class RedisConfig {
	
	@Value("${spring.redis.port}")
	public int port;
	
	@Value("${spring.redis.host}")
	public String host;

	@Value("${spring.redis.database}")
	public int database;
	
	@Value("${spring.redis.password}")
	public String password;
	@Value("${spring.redis.rawKey}")
	public String rawKey;
	
	@Bean
	public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
		RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
	    redisTemplate.setKeySerializer(new StringRedisSerializer());
	    redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
	    redisTemplate.setConnectionFactory(connectionFactory);
	    return redisTemplate;
	}

	@Bean
	public RedisConnectionFactory redisConnectionFactory() {
		/** 암복호화 java.security.NoSuchProviderException: No such provider: BC 오류로 추가 */
  		Security.addProvider(new BouncyCastleProvider());
  		
		RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
	    redisStandaloneConfiguration.setHostName(host);
	    redisStandaloneConfiguration.setPort(port);
	    redisStandaloneConfiguration.setDatabase(database);
	    redisStandaloneConfiguration.setPassword(EncryptionUtils.decryptAES(password, rawKey));
	    LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(redisStandaloneConfiguration);
	    return connectionFactory;
	}
}
