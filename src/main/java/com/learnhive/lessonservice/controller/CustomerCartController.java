package com.learnhive.lessonservice.controller;

import com.learnhive.lessonservice.domain.redis.Cart;
import com.learnhive.lessonservice.dto.CartDto;
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
    public ResponseEntity<CartDto> addLessonToCart(@RequestBody CartDto form) {
        return ResponseEntity.ok(customerCartService.addLessonToCartFromLessonPage(form).toDto());
    }

    @GetMapping("/{customerId}/cart")
    public ResponseEntity<CartDto> getCart(@PathVariable Long customerId) {
        return ResponseEntity.ok(customerCartService.returnCart(customerId).toDto());
    }

    @PutMapping("/cart")
    public ResponseEntity<CartDto> updateCart(@RequestBody Cart updatedCart) {
        return ResponseEntity.ok(customerCartService.replaceWholeCartWithUpdatedCart(updatedCart).toDto());
    }

    @DeleteMapping("/cart/{lessonId}")
    public ResponseEntity<String> removeLessonFromCart(@PathVariable Long lessonId) {
        customerCartService.removeLessonFromCart(lessonId);
        return ResponseEntity.ok("해당 레슨을 장바구니에서 삭제했습니다.");
    }

    @PostMapping("/{customerId}/cart")
    public ResponseEntity<String> orderCartContents(@PathVariable Long customerId) {
        customerOrderService.processOrder(customerCartService.returnCart(customerId));
        return ResponseEntity.ok("장바구니의 모든 레슨에 대한 주문이 완료되었습니다.");
    }

}