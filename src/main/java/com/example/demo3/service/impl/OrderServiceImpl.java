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
import com.example.demo3.service.AuthService;
import com.example.demo3.service.CartService;
import com.example.demo3.service.OrderService;
import com.example.demo3.service.ProductService;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final AuthService authService;
    private final ProductService productService;
    private final CartService cartService;
    private final OrderMapper orderMapper;

    public OrderServiceImpl(OrderRepository orderRepository,
                            OrderItemRepository orderItemRepository,
                            AuthService authService,
                            ProductService productService,
                            CartService cartService,
                            OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.authService = authService;
        this.productService = productService;
        this.cartService = cartService;
        this.orderMapper = orderMapper;
    }

    @Transactional
    @Override
    public void createOrder(String token, CreateOrderRequestDTO request) {
        UserEntity user = authService.validateTokenAndGetUser(token);
        if (user.getCart() == null || user.getCart().getCartItems().isEmpty()) {
            throw new NotFoundException("User cart is empty or not found.");
        }
        List<CartItemEntity> cartItems = user.getCart().getCartItems();
        productService.validateProductsForOrder(cartItems);
        orderRepository.save(orderMapper.createOrder(user, request, cartItems));
        productService.decreaseStockForOrderItems(cartItems);
        cartService.deleteAllByCartId(user.getCart().getId());
    }

    @Override
    public PageableResponseOrdersDTO getUserOrders(String token, int page, int size) {
        UserEntity user = authService.validateTokenAndGetUser(token);
        Page<OrderEntity> orderEntityPage = orderRepository.findByUserId(user.getId(), PageRequest.of(page, size));
        return orderMapper.createPageableResponseOrdersDTO(orderEntityPage);
    }

    @Override
    public OrderEntityDTO getOrderDetails(String token, Long id) {
        OrderEntity order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order with id:" + id + " not found"));
        UserEntity user = authService.validateTokenAndGetUser(token);
        if (user.getRole().equals(UserRole.ROLE_CUSTOMER) && !order.getUser().equals(user)) {
            throw new ForbiddenException("You don`t have access to this order");
        }
        return orderMapper.toOrderEntityDTO(order);
    }

    @Transactional
    @Override
    public void cancelOrder(String token, Long orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found! ID: " + orderId));
        UserEntity user = authService.validateTokenAndGetUser(token);
        if (user.getRole().equals(UserRole.ROLE_CUSTOMER) && !order.getUser().equals(user)) {
            throw new ForbiddenException("You don`t have access to this order");
        }
        if (!order.getStatus().equals(OrderStatus.PENDING)) {
            throw new BadRequestException("Order is not pending! Order status:" + order.getStatus());
        }
        List<OrderItemEntity> orderItems = orderItemRepository.findByOrder_Id(orderId)
                .orElseThrow(() -> new NotFoundException("No items in order id: " + orderId));
        productService.increaseStockForOrderItems(orderItems);
        orderRepository.save(orderMapper.cancelOrder(order));
    }

    @Override
    public PageableResponseOrdersDTO getAllOrders(String token, int page, int size, String status) {
        authService.checkIsUserAdmin(token);
        Page<OrderEntity> orderEntityPage;
        if (status == null) {
            orderEntityPage = orderRepository.findAll(PageRequest.of(page, size));
        } else {
            orderEntityPage = orderRepository.findByStatus(OrderStatus.valueOf(status), PageRequest.of(page, size));
        }
        return orderMapper.createPageableResponseOrdersDTO(orderEntityPage);
    }

    @Transactional
    @Override
    public void updateOrderStatus(String token, Long orderId, UpdateOrderStatusRequestDTO request) {
        authService.checkIsUserAdmin(token);
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found. ID:" + orderId));
        if (order.getStatus().equals(request.getStatus())) {
            throw new BadRequestException("Order already:" + request.getStatus());
        }
        if (!isValidStatusTransition(order.getStatus(), request.getStatus())) {
            throw new BadRequestException(
                    String.format("Invalid status transition from %s to %s", order.getStatus(), request.getStatus())
            );
        }
        if (request.getStatus().equals(OrderStatus.CANCELLED)) {
            cancelOrder(token, orderId);
            return;
        }
        order.setStatus(request.getStatus());
        orderRepository.save(order);
    }

    private boolean isValidStatusTransition(OrderStatus currentStatus, OrderStatus requestStatus) {
        if (currentStatus.equals(OrderStatus.PENDING) &&
                requestStatus.equals(OrderStatus.CONFIRMED)) {
            return true;
        }
        if (currentStatus.equals(OrderStatus.CONFIRMED) &&
                requestStatus.equals(OrderStatus.SHIPPED)) {
            return true;
        }
        return currentStatus.equals(OrderStatus.SHIPPED) &&
                requestStatus.equals(OrderStatus.DELIVERED);
    }
}
