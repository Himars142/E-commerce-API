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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.example.demo3.utill.GenerateRequestID.generateRequestID;

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

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Transactional
    @Override
    public Long createOrder(String token, CreateOrderRequestDTO request, String userAgent) {
        String requestId = generateRequestID();
        logger.info("Attempt to create order. Request id: {}, user agent: {}", requestId, userAgent);
        UserEntity user = authService.validateTokenAndGetUser(token, requestId);
        if (user.getCart() == null || user.getCart().getCartItems().isEmpty()) {
            throw new NotFoundException("User cart is empty or not found. Request id:" + requestId);
        }
        List<CartItemEntity> cartItems = user.getCart().getCartItems();
        productService.validateProductsForOrder(cartItems, requestId);
        OrderEntity order = orderRepository.save(orderMapper.createOrder(user, request, cartItems));
        productService.decreaseStockForOrderItems(cartItems);
        cartService.deleteAllByCartId(user.getCart().getId());
        logger.info("Order created for user id {}, order id {}, request id: {}", user.getId(), order.getId(), requestId);
        return order.getId();
    }

    @Override
    public PageableResponseOrdersDTO getUserOrders(String token, int page, int size, String userAgent) {
        String requestId = generateRequestID();
        logger.info("Attempt to get users orders. Request id: {}, user agent: {}, page: {}, size: {}.",
                requestId, userAgent, page, size);
        UserEntity user = authService.validateTokenAndGetUser(token, requestId);
        Page<OrderEntity> orderEntityPage = orderRepository.findByUserId(user.getId(), PageRequest.of(page, size));
        PageableResponseOrdersDTO response = orderMapper.createPageableResponseOrdersDTO(orderEntityPage);
        logger.info("User with id {} orders retrieved. Total elements: {}, total pages: {}",
                user.getId(), response.getTotalElements(), response.getTotalPages());
        return response;
    }

    @Override
    public OrderEntityDTO getOrderDetails(String token, Long id, String userAgent) {
        String requestId = generateRequestID();
        logger.info("Attempt to get order details. Request id: {}, user agent: {}, order id: {}.",
                requestId, userAgent, id);
        OrderEntity order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order with id:" + id + " not found. Request id: " + requestId));
        UserEntity user = authService.validateTokenAndGetUser(token, requestId);
        checkOrderAccess(user, order, requestId);
        OrderEntityDTO response = orderMapper.toOrderEntityDTO(order);
        logger.info("Order details retrieved for orderId {}, request id: {}", id, requestId);
        return response;
    }

    @Transactional
    @Override
    public void cancelOrder(String token, Long orderId, String userAgent) {
        String requestId = generateRequestID();
        logger.info("Attempt to cancel order. Request id: {}, user agent: {}, order id: {}.",
                requestId, userAgent, orderId);
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found! ID: " + orderId + ". Request id: " + requestId));
        UserEntity user = authService.validateTokenAndGetUser(token, requestId);
        checkOrderAccess(user, order, requestId);
        if (!order.getStatus().equals(OrderStatus.PENDING)) {
            throw new BadRequestException("Order is not pending! Order status:" + order.getStatus() + ". Request id: " + requestId);
        }
        List<OrderItemEntity> orderItems = orderItemRepository.findByOrderId(orderId);
        if (orderItems.isEmpty()) {
            throw new NotFoundException("No items in order id: " + orderId + ". Request id: " + requestId);
        }
        productService.increaseStockForOrderItems(orderItems);
        orderRepository.save(orderMapper.cancelOrder(order));
        logger.info("Success. OrderId {} canceled, request id: {}", orderId, requestId);
    }

    @Override
    public PageableResponseOrdersDTO getAllOrders(String token, int page, int size, String status, String userAgent) {
        String requestId = generateRequestID();
        logger.info("Attempt to get all orders. Request id: {}, user agent: {}, page: {}, size: {}.",
                requestId, userAgent, page, size);
        authService.checkIsUserAdmin(token, requestId);
        Page<OrderEntity> orderEntityPage;
        String message;
        if (status == null) {
            orderEntityPage = orderRepository.findAll(PageRequest.of(page, size));
            message = "All orders retrieved without status filter. Request id: " + requestId;
        } else {
            orderEntityPage = orderRepository.findByStatus(OrderStatus.valueOf(status), PageRequest.of(page, size));
            message = "Orders retrieved with status: " + status + ". Request id: " + requestId;
        }
        PageableResponseOrdersDTO response = orderMapper.createPageableResponseOrdersDTO(orderEntityPage);
        logger.info(message);
        return response;
    }

    @Transactional
    @Override
    public void updateOrderStatus(String token, Long orderId, UpdateOrderStatusRequestDTO request, String userAgent) {
        String requestId = generateRequestID();
        logger.info("Attempt to update order status. Request id: {}, user agent; {},  status request: {}.",
                requestId, userAgent, request);
        authService.checkIsUserAdmin(token, requestId);
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found. ID:" + orderId + ". Request id: " + requestId));
        if (order.getStatus().equals(request.getStatus())) {
            throw new BadRequestException("Order already:" + request.getStatus() + ". Request id: " + requestId);
        }
        if (!isValidStatusTransition(order.getStatus(), request.getStatus())) {
            throw new BadRequestException(
                    String.format("Invalid status transition from %s to %s. Request id: %s",
                            order.getStatus(), request.getStatus(), requestId)
            );
        }
        order.setStatus(request.getStatus());
        orderRepository.save(order);
        logger.info("Success orderId {} status change to {}, request id: {}", orderId, request.getStatus(), requestId);
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

    private void checkOrderAccess(UserEntity user, OrderEntity order, String requestId) {
        if (user.getRole().equals(UserRole.ROLE_CUSTOMER) && !order.getUser().equals(user)) {
            throw new ForbiddenException("You don`t have access to this order. Request id: " + requestId);
        }
    }
}
