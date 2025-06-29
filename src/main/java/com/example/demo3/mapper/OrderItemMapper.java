package com.example.demo3.mapper;

import com.example.demo3.entity.CartItemEntity;
import com.example.demo3.entity.OrderEntity;
import com.example.demo3.entity.OrderItemEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class OrderItemMapper {
    public OrderItemEntity toOrderItemEntity(CartItemEntity entity, OrderEntity order) {
        OrderItemEntity orderItem = new OrderItemEntity();
        orderItem.setOrder(order);
        orderItem.setProduct(entity.getProduct());
        orderItem.setQuantity(entity.getQuantity());
        orderItem.setUnitPrice(entity.getProduct().getPrice());
        orderItem.setTotalPrice(orderItem.getUnitPrice().multiply(new BigDecimal(orderItem.getQuantity())));
        return orderItem;
    }
}
