package com.learnhive.lessonservice.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnhive.lessonservice.exception.CustomException;
import com.learnhive.lessonservice.exception.ExceptionCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class RedisClient {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper(); // 자바 객체와 json 간 변환하는 역할

    // 숫자형 키를 받아 문자열 키로 변환해서 조회
    public <T> Optional<T> get(Long key, Class<T> classType) {
        return get(key.toString(), classType);
    }

    // 문자열 키로 조회
    private <T> Optional<T> get(String key, Class<T> classType) {
        String redisValue = (String) redisTemplate.opsForValue().get(key);
        if (ObjectUtils.isEmpty(redisValue)) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(redisValue, classType));
        } catch (JsonProcessingException e) {
            log.error("요청한 Key에 대한 Redis Value를 파싱하는 작업이 실패했습니다 -> {}: {}", key, e.getMessage());
            return Optional.empty();
        }
    }

    // 숫자형 키를 받아 문자열 키로 변환해서 저장
    public <T> void put(Long key, T value) {
        put(key.toString(), value);
    }

    // 문자열 키를 받아 저장
    private <T> void put(String key, T value) {
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value));
        } catch (JsonProcessingException e) {
            throw new CustomException(ExceptionCode.CART_SAVE_FAIL);
        }
    }
}
