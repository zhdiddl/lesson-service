package com.learnhive.lessonservice.service;

import com.learnhive.lessonservice.domain.lesson.Lesson;
import com.learnhive.lessonservice.domain.lesson.LessonSlot;
import com.learnhive.lessonservice.domain.redis.Cart;
import com.learnhive.lessonservice.domain.user.UserAccount;
import com.learnhive.lessonservice.domain.user.UserRole;
import com.learnhive.lessonservice.dto.CartDto;
import com.learnhive.lessonservice.security.AuthenticatedUserService;
import com.learnhive.lessonservice.service.customer.CustomerCartService;
import com.learnhive.lessonservice.service.redis.RedisCartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

class CustomerCartServiceTest {

    @InjectMocks
    private CustomerCartService customerCartService;

    @Mock
    private AuthenticatedUserService authenticatedUserService;

    @Mock
    private LessonSearchService lessonSearchService;

    @Mock
    private RedisCartService redisCartService;

    private UserAccount mockUser;
    private Lesson mockLesson;
    private Cart mockCart;
    private CartDto mockCartDto;

    @BeforeEach
    void setUp() {
        initMocks(this);

        mockUser = UserAccount.builder()
                .id(1L)
                .username("testUser")
                .userPassword("password123")
                .email("testuser@example.com")
                .userRole(UserRole.CUSTOMER)
                .emailVerified(true)
                .build();

        List<LessonSlot> lessonSlots = new ArrayList<>();
        lessonSlots.add(LessonSlot.builder()
                .id(1L)
                .coach(mockUser)
                .lesson(mockLesson)
                .startTime(LocalDateTime.now().plusDays(1))
                .quantity(5)
                .build());

        mockLesson = Lesson.builder()
                .id(1L)
                .title("Test Lesson")
                .price(1000)
                .lessonSlots(lessonSlots)
                .build();

        // Cart Dto
        List<CartDto.Lesson> lessonDtoList = new ArrayList<>();
        List<CartDto.Lesson.LessonSlot> lessonSlotsDtoList = new ArrayList<>();

        lessonSlotsDtoList.add(new CartDto.Lesson.LessonSlot(1L, LocalDateTime.now().plusDays(1), 2));  // 슬롯 추가
        CartDto.Lesson lessonDto = new CartDto.Lesson(1L, 1L, "Test Lesson", 1000, "Test Description", lessonSlotsDtoList);
        lessonDtoList.add(lessonDto);

        mockCartDto = new CartDto(1L, lessonDtoList, new ArrayList<>());

        // Cart
        mockCart = new Cart(mockUser.getId());
//        mockCart.setLessons(new ArrayList<>());  // 빈 lessons 리스트 초기화
    }

    @Test
    @DisplayName("장바구니에 레슨 상품을 추가한다.")
    void testAddLessonToCartFromLessonPage_Success() {
        // given
        when(authenticatedUserService.getAuthenticatedUser()).thenReturn(mockUser);
        when(lessonSearchService.getByLessonId(anyLong())).thenReturn(mockLesson);
        when(redisCartService.getCart(anyLong())).thenReturn(mockCart);
        when(redisCartService.putCart(anyLong(), any(Cart.class))).thenReturn(mockCart);

        // when
        Cart updatedCart = customerCartService.addLessonToCartFromLessonPage(mockCartDto);

        // then
        assertNotNull(updatedCart);
        verify(redisCartService, times(1)).putCart(anyLong(), any(Cart.class));
    }


    @Test
    @DisplayName("장바구니에서 레슨 상품 제거한다.")
    void testRemoveLessonFromCart_Success() {
        // given
        Cart.Lesson cartLesson = Cart.Lesson.builder()
                .id(1L)
                .coachId(1L)
                .title("Test Lesson")
                .price(1000)
                .lessonSlots(new ArrayList<>())
                .build();
        mockCart.getLessons().add(cartLesson);

        when(authenticatedUserService.getAuthenticatedUser()).thenReturn(mockUser);
        when(redisCartService.getCart(anyLong())).thenReturn(mockCart);
        when(lessonSearchService.getByLessonId(anyLong())).thenReturn(mockLesson);

        // when
        customerCartService.removeLessonFromCart(1L);

        // then
        assertTrue(mockCart.getLessons().isEmpty());
        verify(redisCartService, times(1)).putCart(anyLong(), any(Cart.class));
    }

    @Test
    @DisplayName("장바구니 내역을 수정 후 저장한다")
    void testReplaceWholeCartWithUpdatedCart_Success() {
        // given
        when(authenticatedUserService.getAuthenticatedUser()).thenReturn(mockUser);
        when(lessonSearchService.getByLessonId(anyLong())).thenReturn(mockLesson);
        when(redisCartService.getCart(anyLong())).thenReturn(mockCart);
        when(redisCartService.putCart(anyLong(), any(Cart.class))).thenReturn(mockCart);

        // when
        Cart updatedCart = customerCartService.replaceWholeCartWithUpdatedCart(mockCart);

        // then - 수정한 장바구니를 우선 저장하고, 임시 메시지를 제거 후 정리된 장바구니를 다시 한  저장
        assertNotNull(updatedCart);
        verify(redisCartService, times(2)).putCart(anyLong(), any(Cart.class));
    }

    @Test
    @DisplayName("장바구니 내역을 조회한다.")
    void testReturnCart_Success() {
        // given
        when(authenticatedUserService.getAuthenticatedUser()).thenReturn(mockUser);
        when(redisCartService.getCart(anyLong())).thenReturn(mockCart);

        // when
        Cart returnedCart = customerCartService.returnCart(mockUser.getId());

        // then
        assertNotNull(returnedCart);
        assertEquals(mockUser.getId(), returnedCart.getCustomerId());
        verify(redisCartService, times(1)).getCart(anyLong());
        verify(redisCartService, times(1)).putCart(anyLong(), any(Cart.class));  // 메시지 없는 상태로 저장되는지 확인
    }

    @Test
    @DisplayName("장바구니 조회시 DB에 존재하는 상품인지 확인한다.")
    void testValidateCartContents_ValidCart() {
        Cart.Lesson cartLesson = Cart.Lesson.builder()
                .id(1L)
                .coachId(1L)
                .title("Test Lesson")
                .price(1000)
                .lessonSlots(new ArrayList<>())
                .build();

        mockCart.getLessons().add(cartLesson);

        when(lessonSearchService.getByLessonId(anyLong())).thenReturn(mockLesson);

        // when
        customerCartService.validateCartContents(mockCart);

        // then
        verify(lessonSearchService, times(1)).getByLessonId(anyLong());
    }
}
