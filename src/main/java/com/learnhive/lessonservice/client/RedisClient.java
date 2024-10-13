package com.learnhive.lessonservice.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnhive.lessonservice.domain.redis.Cart;
import com.learnhive.lessonservice.exception.CustomException;
import com.learnhive.lessonservice.exception.ExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@RequiredArgsConstructor
@Service
public class RedisClient {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final ObjectMapper objectMapper = new ObjectMapper(); // 자바 객체와 json 간 변환하는 역할

    public <T> T get(Long key, Class<T> classType) {
        return get(key.toString(), classType);
    }

    private <T> T get(String key, Class<T> classType) {
        String redisValue = (String) redisTemplate.opsForValue().get(key);
        if (ObjectUtils.isEmpty(redisValue)) {
            return null;
        } else {
            try {
                return objectMapper.readValue(redisValue, classType);
            } catch (JsonProcessingException e) {
                return null;
            }
        }
    }

    // key-value 에서 Long 타입 key 로 cart 객체를 생성해서 저장
    public void put(Long key, Cart cart) {
        put(key.toString(), cart);
    }

    // key-value 에서 string 타입 key 로 cart 객체를 생성해서 저장
    private void put(String key, Cart cart) {
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(cart));
        } catch (JsonProcessingException e) {
            throw new CustomException(ExceptionCode.CART_SAVE_FAIL);
        }
    }
}
