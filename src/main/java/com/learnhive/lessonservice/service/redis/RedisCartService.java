package com.learnhive.lessonservice.service.redis;

import com.learnhive.lessonservice.client.RedisClient;
import com.learnhive.lessonservice.domain.redis.Cart;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RedisCartService {

    private final RedisClient redisClient;

    public Cart getCart(Long customerId) {
        return redisClient.get(customerId, Cart.class)
                .orElseGet(() -> new Cart(customerId));
    }

    public Cart putCart(Long customerId, Cart cart) {
        redisClient.put(customerId, cart);
        return cart;
    }

}
