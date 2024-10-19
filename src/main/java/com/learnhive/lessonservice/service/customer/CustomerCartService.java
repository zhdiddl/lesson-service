package com.learnhive.lessonservice.service.customer;

import com.learnhive.lessonservice.domain.lesson.Lesson;
import com.learnhive.lessonservice.domain.lesson.LessonSlot;
import com.learnhive.lessonservice.domain.redis.Cart;
import com.learnhive.lessonservice.domain.user.UserAccount;
import com.learnhive.lessonservice.dto.CartDto;
import com.learnhive.lessonservice.exception.CustomException;
import com.learnhive.lessonservice.exception.ExceptionCode;
import com.learnhive.lessonservice.security.AuthenticatedUserService;
import com.learnhive.lessonservice.service.LessonSearchService;
import com.learnhive.lessonservice.service.redis.RedisCartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class CustomerCartService {

    private final AuthenticatedUserService authenticatedUserService;
    private final LessonSearchService lessonSearchService;
    private final RedisCartService redisCartService;

    public Cart addLessonToCartFromLessonPage(CartDto form) {
        UserAccount authenticatedUser = authenticatedUserService.getAuthenticatedUser();

        // 고객의 장바구니를 불러오기 (기존 장바구니가 없다면 새 장바구니 생성)
        Cart currentCart = redisCartService.getCart(authenticatedUser.getId());

        // 슬롯 선택을 하지 않은 레슨을 장바구니에 담으려고 하는 경우 예외 처리
        if (form.lessons() == null || form.lessons().isEmpty()) {
            throw new CustomException(ExceptionCode.LESSON_NOT_FOUND);
        }

        for (CartDto.Lesson lesson : form.lessons()) {
            if (lesson.lessonSlots() == null || lesson.lessonSlots().isEmpty()) {
                throw new CustomException(ExceptionCode.LESSON_SLOT_NOT_FOUND);
            }

            // 추가하려는 레슨이 DB에 존재하는지 확인
            Lesson dbLesson = lessonSearchService.getByLessonId(lesson.id());

            // 장바구니 요청 수량이 재고량보다 크지 않은지 확인
            if (!isLessonAddableToCart(currentCart, dbLesson, form)) {
                throw new CustomException(ExceptionCode.NOT_ENOUGH_LESSON_SLOT_QUANTITY);
            }

            // 레슨 및 슬롯 수량 업데이트
            updateOrAddLessonToCart(currentCart, form);
        }

        return redisCartService.putCart(authenticatedUser.getId(), currentCart);
    }

    public Cart replaceWholeCartWithUpdatedCart(Cart updatedCart) {
        UserAccount authenticatedUser = authenticatedUserService.getAuthenticatedUser();

        validateCartContents(updatedCart);
        redisCartService.putCart(authenticatedUser.getId(), updatedCart);
        return returnCart(authenticatedUser.getId());
    }

    public void removeLessonFromCart(Long lessonId) {
        UserAccount authenticatedUser = authenticatedUserService.getAuthenticatedUser();

        // 현재 장바구니 불러오기
        Cart currentCart = redisCartService.getCart(authenticatedUser.getId());
        validateCartContents(currentCart);

        // 장바구니에서 일치하는 레슨 찾기
        Optional<Cart.Lesson> optionalLessonInCart = currentCart.getLessons().stream()
                .filter(lesson -> lesson.getId().equals(lessonId))
                .findFirst();

        // 장바구니에서 일치하는 레슨을 삭제
        optionalLessonInCart.ifPresent(lesson -> currentCart.getLessons().remove(lesson));

        // 변경된 장바구니 저장
        redisCartService.putCart(authenticatedUser.getId(), currentCart);
    }

    public void validateCartContents(Cart cart) {
        // 장바구니에 담긴 레슨 불러오기
        for (Cart.Lesson cartLesson : cart.getLessons()) {
            // DB에 존재하는 레슨인지 확인
            Lesson dbLesson = lessonSearchService.getByLessonId(cartLesson.getId());

            // key-value 맵으로 slot-slotId 를 변환
            Map<Long, LessonSlot> dbSlotMap = dbLesson.getLessonSlots()
                    .stream().collect(Collectors.toMap(LessonSlot::getId, slot -> slot));

            // DB에 존재하는 슬롯인지 확인
            for (Cart.Lesson.LessonSlot cartSlot : cartLesson.getLessonSlots()) {
                LessonSlot dbSlot = dbSlotMap.get(cartSlot.getId());
                if (dbSlot == null) {
                    throw new CustomException(ExceptionCode.LESSON_SLOT_NOT_FOUND);
                }

                // DB에 존재하는 슬롯의 현재 재고보다 카트에 담아둔 재고가 큰 경우 예외 발생
                if (dbSlot.getQuantity() < cartSlot.getQuantity()) {
                    throw new CustomException(ExceptionCode.NOT_ENOUGH_LESSON_SLOT_QUANTITY);
                }
            }
        }
    }

    private void updateOrAddLessonToCart(Cart cart, CartDto form) {
        // form 으로 전달된 레슨 id와 동일한 레슨이 장바구니에 있는지 확인
        Optional<Cart.Lesson> optionalCartLesson = cart.getLessons().stream()
                .filter(lesson -> lesson.getId().equals(form.customerId()))
                .findFirst();

        // form 으로 전달된 레슨이 장바구니에 있는 레슨인 경우
        if (optionalCartLesson.isPresent()) {
            Cart.Lesson cartLesson = optionalCartLesson.get();

            // key-value 맵으로 해당 레슨 슬롯을 slot-slotId 로 변환
            Map<Long, Cart.Lesson.LessonSlot> cartSlotMap = cartLesson.getLessonSlots().stream()
                    .collect(Collectors.toMap(Cart.Lesson.LessonSlot::getId, slot -> slot));

            // form 으로 전달된 레슨 슬롯을 리스트로 변환
            List<Cart.Lesson.LessonSlot> formSlot = form.lessons().stream()
                    .flatMap(lesson -> lesson.lessonSlots().stream())
                    .map(Cart.Lesson.LessonSlot::fromDto)
                    .toList();

            // form 으로 전달된 레슨 슬롯과 장바구니에 있는 레슨 슬롯을 비교
            for (Cart.Lesson.LessonSlot slot : formSlot) {
                Cart.Lesson.LessonSlot cartSlot = cartSlotMap.get(slot.getId());

                // 장바구니에 해당 슬롯이 없으면 추가
                if (cartSlot == null) {
                    cartLesson.getLessonSlots().add(slot);
                } else { // 장바구니에 이미 같은 슬롯이 있으면 수량만 증가
                    cartSlot.setQuantity(cartSlot.getQuantity() + slot.getQuantity());
                }
            }
        } else {
            // form 으로 전달된 레슨이 장바구니에 없는 레슨인 경우 추가
            for (CartDto.Lesson formLesson : form.lessons()) {
                Cart.Lesson newLesson = Cart.Lesson.fromDto(formLesson);
                cart.getLessons().add(newLesson);
            }
        }
    }

    public Cart returnCart(Long customerId) {
        Cart refreshedCart = refreshCart(redisCartService.getCart(customerId));
        refreshedCart.setCustomerId(customerId);

        // 메시지를 포함한 장바구니 생성 (refreshedCart 내용을 복사)
        Cart cartWithMessages = cloneCart(refreshedCart);

        // 메시지 없이 변동 사항을 처리한 장바구니 저장
        refreshedCart.clearMessages();
        redisCartService.putCart(customerId, refreshedCart);

        return cartWithMessages;
    }

    protected Cart refreshCart(Cart cart) {
        // DB에서 레슨 정보를 미리 로드하여 map으로 변환
        Map<Long, Lesson> dbLessonMap = lessonSearchService.getListByLessonIds(
                        cart.getLessons().stream().map(Cart.Lesson::getId).collect(Collectors.toList()))
                .stream().collect(Collectors.toMap(Lesson::getId, lesson -> lesson));

        // 삭제 대상인 레슨을 담을 리스트
        List<Cart.Lesson> lessonsToRemove = new ArrayList<>();

        // 장바구니에 담긴 레슨을 하나씩 꺼내서
        for (Cart.Lesson cartLesson : cart.getLessons()) {
            Lesson dbLesson = dbLessonMap.get(cartLesson.getId());

            // 실제 db에 없으면 장바구니에 있는 레슨을 삭제 대상 리스트에 추가하고 메시지 기록
            if (dbLesson == null) {
                lessonsToRemove.add(cartLesson);
                cart.addMessages(cartLesson.getTitle() + " 레슨이 더 이상 존재하지 않아 삭제되었습니다.");
                continue;
            }

            // 슬롯 처리 로직을 메소드로 분리
            List<String> slotMessages = processSlots(cartLesson, dbLesson);
            if (!slotMessages.isEmpty()) {
                cart.addMessages(String.join("\n", slotMessages));
            }

            // 슬롯이 모두 삭제된 레슨을 삭제 대상에 추가
            if (cartLesson.getLessonSlots().isEmpty()) {
                lessonsToRemove.add(cartLesson);
                cart.addMessages(cartLesson.getTitle() + " 선택 가능한 슬롯이 없어 레슨이 삭제되었습니다.");
            }
        }

        // 삭제 대상 리스트의 레슨은 장바구니에서 일괄 삭제
        cart.getLessons().removeAll(lessonsToRemove);

        return cart;
    }


    private Cart cloneCart(Cart originalCart) {
        Cart clonedCart = new Cart();
        clonedCart.setCustomerId(originalCart.getCustomerId());
        clonedCart.setLessons(originalCart.getLessons());
        clonedCart.setMessages(originalCart.getMessages());
        return clonedCart;
    }

    private List<String> processSlots(Cart.Lesson cartLesson, Lesson dbLesson) {
        List<String> messages = new ArrayList<>();
        List<Cart.Lesson.LessonSlot> slotsToRemove = new ArrayList<>();
        Map<Long, LessonSlot> dbSlotMap = dbLesson.getLessonSlots().stream()
                .collect(Collectors.toMap(LessonSlot::getId, slot -> slot));
        for (Cart.Lesson.LessonSlot cartLessonSlot : cartLesson.getLessonSlots()) {
            LessonSlot dbSlot = dbSlotMap.get(cartLessonSlot.getId());

            if (dbSlot == null) {
                slotsToRemove.add(cartLessonSlot);
                messages.add(cartLessonSlot.getStartTime() + " 슬롯이 더 이상 존재하지 않아 삭제되었습니다.");
                continue;
            }

            // 가격 및 수량 변화 확인
            messages.addAll(checkPriceAndQuantityChanges(cartLesson, cartLessonSlot, dbLesson, dbSlot));
        }

        // 삭제 대상 슬롯 처리
        cartLesson.getLessonSlots().removeAll(slotsToRemove);

        return messages;
    }

    private List<String> checkPriceAndQuantityChanges(Cart.Lesson cartLesson, Cart.Lesson.LessonSlot cartLessonSlot,
                                                      Lesson dbLesson, LessonSlot dbSlot) {
        List<String> messages = new ArrayList<>();
        boolean isPriceChanged = false, isQuantityNotEnough = false;

        // 가격이 변경된 경우, 갱신하고 플래그 설정
        int quantityBeforeUpdate = cartLessonSlot.getQuantity();
        if (cartLessonSlot.getQuantity() > dbSlot.getQuantity()) {
            isQuantityNotEnough = true;
            cartLessonSlot.setQuantity(dbSlot.getQuantity());
        }

        // 재고량이 부족한 경우, 갱신하고 플래그 설정
        int priceBeforeUpdate = cartLesson.getPrice();
        if (!cartLesson.getPrice().equals(dbLesson.getPrice())) {
            isPriceChanged = true;
            cartLesson.setPrice(dbLesson.getPrice());
        }

        // 메시지 생성
        if (isPriceChanged && isQuantityNotEnough) {
            messages.add(cartLessonSlot.getStartTime() + "의 가격과 재고량이 변동되어 장바구니 내역이 수정되었습니다.");
            messages.add("[기존 가격] -> " + priceBeforeUpdate + " [변동된 가격] -> " + cartLesson.getPrice());
            messages.add("[기존 수량] -> " + quantityBeforeUpdate + " [변동된 수량] -> " + cartLessonSlot.getQuantity());
        } else if (isPriceChanged) {
            messages.add(cartLesson.getTitle() + " 레슨의 가격이 변동되어 장바구니 내역이 수정되었습니다.");
            messages.add("[기존 가격] -> " + priceBeforeUpdate + " [변동된 가격] -> " + cartLesson.getPrice());
        } else if (isQuantityNotEnough) {
            messages.add(cartLessonSlot.getStartTime() + " 슬롯의 재고량이 변동되어 장바구니 내역이 수정되었습니다.");
            messages.add("[기존 수량] -> " + quantityBeforeUpdate + " [변동된 수량] -> " + cartLessonSlot.getQuantity());
        }

        return messages;
    }

    private boolean isLessonAddableToCart(Cart cart, Lesson lesson, CartDto form) {
        // 장바구니에 해당 레슨이 이미 있는지 확인
        Cart.Lesson optionalCartLesson = cart.getLessons().stream()
                .filter(optionalLesson -> optionalLesson.getId().equals(form.customerId()))
                .findFirst()
                .orElse(null);

        // 장바구니에 해당 레슨이 없으면 추가 가능 true
        if (optionalCartLesson == null) {
            return true;
        }

        // 장바구니에 해당 레슨이 있으면 슬롯 재고를 확인
        // 장바구니 슬롯과 슬롯 수량 map
        Map<Long, Integer> cartSlotQuantityMap = optionalCartLesson.getLessonSlots().stream()
                .collect(Collectors.toMap(Cart.Lesson.LessonSlot::getId, Cart.Lesson.LessonSlot::getQuantity));
        // 데이터베이스 슬롯과 슬롯 수량 map
        Map<Long, Integer> dbSlotQuantityMap = lesson.getLessonSlots().stream()
                .collect(Collectors.toMap(LessonSlot::getId, LessonSlot::getQuantity));

        // 추가하려는 레슨 슬롯과 장바구니에 있는 슬롯의 재고를 비교하여, 실제 db 재고가 충분한지 확인
        return form.lessons().stream()
                .flatMap(lessons -> lessons.lessonSlots().stream())
                .allMatch(formSlot ->
                        {
                            Integer cartSlotQuantity = cartSlotQuantityMap.getOrDefault(formSlot.id(), 0);
                            Integer dbSlotQuantity = dbSlotQuantityMap.getOrDefault(formSlot.id(), 0);

                            return formSlot.quantity() + cartSlotQuantity <= dbSlotQuantity;
                        } // 추가하려는 수량과 장바구니에 이미 담긴 수량의 합이 실제 db 재고를 넘어서지 않으면 true 반환
                );
    }

}
