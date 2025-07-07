package com.example.demo3.service;

import com.example.demo3.dto.CreateOrderRequestDTO;
import com.example.demo3.dto.OrderEntityDTO;
import com.example.demo3.dto.PageableResponseOrdersDTO;
import com.example.demo3.dto.UpdateOrderStatusRequestDTO;

public interface OrderService {
    void createOrder(String token, CreateOrderRequestDTO request, String userAgent);

    PageableResponseOrdersDTO getUserOrders(String token, int page, int size, String userAgent);

    OrderEntityDTO getOrderDetails(String token, Long id, String userAgent);

    void cancelOrder(String token, Long orderId, String userAgent);

    PageableResponseOrdersDTO getAllOrders(String token, int page, int size, String status, String userAgent);

    void updateOrderStatus(String token, Long orderId, UpdateOrderStatusRequestDTO request, String userAgent);
}
