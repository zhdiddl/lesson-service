package com.learnhive.lessonservice.service.customer;

import com.learnhive.lessonservice.domain.lesson.LessonSlot;
import com.learnhive.lessonservice.domain.redis.Cart;
import com.learnhive.lessonservice.domain.user.CustomerBalance;
import com.learnhive.lessonservice.domain.user.UserAccount;
import com.learnhive.lessonservice.dto.CustomerBalanceDto;
import com.learnhive.lessonservice.exception.CustomException;
import com.learnhive.lessonservice.exception.ExceptionCode;
import com.learnhive.lessonservice.repository.CustomerBalanceRepository;
import com.learnhive.lessonservice.repository.LessonSlotRepository;
import com.learnhive.lessonservice.security.AuthenticatedUserService;
import com.learnhive.lessonservice.service.redis.RedisCartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CustomerOrderService {
    
    private final AuthenticatedUserService authenticatedUserService;
    private final CustomerCartService customerCartService;
    private final RedisCartService redisCartService;
    private final CustomerBalanceService customerBalanceService;
    private final CustomerBalanceRepository customerBalanceRepository;
    private final LessonSlotRepository lessonSlotRepository;

    @Transactional(rollbackFor = Exception.class)
    public void processOrder(Cart customerCart) {
        UserAccount authenticatedUser = authenticatedUserService.getAuthenticatedUser();
        CustomerBalance customerBalance = customerBalanceRepository.findByCustomerId(authenticatedUser.getId())
                .orElseThrow(() -> new CustomException(ExceptionCode.CUSTOMER_BALANCE_NOT_FOUND));

        // 장바구니 변동 사항 확인
        Cart updatedCart = customerCartService.refreshCart(redisCartService.getCart(customerCart.getCustomerId()));
        customerCartService.validateCartContents(customerCart);
        Cart verifiedCart = customerCartService.refreshCart(redisCartService.getCart(customerCart.getCustomerId()));

        if (!verifiedCart.getMessages().isEmpty()) {
            throw new CustomException(ExceptionCode.CART_UPDATE_REQUIRED);
        }

        // 고객 잔액 확인
        int orderTotalPrice = calculateTotalPrice(customerCart);
        if (customerBalance.getCurrentBalance() < orderTotalPrice) {
            throw new CustomException(ExceptionCode.INSUFFICIENT_BALANCE);
        }

        try {
            // 1. 고객 잔액 차감
            customerBalanceService.adjustCustomerBalance(authenticatedUser.getId(), CustomerBalanceDto.builder()
                    .initiator(authenticatedUser.getUsername())
                    .message("주문 결제로 잔액 차감")
                    .requestedAmount(-orderTotalPrice)
                    .build());

            // 2. 주문한 레슨 슬롯 재고 차감
            for (Cart.Lesson selectedLesson : customerCart.getLessons()) {
                for (Cart.LessonSlot selectedSlot : selectedLesson.getLessonSlots()) {
                    LessonSlot lessonSlot = lessonSlotRepository.findById(selectedSlot.getId())
                            .orElseThrow(() -> new CustomException(ExceptionCode.LESSON_SLOT_NOT_FOUND));
                    lessonSlot.updateQuantity(lessonSlot.getQuantity() - selectedSlot.getQuantity());
                }
            }

            // 3. 주문 후 남은 장바구니 정보 저장
            updateCartAfterOrder(updatedCart, customerCart);
            redisCartService.putCart(customerCart.getCustomerId(), updatedCart);
        } catch (Exception e) {
            throw e;
        }
    }


    private void updateCartAfterOrder(Cart updatedCart, Cart currentCart) {
        for (Cart.Lesson currentCartLesson : currentCart.getLessons()) {
            Optional<Cart.Lesson> optionalCartLesson = updatedCart.getLessons().stream()
                    .filter(lesson -> lesson.getId().equals(currentCartLesson.getId()))
                    .findFirst();

            if (optionalCartLesson.isPresent()) {
                Cart.Lesson cartLesson = optionalCartLesson.get();
                for (Cart.LessonSlot currentCartSlot : currentCartLesson.getLessonSlots()) {
                    cartLesson.getLessonSlots()
                            .removeIf(slot -> slot.getId().equals(currentCartSlot.getId()));
                }

                if (cartLesson.getLessonSlots().isEmpty()) {
                    updatedCart.getLessons().removeIf(lesson -> lesson.getId().equals(cartLesson.getId()));
                }

            }
        }
    }

    private Integer calculateTotalPrice(Cart selectedCart) {
        return selectedCart.getLessons().stream()
                .mapToInt(Cart.Lesson::getPrice)
                .sum();
    }

}
