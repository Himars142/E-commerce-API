package com.example.demo3.service;

import com.example.demo3.dto.CreateOrderRequestDTO;
import com.example.demo3.dto.OrderEntityDTO;
import com.example.demo3.dto.PageableResponseOrdersDTO;
import com.example.demo3.dto.UpdateOrderStatusRequestDTO;

public interface OrderService {
    void createOrder(String token, CreateOrderRequestDTO request);

    PageableResponseOrdersDTO getUserOrders(String token, int page, int size);

    OrderEntityDTO getOrderDetails(String token, Long id);

    void cancelOrder(String token, Long orderId);

    PageableResponseOrdersDTO getAllOrders(String token, int page, int size, String status);

    void updateOrderStatus(String token, Long orderId, UpdateOrderStatusRequestDTO request);
}
