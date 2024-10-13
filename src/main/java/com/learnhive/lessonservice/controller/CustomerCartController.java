package com.learnhive.lessonservice.controller;

import com.learnhive.lessonservice.domain.redis.Cart;
import com.learnhive.lessonservice.dto.LessonCartRequestDto;
import com.learnhive.lessonservice.service.customer.CustomerCartService;
import com.learnhive.lessonservice.service.customer.CustomerOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/customers")
public class CustomerCartController {

    private final CustomerCartService customerCartService;
    private final CustomerOrderService customerOrderService;

    @PostMapping("/cart")
    public ResponseEntity<Cart> addLessonToCart(@RequestBody LessonCartRequestDto form) {
        Cart updatedCart = customerCartService.addLessonToCartFromLessonPage(form);
        return ResponseEntity.ok(updatedCart);
    }

    @GetMapping("/{customerId}/cart")
    public ResponseEntity<Cart> getCart(@PathVariable Long customerId) {
        Cart cart = customerCartService.returnCart(customerId);
        return ResponseEntity.ok(cart);
    }

    @PutMapping("/cart")
    public ResponseEntity<Cart> updateCart(@RequestBody Cart updatedCart) {
        Cart replacedCart = customerCartService.replaceWholeCartWithUpdatedCart(updatedCart);
        return ResponseEntity.ok(replacedCart);
    }

    @DeleteMapping("/cart/{lessonId}")
    public ResponseEntity<Cart> removeLessonFromCart(@PathVariable Long lessonId) {
        Cart updatedCart = customerCartService.removeLessonFromCart(lessonId);
        return ResponseEntity.ok(updatedCart);
    }

    @PostMapping("/{customerId}/cart")
    public ResponseEntity<String> orderCartContents(@PathVariable Long customerId) {
        customerOrderService.processOrder(customerCartService.returnCart(customerId));
        return ResponseEntity.ok("주문이 완료되었습니다.");
    }

}