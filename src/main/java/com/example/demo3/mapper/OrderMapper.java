package com.example.demo3.mapper;

import com.example.demo3.dto.CreateOrderRequestDTO;
import com.example.demo3.dto.OrderEntityDTO;
import com.example.demo3.dto.OrderItemEntityDTO;
import com.example.demo3.dto.PageableResponseOrdersDTO;
import com.example.demo3.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Component
public class OrderMapper {
    private final OrderItemMapper orderItemMapper;
    private final UserMapper userMapper;
    private final ProductMapper productMapper;

    public OrderMapper(OrderItemMapper orderItemMapper,
                       UserMapper userMapper,
                       ProductMapper productMapper) {
        this.orderItemMapper = orderItemMapper;
        this.userMapper = userMapper;
        this.productMapper = productMapper;
    }

    public OrderEntityDTO toOrderEntityDTO(OrderEntity order) {
        OrderEntityDTO orderEntityDTO = new OrderEntityDTO();
        orderEntityDTO.setId(order.getId());
        orderEntityDTO.setOrderNumber(order.getOrderNumber());
        orderEntityDTO.setStatus(order.getStatus());
        orderEntityDTO.setTotalAmount(order.getTotalAmount());
        orderEntityDTO.setShippingAddress(order.getShippingAddress());
        orderEntityDTO.setCreatedAt(order.getCreatedAt());
        orderEntityDTO.setUpdatedAt(order.getUpdatedAt());
        orderEntityDTO.setUser(userMapper.toDTO(order.getUser()));
        orderEntityDTO.setOrderItems(order.getOrderItems()
                .stream()
                .map(orderItemEntity -> {
                    OrderItemEntityDTO dto = new OrderItemEntityDTO();
                    dto.setId(orderItemEntity.getId());
                    dto.setQuantity(orderItemEntity.getQuantity());
                    dto.setUnitPrice(orderItemEntity.getUnitPrice());
                    dto.setTotalPrice(orderItemEntity.getTotalPrice());
                    dto.setProduct(productMapper.toDTO(orderItemEntity.getProduct()));
                    return dto;
                })
                .toList());
        return orderEntityDTO;
    }

    public PageableResponseOrdersDTO createPageableResponseOrdersDTO(Page<OrderEntity> orderEntityPage) {
        PageableResponseOrdersDTO request = new PageableResponseOrdersDTO();
        request.setContent(orderEntityPage.getContent().stream()
                .map(this::toOrderEntityDTO).toList());
        request.setPageNumber(orderEntityPage.getPageable().getPageNumber());
        request.setPageSize(orderEntityPage.getPageable().getPageSize());
        request.setTotalElements(orderEntityPage.getTotalElements());
        request.setTotalPages(orderEntityPage.getTotalPages());
        request.setFirst(orderEntityPage.getPageable().getPageNumber() == 0);
        request.setLast(orderEntityPage.getTotalPages() == orderEntityPage.getPageable().getPageNumber());
        return request;
    }

    public OrderEntity cancelOrder(OrderEntity order) {
        order.setStatus(OrderStatus.CANCELLED);
        order.setTotalAmount(BigDecimal.ZERO);
        return order;
    }

    public OrderEntity createOrder(UserEntity user,
                                   CreateOrderRequestDTO request,
                                   List<CartItemEntity> cartItems) {
        OrderEntity order = new OrderEntity();
        List<OrderItemEntity> orderItems = cartItems
                .stream()
                .map(cartItem -> orderItemMapper.toOrderItemEntity(cartItem, order))
                .toList();
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (OrderItemEntity orderItem : orderItems) {
            totalPrice = totalPrice.add(orderItem.getTotalPrice());
        }
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setShippingAddress(request.getShippingAddress());
        order.setTotalAmount(totalPrice);
        order.setOrderItems(orderItems);
        order.setOrderNumber(UUID.randomUUID().toString());
        return order;
    }
}

