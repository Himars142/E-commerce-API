package com.example.demo3.service.impl;

import com.example.demo3.dto.CreateOrderRequestDTO;
import com.example.demo3.dto.OrderEntityDTO;
import com.example.demo3.dto.PageableResponseOrdersDTO;
import com.example.demo3.dto.UpdateOrderStatusRequestDTO;
import com.example.demo3.entity.*;
import com.example.demo3.exception.BadRequestException;
import com.example.demo3.exception.ForbiddenException;
import com.example.demo3.exception.NotFoundException;
import com.example.demo3.mapper.OrderMapper;
import com.example.demo3.repository.OrderItemRepository;
import com.example.demo3.repository.OrderRepository;
import com.example.demo3.service.impl.testutil.BaseServiceTest;
import org.junit.jupiter.api.*;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class OrderServiceImplTest extends BaseServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ProductServiceImpl productService;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private AuthServiceImpl authService;
    @Mock
    private CartServiceImpl cartService;
    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private OrderServiceImpl underTest;

    private static final UserEntity USER = new UserEntity();
    private static final Page<OrderEntity> PAGE_EMPTY = Page.empty();
    private static final OrderEntity ORDER = new OrderEntity();
    private static final OrderEntity ANOTHER_USER_ORDER = new OrderEntity();

    @BeforeAll
    static void setUp() {
        USER.setId(1L);
        ORDER.setUser(USER);
        ORDER.setId(1L);
        ANOTHER_USER_ORDER.setUser(new UserEntity());
        ANOTHER_USER_ORDER.setId(2L);
    }

    @BeforeEach
    void clearRole() {
        USER.setRole(UserRole.ROLE_CUSTOMER);
    }

    @Nested
    @DisplayName("Create order tests")
    class CreateOrder {

        private static final CreateOrderRequestDTO REQUEST = new CreateOrderRequestDTO();
        private static final ProductEntity PRODUCT = new ProductEntity();
        private static final CartEntity CART = new CartEntity();
        private static final CartItemEntity CART_ITEM = new CartItemEntity();

        @BeforeAll
        static void setUp() {
            USER.setCart(CART);
            CART.setUser(USER);
            CART_ITEM.setCart(CART);
            CART_ITEM.setProduct(PRODUCT);
        }

        @Test
        @DisplayName("Should create order")
        void createOrder_ShouldCreateOrder() {
            CART.setCartItems(List.of(CART_ITEM));

            when(authService.validateTokenAndGetUser(eq(TOKEN), anyString())).thenReturn(USER);
            when(orderMapper.createOrder(USER, REQUEST, List.of(CART_ITEM))).thenReturn(ORDER);
            when(orderRepository.save(ORDER)).thenReturn(ORDER);

            underTest.createOrder(TOKEN, REQUEST, USER_AGENT);

            InOrder inOrder = inOrder(orderMapper, orderRepository, productService, cartService);
            inOrder.verify(orderMapper).createOrder(USER, REQUEST, List.of(CART_ITEM));
            inOrder.verify(orderRepository).save(ORDER);
            inOrder.verify(productService).decreaseStockForOrderItems(List.of(CART_ITEM));
            inOrder.verify(cartService).deleteAllByCartId(USER.getCart().getId());
        }

        @Test
        @DisplayName("Should throw not found exception")
        void createOrder_ShouldThrowNotFoundException() {
            CART.setCartItems(List.of());

            when(authService.validateTokenAndGetUser(eq(TOKEN), anyString())).thenReturn(USER);

            assertThrows(NotFoundException.class, () -> underTest.createOrder(TOKEN, REQUEST, USER_AGENT));

            verify(authService).validateTokenAndGetUser(eq(TOKEN), anyString());
        }

        @Test
        @DisplayName("Should throw not found exception because cart is empty")
        void createOrder_WhenCartIsEmpty_ShouldThrowNotFoundException() {
            CART.setCartItems(List.of());

            when(authService.validateTokenAndGetUser(eq(TOKEN), anyString())).thenReturn(USER);

            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> underTest.createOrder(TOKEN, REQUEST, USER_AGENT));

            assertThat(exception).isNotNull();

            verify(authService).validateTokenAndGetUser(eq(TOKEN), anyString());
        }
    }

    @Nested
    @DisplayName("Get user orders tests")
    class GetUserOrders {

        @Test
        @DisplayName("should return users orders")
        void getUserOrders() {
            PageableResponseOrdersDTO response = new PageableResponseOrdersDTO();

            when(authService.validateTokenAndGetUser(eq(TOKEN), anyString())).thenReturn(USER);
            when(orderRepository.findByUserId(USER.getId(), PageRequest.of(DEFAULT_PAGE, DEFAULT_PAGE_SIZE)))
                    .thenReturn(PAGE_EMPTY);
            when(orderMapper.createPageableResponseOrdersDTO(PAGE_EMPTY)).thenReturn(response);

            PageableResponseOrdersDTO result = underTest
                    .getUserOrders(TOKEN, DEFAULT_PAGE, DEFAULT_PAGE_SIZE, USER_AGENT);

            assertThat(result).isNotNull().isSameAs(response);

            InOrder inOrder = inOrder(orderRepository, orderMapper);
            inOrder.verify(orderRepository).findByUserId(USER.getId(), PageRequest.of(DEFAULT_PAGE, DEFAULT_PAGE_SIZE));
            inOrder.verify(orderMapper).createPageableResponseOrdersDTO(PAGE_EMPTY);
        }
    }

    @Nested
    @DisplayName("Get order details tests")
    class GetOrderDetails {

        private static final OrderEntityDTO RESPONSE = new OrderEntityDTO();

        @Test
        @DisplayName("Should get order details")
        void getOrderDetails_ShouldGetOrderDetails() {
            when(orderRepository.findById(1L)).thenReturn(Optional.of(ORDER));
            when(authService.validateTokenAndGetUser(eq(TOKEN), anyString())).thenReturn(USER);
            when(orderMapper.toOrderEntityDTO(ORDER)).thenReturn(RESPONSE);

            OrderEntityDTO result = underTest.getOrderDetails(TOKEN, ORDER.getId(), USER_AGENT);

            assertThat(result).isNotNull().isSameAs(RESPONSE);

            InOrder inOrder = inOrder(orderRepository, orderMapper);
            inOrder.verify(orderRepository).findById(1L);
            inOrder.verify(orderMapper).toOrderEntityDTO(ORDER);
        }

        @Test
        @DisplayName("Should get order details case user admin")
        void getOrderDetails_ShouldGetOrderDetailsCaseUserAdmin() {
            USER.setRole(UserRole.ROLE_ADMIN);

            when(orderRepository.findById(1L)).thenReturn(Optional.of(ORDER));
            when(authService.validateTokenAndGetUser(eq(TOKEN), anyString())).thenReturn(USER);
            when(orderMapper.toOrderEntityDTO(ORDER)).thenReturn(RESPONSE);

            OrderEntityDTO result = underTest.getOrderDetails(TOKEN, ORDER.getId(), USER_AGENT);

            assertThat(result).isNotNull().isSameAs(RESPONSE);

            InOrder inOrder = inOrder(orderRepository, orderMapper);
            inOrder.verify(orderRepository).findById(1L);
            inOrder.verify(orderMapper).toOrderEntityDTO(ORDER);
        }

        @Test
        @DisplayName("Should throw forbidden exception")
        void getOrderDetails_ShouldThrowForbiddenException() {
            when(orderRepository.findById(2L)).thenReturn(Optional.of(ANOTHER_USER_ORDER));
            when(authService.validateTokenAndGetUser(eq(TOKEN), anyString())).thenReturn(USER);

            assertThrows(ForbiddenException.class, () -> underTest.getOrderDetails(TOKEN, ANOTHER_USER_ORDER.getId(), USER_AGENT));

            verify(orderRepository).findById(2L);
        }

        @Test
        @DisplayName("Should throw not found exception")
        void getOrderDetails_ShouldThrowNotFoundException() {
            when(orderRepository.findById(1L)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> underTest.getOrderDetails(TOKEN, 1L, USER_AGENT));

            verify(orderRepository).findById(1L);
        }
    }

    @Nested
    @DisplayName("Cancel order tests")
    class CancelOrder {

        private static final OrderEntity ORDER_CANCELED = new OrderEntity();
        private static final OrderItemEntity ORDER_ITEM = new OrderItemEntity();

        @BeforeAll
        static void setUp() {
            ORDER_CANCELED.setId(1L);
            ORDER_CANCELED.setStatus(OrderStatus.CANCELLED);
            ORDER_ITEM.setOrder(ORDER);
        }

        @Test
        @DisplayName("Should throw not found exception order not found")
        void cancelOrder_ShouldThrowNotFoundExceptionBecauseOrder() {
            when(orderRepository.findById(1L)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> underTest.cancelOrder(TOKEN, 1L, USER_AGENT));

            verify(orderRepository).findById(1L);
        }

        @Test
        @DisplayName("Should throw forbidden exception")
        void cancelOrder_ShouldThrowForbiddenException() {
            when(orderRepository.findById(2L)).thenReturn(Optional.of(ANOTHER_USER_ORDER));
            when(authService.validateTokenAndGetUser(eq(TOKEN), anyString())).thenReturn(USER);

            assertThrows(ForbiddenException.class, () -> underTest.cancelOrder(TOKEN, ANOTHER_USER_ORDER.getId(), USER_AGENT));

            verify(orderRepository).findById(2L);
        }

        @Test
        @DisplayName("Should throw bad request exception")
        void cancelOrder_ShouldThrowBadRequestException() {
            ORDER.setStatus(OrderStatus.CANCELLED);

            when(orderRepository.findById(1L)).thenReturn(Optional.of(ORDER));
            when(authService.validateTokenAndGetUser(eq(TOKEN), anyString())).thenReturn(USER);

            assertThrows(BadRequestException.class, () -> underTest.cancelOrder(TOKEN, ORDER.getId(), USER_AGENT));

            verify(orderRepository).findById(1L);
        }

        @Test
        @DisplayName("Should throw not found exception cart is empty")
        void cancelOrder_ShouldThrowNotFoundExceptionBecauseCartItems() {
            ORDER.setStatus(OrderStatus.PENDING);

            when(orderRepository.findById(1L)).thenReturn(Optional.of(ORDER));
            when(authService.validateTokenAndGetUser(eq(TOKEN), anyString())).thenReturn(USER);
            when(orderItemRepository.findByOrderId(ORDER.getId())).thenReturn(List.of());

            assertThrows(NotFoundException.class, () -> underTest.cancelOrder(TOKEN, ORDER.getId(), USER_AGENT));

            InOrder inOrder = inOrder(orderRepository, orderItemRepository);
            inOrder.verify(orderRepository).findById(1L);
            inOrder.verify(orderItemRepository).findByOrderId(ORDER.getId());
        }

        @Test
        @DisplayName("Should cancel order")
        void cancelOrder_ShouldCancelOrder() {
            ORDER.setStatus(OrderStatus.PENDING);

            when(orderRepository.findById(1L)).thenReturn(Optional.of(ORDER));
            when(authService.validateTokenAndGetUser(eq(TOKEN), anyString())).thenReturn(USER);
            when(orderItemRepository.findByOrderId(ORDER.getId())).thenReturn(List.of(ORDER_ITEM));
            when(orderMapper.cancelOrder(ORDER)).thenReturn(ORDER_CANCELED);

            underTest.cancelOrder(TOKEN, ORDER.getId(), USER_AGENT);

            InOrder inOrder = inOrder(orderRepository, orderItemRepository, productService);
            inOrder.verify(orderRepository).findById(1L);
            inOrder.verify(orderItemRepository).findByOrderId(ORDER.getId());
            inOrder.verify(productService).increaseStockForOrderItems(List.of(ORDER_ITEM));
        }

        @Test
        @DisplayName("Should cancel order case user admin")
        void cancelOrder_ShouldCancelOrderCaseUserAdmin() {
            USER.setRole(UserRole.ROLE_ADMIN);
            ORDER.setStatus(OrderStatus.PENDING);

            when(orderRepository.findById(1L)).thenReturn(Optional.of(ORDER));
            when(authService.validateTokenAndGetUser(eq(TOKEN), anyString())).thenReturn(USER);
            when(orderItemRepository.findByOrderId(ORDER.getId())).thenReturn(List.of(ORDER_ITEM));
            when(orderMapper.cancelOrder(ORDER)).thenReturn(ORDER_CANCELED);

            underTest.cancelOrder(TOKEN, ORDER.getId(), USER_AGENT);

            InOrder inOrder = inOrder(orderRepository, orderItemRepository, productService);
            inOrder.verify(orderRepository).findById(1L);
            inOrder.verify(orderItemRepository).findByOrderId(ORDER.getId());
            inOrder.verify(productService).increaseStockForOrderItems(List.of(ORDER_ITEM));
        }
    }

    @Nested
    @DisplayName("Get all orders tests")
    class GetAllOrders {

        private static final PageableResponseOrdersDTO RESPONSE = new PageableResponseOrdersDTO();

        @Test
        @DisplayName("Should return orders without status")
        void getAllOrders_ShouldReturnOrdersWithoutStatus() {
            when(orderRepository.findAll(PageRequest.of(DEFAULT_PAGE, DEFAULT_PAGE_SIZE))).thenReturn(PAGE_EMPTY);
            when(orderMapper.createPageableResponseOrdersDTO(PAGE_EMPTY)).thenReturn(RESPONSE);

            PageableResponseOrdersDTO result = underTest
                    .getAllOrders(TOKEN, DEFAULT_PAGE, DEFAULT_PAGE_SIZE, null, USER_AGENT);

            assertThat(result).isNotNull().isSameAs(RESPONSE);

            InOrder inOrder = inOrder(authService, orderRepository, orderMapper);
            inOrder.verify(authService).checkIsUserAdmin(eq(TOKEN), anyString());
            inOrder.verify(orderRepository).findAll(PageRequest.of(DEFAULT_PAGE, DEFAULT_PAGE_SIZE));
            inOrder.verify(orderMapper).createPageableResponseOrdersDTO(PAGE_EMPTY);
        }

        @Test
        @DisplayName("Should return orders with status")
        void getAllOrders_ShouldReturnOrdersWithStatus() {
            String status = "PENDING";

            when(orderRepository.findByStatus(OrderStatus.valueOf(status),
                    PageRequest.of(DEFAULT_PAGE, DEFAULT_PAGE_SIZE)))
                    .thenReturn(PAGE_EMPTY);
            when(orderMapper.createPageableResponseOrdersDTO(PAGE_EMPTY)).thenReturn(RESPONSE);

            PageableResponseOrdersDTO result = underTest
                    .getAllOrders(TOKEN, DEFAULT_PAGE, DEFAULT_PAGE_SIZE, status, USER_AGENT);

            assertThat(result).isNotNull().isSameAs(RESPONSE);

            InOrder inOrder = inOrder(authService, orderRepository, orderMapper);
            inOrder.verify(authService).checkIsUserAdmin(eq(TOKEN), anyString());
            inOrder.verify(orderRepository).findByStatus(OrderStatus.valueOf(status),
                    PageRequest.of(DEFAULT_PAGE, DEFAULT_PAGE_SIZE));
            inOrder.verify(orderMapper).createPageableResponseOrdersDTO(PAGE_EMPTY);
        }
    }

    @Nested
    @DisplayName("Update order status tests")
    class UpdateOrderStatus {

        private static final UpdateOrderStatusRequestDTO REQUEST = new UpdateOrderStatusRequestDTO();

        @Test
        @DisplayName("Should redirect to cancel order and throw not found error")
        void updateOrderStatus_ShouldRedirectToCancelOrderAndThrowNotFoundError() {
            REQUEST.setStatus(OrderStatus.CANCELLED);

            assertThrows(NotFoundException.class,
                    () -> underTest.updateOrderStatus(TOKEN, ORDER.getId(), REQUEST, USER_AGENT));
        }

        @Test
        @DisplayName("Should throw not found error order not found")
        void updateOrderStatus_ShouldThrowNotFoundErrorOrderNotFound() {
            REQUEST.setStatus(OrderStatus.PENDING);

            when(orderRepository.findById(1L)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> underTest.updateOrderStatus(TOKEN, 1L, REQUEST, USER_AGENT));

            verify(orderRepository).findById(1L);
        }

        @Test
        @DisplayName("Should throw bad request exception")
        void updateOrderStatus_ShouldThrowBadRequestException() {
            REQUEST.setStatus(OrderStatus.PENDING);
            ORDER.setStatus(OrderStatus.PENDING);

            when(orderRepository.findById(1L)).thenReturn(Optional.of(ORDER));

            assertThrows(BadRequestException.class, () -> underTest.updateOrderStatus(TOKEN, 1L, REQUEST, USER_AGENT));

            verify(orderRepository).findById(1L);
        }

        @Test
        @DisplayName("Should update order to confirmed")
        void updateOrderStatus_ShouldUpdateOrderToConfirmed() {
            REQUEST.setStatus(OrderStatus.CONFIRMED);
            ORDER.setStatus(OrderStatus.PENDING);

            when(orderRepository.findById(1L)).thenReturn(Optional.of(ORDER));

            underTest.updateOrderStatus(TOKEN, 1L, REQUEST, USER_AGENT);

            InOrder inOrder = inOrder(orderRepository, orderRepository);
            inOrder.verify(orderRepository).findById(1L);
            inOrder.verify(orderRepository).save(ORDER);
        }

        @Test
        @DisplayName("Should update order to shipped")
        void updateOrderStatus_ShouldUpdateOrderToShipped() {
            REQUEST.setStatus(OrderStatus.SHIPPED);
            ORDER.setStatus(OrderStatus.CONFIRMED);

            when(orderRepository.findById(1L)).thenReturn(Optional.of(ORDER));

            underTest.updateOrderStatus(TOKEN, 1L, REQUEST, USER_AGENT);

            InOrder inOrder = inOrder(orderRepository, orderRepository);
            inOrder.verify(orderRepository).findById(1L);
            inOrder.verify(orderRepository).save(ORDER);
        }

        @Test
        @DisplayName("Should update order to delivered")
        void updateOrderStatus_ShouldUpdateOrderToDelivered() {
            REQUEST.setStatus(OrderStatus.DELIVERED);
            ORDER.setStatus(OrderStatus.SHIPPED);

            when(orderRepository.findById(1L)).thenReturn(Optional.of(ORDER));

            underTest.updateOrderStatus(TOKEN, 1L, REQUEST, USER_AGENT);

            InOrder inOrder = inOrder(orderRepository, orderRepository);
            inOrder.verify(orderRepository).findById(1L);
            inOrder.verify(orderRepository).save(ORDER);
        }

        @Test
        @DisplayName("Should throw bad request exception invalid status transition")
        void updateOrderStatus_ShouldThrowBadRequestExceptionInvalidStatusTransition() {
            REQUEST.setStatus(OrderStatus.DELIVERED);
            ORDER.setStatus(OrderStatus.PENDING);

            when(orderRepository.findById(1L)).thenReturn(Optional.of(ORDER));

            assertThrows(BadRequestException.class,
                    () -> underTest.updateOrderStatus(TOKEN, 1L, REQUEST, USER_AGENT));

            verify(orderRepository).findById(1L);
        }

        @Test
        @DisplayName("Should throw bad request exception invalid status transition case with confirmed in request")
        void updateOrderStatus_WhenInvalidTransition_ShouldThrowBadRequestException() {
            REQUEST.setStatus(OrderStatus.DELIVERED);
            ORDER.setStatus(OrderStatus.CONFIRMED);

            when(orderRepository.findById(1L)).thenReturn(Optional.of(ORDER));

            assertThrows(BadRequestException.class,
                    () -> underTest.updateOrderStatus(TOKEN, 1L, REQUEST, USER_AGENT));

            verify(orderRepository).findById(1L);
        }
    }
}