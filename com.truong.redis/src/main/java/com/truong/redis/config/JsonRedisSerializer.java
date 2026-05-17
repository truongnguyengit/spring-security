package com.truong.redis.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

/**
 * @author Long Nguyen
 *
 */

public class JsonRedisSerializer implements RedisSerializer<Object> {

	private final ObjectMapper om;

	public JsonRedisSerializer() {
		this.om = new ObjectMapper()
				.enableDefaultTyping(DefaultTyping.NON_FINAL, As.PROPERTY)
				.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
				.disable(MapperFeature.USE_GETTERS_AS_SETTERS);
	}

	@Override
	public byte[] serialize(Object t) throws SerializationException {
		try {
			return om.writeValueAsBytes(t);
		} catch (JsonProcessingException e) {
			throw new SerializationException(e.getMessage(), e);
		}
	}

	@Override
	public Object deserialize(byte[] bytes) throws SerializationException {
		
		if(bytes == null){
			return null;
		}
		
		try {
			return om.readValue(bytes, Object.class);
		} catch (Exception e) {
			throw new SerializationException(e.getMessage(), e);
		}
	}
}
