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
import com.example.demo3.testutil.BaseServiceTest;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @Test
    void createOrder_ShouldCreateOrder() {
        String token = "valid-token";
        String userAgent = "testing";
        CreateOrderRequestDTO request = new CreateOrderRequestDTO();
        ProductEntity product = new ProductEntity();
        CartEntity cart = new CartEntity();
        CartItemEntity cartItem = new CartItemEntity();
        cartItem.setCart(cart);
        UserEntity user = new UserEntity();
        user.setCart(cart);

        cartItem.setProduct(product);
        cart.setUser(user);
        cart.setCartItems(List.of(cartItem));
        OrderEntity order = new OrderEntity();

        when(authService.validateTokenAndGetUser(eq(token), anyString())).thenReturn(user);
        when(orderMapper.createOrder(user, request, List.of(cartItem))).thenReturn(order);
        when(orderRepository.save(order)).thenReturn(order);

        underTest.createOrder(token, request, userAgent);

        verify(orderMapper).createOrder(user, request, List.of(cartItem));
        verify(orderRepository).save(order);
        verify(productService).decreaseStockForOrderItems(List.of(cartItem));
        verify(cartService).deleteAllByCartId(user.getCart().getId());
    }

    @Test
    void createOrder_ShouldThrowNotFoundException() {
        String token = "valid-token";
        String userAgent = "testing";
        CreateOrderRequestDTO request = new CreateOrderRequestDTO();
        UserEntity user = new UserEntity();

        when(authService.validateTokenAndGetUser(eq(token), anyString())).thenReturn(user);

        assertThrows(NotFoundException.class, () -> underTest.createOrder(token, request, userAgent));
    }

    @Test
    void createOrder_ShouldThrowNotFoundExceptionBecauseCartIsEmpty() {
        String token = "valid-token";
        String userAgent = "testing";
        CreateOrderRequestDTO request = new CreateOrderRequestDTO();
        CartEntity cart = new CartEntity();
        cart.setCartItems(List.of());
        UserEntity user = new UserEntity();
        user.setCart(cart);
        cart.setUser(user);

        when(authService.validateTokenAndGetUser(eq(token), anyString())).thenReturn(user);

        assertThrows(NotFoundException.class, () -> underTest.createOrder(token, request, userAgent));
    }

    @Test
    void getUserOrders() {
        String token = "valid-token";
        String userAgent = "testing";
        int page = 0;
        int size = 10;

        UserEntity user = new UserEntity();
        user.setId(1L);
        Page<OrderEntity> orderEntityPage = Page.empty();
        PageableResponseOrdersDTO response = new PageableResponseOrdersDTO();

        when(authService.validateTokenAndGetUser(eq(token), anyString())).thenReturn(user);
        when(orderRepository.findByUserId(user.getId(), PageRequest.of(page, size))).thenReturn(orderEntityPage);
        when(orderMapper.createPageableResponseOrdersDTO(orderEntityPage)).thenReturn(response);

        underTest.getUserOrders(token, page, size, userAgent);

        verify(orderRepository).findByUserId(user.getId(), PageRequest.of(page, size));
        verify(orderMapper).createPageableResponseOrdersDTO(orderEntityPage);
    }

    @Test
    void getOrderDetails_ShouldGetOrderDetails() {
        String token = "valid-token";
        String userAgent = "testing";

        UserEntity user = new UserEntity();
        user.setRole(UserRole.ROLE_CUSTOMER);
        OrderEntity order = new OrderEntity();
        order.setUser(user);
        order.setId(1L);
        OrderEntityDTO response = new OrderEntityDTO();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(authService.validateTokenAndGetUser(eq(token), anyString())).thenReturn(user);
        when(orderMapper.toOrderEntityDTO(order)).thenReturn(response);

        underTest.getOrderDetails(token, order.getId(), userAgent);

        verify(orderRepository).findById(1L);
        verify(orderMapper).toOrderEntityDTO(order);
    }

    @Test
    void getOrderDetails_ShouldGetOrderDetailsCaseUserAdmin() {
        String token = "valid-token";
        String userAgent = "testing";

        UserEntity user = new UserEntity();
        user.setRole(UserRole.ROLE_ADMIN);
        OrderEntity order = new OrderEntity();
        order.setUser(user);
        order.setId(1L);
        OrderEntityDTO response = new OrderEntityDTO();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(authService.validateTokenAndGetUser(eq(token), anyString())).thenReturn(user);
        when(orderMapper.toOrderEntityDTO(order)).thenReturn(response);

        underTest.getOrderDetails(token, order.getId(), userAgent);

        verify(orderRepository).findById(1L);
        verify(orderMapper).toOrderEntityDTO(order);
    }

    @Test
    void getOrderDetails_ShouldThrowForbiddenException() {
        String token = "valid-token";
        String userAgent = "testing";

        UserEntity user = new UserEntity();
        user.setRole(UserRole.ROLE_CUSTOMER);
        OrderEntity order = new OrderEntity();
        order.setUser(new UserEntity());
        order.setId(1L);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(authService.validateTokenAndGetUser(eq(token), anyString())).thenReturn(user);

        assertThrows(ForbiddenException.class, () -> underTest.getOrderDetails(token, order.getId(), userAgent));

        verify(orderRepository).findById(1L);
    }

    @Test
    void getOrderDetails_ShouldThrowNotFoundException() {
        String token = "valid-token";
        String userAgent = "testing";

        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> underTest.getOrderDetails(token, 1L, userAgent));

        verify(orderRepository).findById(1L);
    }

    @Test
    void cancelOrder_ShouldThrowNotFoundExceptionBecauseOrder() {
        String token = "valid-token";
        String userAgent = "testing";

        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> underTest.cancelOrder(token, 1L, userAgent));

        verify(orderRepository).findById(1L);
    }

    @Test
    void cancelOrder_ShouldThrowForbiddenException() {
        String token = "valid-token";
        String userAgent = "testing";

        UserEntity user = new UserEntity();
        user.setRole(UserRole.ROLE_CUSTOMER);
        OrderEntity order = new OrderEntity();
        order.setUser(new UserEntity());
        order.setId(1L);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(authService.validateTokenAndGetUser(eq(token), anyString())).thenReturn(user);

        assertThrows(ForbiddenException.class, () -> underTest.cancelOrder(token, order.getId(), userAgent));

        verify(orderRepository).findById(1L);
    }

    @Test
    void cancelOrder_ShouldThrowBadRequestException() {
        String token = "valid-token";
        String userAgent = "testing";

        UserEntity user = new UserEntity();
        user.setRole(UserRole.ROLE_CUSTOMER);
        OrderEntity order = new OrderEntity();
        order.setUser(user);
        order.setId(1L);
        order.setStatus(OrderStatus.CANCELLED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(authService.validateTokenAndGetUser(eq(token), anyString())).thenReturn(user);

        assertThrows(BadRequestException.class, () -> underTest.cancelOrder(token, order.getId(), userAgent));

        verify(orderRepository).findById(1L);
    }

    @Test
    void cancelOrder_ShouldThrowNotFoundExceptionBecauseCartItems() {
        String token = "valid-token";
        String userAgent = "testing";

        UserEntity user = new UserEntity();
        user.setRole(UserRole.ROLE_CUSTOMER);
        OrderEntity order = new OrderEntity();
        order.setUser(user);
        order.setId(1L);
        order.setStatus(OrderStatus.PENDING);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(authService.validateTokenAndGetUser(eq(token), anyString())).thenReturn(user);
        when(orderItemRepository.findByOrder_Id(order.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> underTest.cancelOrder(token, order.getId(), userAgent));

        verify(orderRepository).findById(1L);
        verify(orderItemRepository).findByOrder_Id(order.getId());
    }

    @Test
    void cancelOrder_ShouldCancelOrder() {
        String token = "valid-token";
        String userAgent = "testing";

        UserEntity user = new UserEntity();
        user.setRole(UserRole.ROLE_CUSTOMER);
        OrderEntity order = new OrderEntity();
        order.setUser(user);
        order.setId(1L);
        order.setStatus(OrderStatus.PENDING);
        OrderItemEntity orderItem = new OrderItemEntity();
        orderItem.setOrder(order);

        OrderEntity orderCanceled = new OrderEntity();
        orderCanceled.setId(1L);
        orderCanceled.setStatus(OrderStatus.CANCELLED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(authService.validateTokenAndGetUser(eq(token), anyString())).thenReturn(user);
        when(orderItemRepository.findByOrder_Id(order.getId())).thenReturn(Optional.of(List.of(orderItem)));
        when(orderMapper.cancelOrder(order)).thenReturn(orderCanceled);

        underTest.cancelOrder(token, order.getId(), userAgent);

        verify(orderRepository).findById(1L);
        verify(orderItemRepository).findByOrder_Id(order.getId());
        verify(productService).increaseStockForOrderItems(List.of(orderItem));
    }

    @Test
    void cancelOrder_ShouldCancelOrderCaseUserAdmin() {
        String token = "valid-token";
        String userAgent = "testing";

        UserEntity user = new UserEntity();
        user.setRole(UserRole.ROLE_ADMIN);
        OrderEntity order = new OrderEntity();
        order.setUser(user);
        order.setId(1L);
        order.setStatus(OrderStatus.PENDING);
        OrderItemEntity orderItem = new OrderItemEntity();
        orderItem.setOrder(order);

        OrderEntity orderCanceled = new OrderEntity();
        orderCanceled.setId(1L);
        orderCanceled.setStatus(OrderStatus.CANCELLED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(authService.validateTokenAndGetUser(eq(token), anyString())).thenReturn(user);
        when(orderItemRepository.findByOrder_Id(order.getId())).thenReturn(Optional.of(List.of(orderItem)));
        when(orderMapper.cancelOrder(order)).thenReturn(orderCanceled);

        underTest.cancelOrder(token, order.getId(), userAgent);

        verify(orderRepository).findById(1L);
        verify(orderItemRepository).findByOrder_Id(order.getId());
        verify(productService).increaseStockForOrderItems(List.of(orderItem));
    }

    @Test
    void getAllOrders_ShouldReturnOrdersWithoutStatus() {
        String token = "valid-token";
        String userAgent = "testing";
        int page = 0;
        int size = 10;
        String status = null;

        Page<OrderEntity> orderEntityPage = Page.empty();
        PageableResponseOrdersDTO response = new PageableResponseOrdersDTO();

        when(orderRepository.findAll(PageRequest.of(page, size))).thenReturn(orderEntityPage);
        when(orderMapper.createPageableResponseOrdersDTO(orderEntityPage)).thenReturn(response);

        underTest.getAllOrders(token, page, size, status, userAgent);

        verify(authService).checkIsUserAdmin(eq(token), anyString());
        verify(orderRepository).findAll(PageRequest.of(page, size));
        verify(orderMapper).createPageableResponseOrdersDTO(orderEntityPage);
    }

    @Test
    void getAllOrders_ShouldReturnOrdersWithStatus() {
        String token = "valid-token";
        String userAgent = "testing";
        int page = 0;
        int size = 10;
        String status = "PENDING";

        Page<OrderEntity> orderEntityPage = Page.empty();
        PageableResponseOrdersDTO response = new PageableResponseOrdersDTO();

        when(orderRepository.findByStatus(OrderStatus.valueOf(status), PageRequest.of(page, size))).thenReturn(orderEntityPage);
        when(orderMapper.createPageableResponseOrdersDTO(orderEntityPage)).thenReturn(response);

        underTest.getAllOrders(token, page, size, status, userAgent);

        verify(authService).checkIsUserAdmin(eq(token), anyString());
        verify(orderRepository).findByStatus(OrderStatus.valueOf(status), PageRequest.of(page, size));
        verify(orderMapper).createPageableResponseOrdersDTO(orderEntityPage);
    }

    @Test
    void updateOrderStatus_ShouldRedirectToCancelOrderAndThrowNotFoundError() {
        String token = "valid-token";
        String userAgent = "testing";

        OrderEntity order = new OrderEntity();
        order.setId(1L);

        UpdateOrderStatusRequestDTO request = new UpdateOrderStatusRequestDTO();
        request.setStatus(OrderStatus.CANCELLED);

        assertThrows(NotFoundException.class,
                () -> underTest.updateOrderStatus(token, order.getId(), request, userAgent));
    }

    @Test
    void updateOrderStatus_ShouldThrowNotFoundErrorOrderNotFound() {
        String token = "valid-token";
        String userAgent = "testing";

        UpdateOrderStatusRequestDTO request = new UpdateOrderStatusRequestDTO();
        request.setStatus(OrderStatus.PENDING);

        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> underTest.updateOrderStatus(token, 1L, request, userAgent));

        verify(orderRepository).findById(1L);
    }

    @Test
    void updateOrderStatus_ShouldThrowBadRequestException() {
        String token = "valid-token";
        String userAgent = "testing";

        UpdateOrderStatusRequestDTO request = new UpdateOrderStatusRequestDTO();
        request.setStatus(OrderStatus.PENDING);

        OrderEntity order = new OrderEntity();
        order.setId(1L);
        order.setStatus(OrderStatus.PENDING);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(BadRequestException.class, () -> underTest.updateOrderStatus(token, 1L, request, userAgent));

        verify(orderRepository).findById(1L);
    }

    @Test
    void updateOrderStatus_ShouldUpdateOrderToConfirmed() {
        String token = "valid-token";
        String userAgent = "testing";

        UpdateOrderStatusRequestDTO request = new UpdateOrderStatusRequestDTO();
        request.setStatus(OrderStatus.CONFIRMED);

        OrderEntity order = new OrderEntity();
        order.setId(1L);
        order.setStatus(OrderStatus.PENDING);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        underTest.updateOrderStatus(token, 1L, request, userAgent);

        verify(orderRepository).findById(1L);
        verify(orderRepository).save(order);
    }

    @Test
    void updateOrderStatus_ShouldUpdateOrderToShipped() {
        String token = "valid-token";
        String userAgent = "testing";

        UpdateOrderStatusRequestDTO request = new UpdateOrderStatusRequestDTO();
        request.setStatus(OrderStatus.SHIPPED);

        OrderEntity order = new OrderEntity();
        order.setId(1L);
        order.setStatus(OrderStatus.CONFIRMED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        underTest.updateOrderStatus(token, 1L, request, userAgent);

        verify(orderRepository).findById(1L);
        verify(orderRepository).save(order);
    }

    @Test
    void updateOrderStatus_ShouldUpdateOrderToDelivered() {
        String token = "valid-token";
        String userAgent = "testing";

        UpdateOrderStatusRequestDTO request = new UpdateOrderStatusRequestDTO();
        request.setStatus(OrderStatus.DELIVERED);

        OrderEntity order = new OrderEntity();
        order.setId(1L);
        order.setStatus(OrderStatus.SHIPPED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        underTest.updateOrderStatus(token, 1L, request, userAgent);

        verify(orderRepository).findById(1L);
        verify(orderRepository).save(order);
    }

    @Test
    void updateOrderStatus_ShouldThrowBadRequestExceptionInvalidStatusTransition() {
        String token = "valid-token";
        String userAgent = "testing";

        UpdateOrderStatusRequestDTO request = new UpdateOrderStatusRequestDTO();
        request.setStatus(OrderStatus.DELIVERED);

        OrderEntity order = new OrderEntity();
        order.setId(1L);
        order.setStatus(OrderStatus.PENDING);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(BadRequestException.class, () -> underTest.updateOrderStatus(token, 1L, request, userAgent));

        verify(orderRepository).findById(1L);
    }

    @Test
    void updateOrderStatus_ShouldThrowBadRequestExceptionInvalidStatusTransitionCaseWithConfirmedInRequest() {
        String token = "valid-token";
        String userAgent = "testing";

        UpdateOrderStatusRequestDTO request = new UpdateOrderStatusRequestDTO();
        request.setStatus(OrderStatus.DELIVERED);

        OrderEntity order = new OrderEntity();
        order.setId(1L);
        order.setStatus(OrderStatus.CONFIRMED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(BadRequestException.class, () -> underTest.updateOrderStatus(token, 1L, request, userAgent));

        verify(orderRepository).findById(1L);
    }
}