package com.learnhive.lessonservice.service.customer;

import com.learnhive.lessonservice.domain.lesson.Lesson;
import com.learnhive.lessonservice.domain.lesson.LessonSlot;
import com.learnhive.lessonservice.domain.redis.Cart;
import com.learnhive.lessonservice.domain.user.UserAccount;
import com.learnhive.lessonservice.dto.LessonCartRequestDto;
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

    public Cart addLessonToCartFromLessonPage(LessonCartRequestDto form) {
        UserAccount authenticatedUser = authenticatedUserService.getAuthenticatedUser();

        // 슬롯 선택을 하지 않은 레슨을 장바구니에 담으려고 하는 경우 예외 처리
        if (form.lessonSlots() == null || form.lessonSlots().isEmpty()) {
            throw new CustomException(ExceptionCode.INVALID_LESSON_SLOT);
        }

        // 레슨 정보 조회
        Lesson dbLesson = lessonSearchService.getByLessonId(form.id());
        if (dbLesson == null) {
            throw new CustomException(ExceptionCode.LESSON_NOT_FOUND);
        }

        // 해당 고객의 장바구니를 가져옴 (없으면 새 장바구니 생성)
        Cart currentCart = redisCartService.getCart(authenticatedUser.getId());

        // 장바구니 요청 수량이 재고량보다 크지 않은지 확인
        assert currentCart != null;
        if (!isLessonAddableToCart(currentCart, dbLesson, form)) {
            throw new CustomException(ExceptionCode.NOT_ENOUGH_LESSON_SLOT_QUANTITY);
        }

        // 레슨 및 슬롯 수량 업데이트
        updateOrAddLessonToCart(currentCart, form);

        return redisCartService.putCart(authenticatedUser.getId(), currentCart);
    }

    public Cart replaceWholeCartWithUpdatedCart(Cart updatedCart) {
        UserAccount authenticatedUser = authenticatedUserService.getAuthenticatedUser();

        validateCartContents(updatedCart);
        redisCartService.putCart(authenticatedUser.getId(), updatedCart);
        return returnCart(authenticatedUser.getId());
    }

    public Cart removeLessonFromCart(Long lessonId) {
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

        // 변경된 장바구니 저장 및 반환
        redisCartService.putCart(authenticatedUser.getId(), currentCart);
        return returnCart(authenticatedUser.getId());
    }

    public void validateCartContents(Cart cart) {
        // 장바구니에 담긴 레슨 불러오기
        for (Cart.Lesson cartLesson : cart.getLessons()) {
            // DB에 존재하는 레슨인지 확인
            Lesson dbLesson = lessonSearchService.getByLessonId(cartLesson.getId());
            if (dbLesson == null) {
                throw new CustomException(ExceptionCode.LESSON_NOT_FOUND);
            }

            // key-value 맵으로 slot-slotId 를 변환
            Map<Long, LessonSlot> dbSlotMap = dbLesson.getLessonSlots()
                    .stream().collect(Collectors.toMap(LessonSlot::getId, slot -> slot));

            // DB에 존재하는 슬롯인지 확인
            for (Cart.LessonSlot cartSlot : cartLesson.getLessonSlots()) {
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

    private void updateOrAddLessonToCart(Cart cart, LessonCartRequestDto form) {
        // form 으로 전달된 레슨 id와 동일한 레슨이 장바구니에 있는지 확인
        Optional<Cart.Lesson> optionalCartLesson = cart.getLessons().stream()
                .filter(lesson -> lesson.getId().equals(form.id()))
                .findFirst();

        // form 으로 전달된 레슨이 장바구니에 있는 레슨인 경우
        if (optionalCartLesson.isPresent()) {
            Cart.Lesson cartLesson = optionalCartLesson.get();

            // key-value 맵으로 해당 레슨 슬롯을 slot-slotId 로 변환
            Map<Long, Cart.LessonSlot> cartSlotMap = cartLesson.getLessonSlots().stream()
                    .collect(Collectors.toMap(Cart.LessonSlot::getId, slot -> slot));

            // form 으로 전달된 레슨 슬롯을 리스트로 변환
            List<Cart.LessonSlot> formSlot = form.lessonSlots().stream()
                    .map(Cart.LessonSlot::fromRequestForm).toList();

            // form 으로 전달된 레슨 슬롯과 장바구니에 있는 레슨 슬롯을 비교
            for (Cart.LessonSlot slot : formSlot) {
                Cart.LessonSlot cartSlot = cartSlotMap.get(slot.getId());

                // 장바구니에 해당 슬롯이 없으면 추가
                if (cartSlot == null) {
                    cartLesson.getLessonSlots().add(slot);
                } else { // 장바구니에 이미 같은 슬롯이 있으면 수량만 증가
                    cartSlot.setQuantity(cartSlot.getQuantity() + slot.getQuantity());
                }
            }
        } else { // form 으로 전달된 레슨이 장바구니에 없는 레슨인 경우
            Cart.Lesson newLesson = Cart.Lesson.fromRequestForm(form);
            cart.getLessons().add(newLesson);
        }
    }

    public Cart returnCart(Long customerId) {
        Cart newCart = refreshCart(redisCartService.getCart(customerId));
        newCart.setCustomerId(customerId);

        // 새로고침한 장바구니 내역을 복사한 객체 생성
        Cart cartWithMessages = new Cart();
        cartWithMessages.setCustomerId(customerId);
        cartWithMessages.setLessons(newCart.getLessons());
        cartWithMessages.setMessages(newCart.getMessages());

        // 새로고침한 장바구니 내역은 변동 사항 메시지를 제거한 후 저장
        newCart.setMessages(new ArrayList<>());
        redisCartService.putCart(customerId, newCart);

        System.out.println("cartWithMessages Products Size: " + cartWithMessages.getLessons().size());

        return cartWithMessages;
    }

    protected Cart refreshCart(Cart cart) {
        // 장바구니에 담긴 레슨들의 id를 통해서 실제 db에 있는 레슨 정보를 불러와 map으로 변환
        Map<Long, Lesson> dbLessonMap = lessonSearchService.getListByLessonIds(
                        cart.getLessons().stream().map(Cart.Lesson::getId).collect(Collectors.toList()))
                .stream().collect(Collectors.toMap(Lesson::getId, lesson -> lesson));

        // 삭제 대상인 레슨을 담을 리스트
        List<Cart.Lesson> lessonsToRemove = new ArrayList<>();

        // 장바구니에 담긴 레슨을 하나씩 꺼내서
        for (Cart.Lesson cartLesson : cart.getLessons()) {
            // 실제 db에 있는 레슨인지 확인
            Lesson dbLesson = dbLessonMap.get(cartLesson.getId());

            // 실제 db에 없으면 장바구니에 있는 레슨을 삭제 대상 리스트에 추가하고 메시지 기록
            if (dbLesson == null) {
                lessonsToRemove.add(cartLesson);
                cart.addMessage(cartLesson.getTitle() + " 레슨이 더 이상 존재하지 않아 삭제되었습니다.");
                continue;
            }

            // 장바구니에 담긴 레슨 슬롯들의 id를 통해서 실제 db에 있는 레슨 슬롯 정보를 불러와 map으로 변환
            Map<Long, LessonSlot> dbSlotMap = dbLesson.getLessonSlots().stream()
                    .collect(Collectors.toMap(LessonSlot::getId, slot -> slot));

            // 장바구니에 담긴 레슨의 변동 사항을 기록할 임시 메시지 리스트
            List<String> temporaryMessages = new ArrayList<>();

            // 삭제 대상인 슬롯을 담을 리스트
            List<Cart.LessonSlot> slotsToRemove = new ArrayList<>();

            // 장바구니에 담긴 슬롯을 하나씩 꺼내서
            for (Cart.LessonSlot cartLessonSlot : cartLesson.getLessonSlots()) {
                // 실제 db에 있는 슬롯인지 확인
                LessonSlot dbSlot = dbSlotMap.get(cartLessonSlot.getId());

                // 실제 db에 없으면 장바구니에 있는 슬롯을 삭제 대상 리스트에 추가하고 메시지 기록
                if (dbSlot == null) {
                    slotsToRemove.add(cartLessonSlot);
                    cart.addMessage(cartLessonSlot.getStartTime() + " 슬롯이 더 이상 존재하지 않아 삭제되었습니다.");
                    continue;
                }

                // 가격 또는 재고량이 변경된 경우, 이를 확인하고 장바구니 정보를 갱신
                boolean isQuantityNotEnough = false, isPriceChanged = false;

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

                // 변동 사항을 기록하는 메시지 생성
                if (isPriceChanged && isQuantityNotEnough) {
                    temporaryMessages.add(cartLessonSlot.getStartTime() + "의 가격과 재고량이 변동되어 장바구니 내역이 수정되었습니다.");
                    temporaryMessages.add("[기존 가격] -> " + priceBeforeUpdate + "[변동된 가격] -> " + cartLesson.getPrice());
                    temporaryMessages.add("[기존 수량] -> " + quantityBeforeUpdate + "[변동된 수량] -> " + cartLessonSlot.getQuantity());
                } else if (isPriceChanged) {
                    temporaryMessages.add(cartLesson.getPrice() + "레슨의 가격이 변동되어 장바구니 내역이 수정되었습니다.");
                    temporaryMessages.add("[기존 가격] -> " + priceBeforeUpdate + " [변동된 가격] -> " + cartLesson.getPrice());
                } else if (isQuantityNotEnough) {
                    temporaryMessages.add(cartLessonSlot.getStartTime() + "시간대 슬롯의 재고량이 변동되어 장바구니 내역이 수정되었습니다.");
                    temporaryMessages.add("[기존 수량] -> " + quantityBeforeUpdate + "[변동된 수량] -> " + cartLessonSlot.getQuantity());
                }
            }

            // 삭제 대상 리스트의 슬롯은 장바구니에서 일괄 삭제 처리
            cartLesson.getLessonSlots().removeAll(slotsToRemove);

            // 슬롯이 모두 삭제된 레슨은 레슨 자체도 제거하고 메시지 기록
            if (cartLesson.getLessonSlots().isEmpty()) {
                lessonsToRemove.add(cartLesson);
                cart.addMessage(cartLesson.getTitle() + " 선택 가능한 슬롯이 없어 레슨이 삭제 처리되었습니다.");
            } else if (!temporaryMessages.isEmpty()) {
                StringBuilder builder = new StringBuilder();
                builder.append(cartLesson.getTitle()).append(" - 레슨 변동 사항:\n");
                for (String message : temporaryMessages) {
                    builder.append(message).append("\n");
                }
                cart.addMessage(builder.toString());
            }
        }

        // 삭제 대상 리스트의 레슨은 장바구니에서 일괄 삭제
        cart.getLessons().removeAll(lessonsToRemove);

        return cart;
    }

    private boolean isLessonAddableToCart(Cart cart, Lesson lesson, LessonCartRequestDto form) {
        // 장바구니에 해당 레슨이 이미 있는지 확인
        Cart.Lesson optionalCartLesson = cart.getLessons().stream()
                .filter(optionalLesson -> optionalLesson.getId().equals(form.id()))
                .findFirst()
                .orElse(null);

        // 장바구니에 해당 레슨이 없으면 추가 가능 true
        if (optionalCartLesson == null) {
            return true;
        }

        // 장바구니에 해당 레슨이 있으 슬롯 재고를 확인
        // 장바구니 슬롯과 슬롯 수량 map
        Map<Long, Integer> cartSlotQuantityMap = optionalCartLesson.getLessonSlots().stream()
                .collect(Collectors.toMap(Cart.LessonSlot::getId, Cart.LessonSlot::getQuantity));
        // 데이터베이스 슬롯과 슬롯 수량 map
        Map<Long, Integer> dbSlotQuantityMap = lesson.getLessonSlots().stream()
                .collect(Collectors.toMap(LessonSlot::getId, LessonSlot::getQuantity));

        // 추가하려는 레슨 슬롯과 장바구니에 있는 슬롯의 재고를 비교하여, 실제 db 재고가 충분한지 확인
        return form.lessonSlots().stream().
                anyMatch(formSlot ->
                        {
                            Integer cartSlotQuantity = cartSlotQuantityMap.getOrDefault(formSlot.id(), 0);
                            Integer dbSlotQuantity = dbSlotQuantityMap.getOrDefault(formSlot.id(), 0);

                            return formSlot.quantity() + cartSlotQuantity <= dbSlotQuantity;
                        } // 추가하려는 수량과 장바구니에 이미 담긴 수량의 합이 실제 db 재고를 넘어서지 않으면 true 반환
                );
    }

}
