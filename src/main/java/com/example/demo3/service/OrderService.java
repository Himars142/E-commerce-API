package com.example.demo3.service;

import com.example.demo3.dto.CreateOrderRequestDTO;
import com.example.demo3.dto.OrderEntityDTO;
import com.example.demo3.dto.PageableResponseOrdersDTO;
import com.example.demo3.dto.UpdateOrderStatusRequestDTO;

public interface OrderService {
    Long createOrder(CreateOrderRequestDTO request, String userAgent);

    PageableResponseOrdersDTO getUserOrders(int page, int size, String userAgent);

    OrderEntityDTO getOrderDetails(Long id, String userAgent);

    void cancelOrder(Long orderId, String userAgent);

    PageableResponseOrdersDTO getAllOrders(int page, int size, String status, String userAgent);

    void updateOrderStatus(Long orderId, UpdateOrderStatusRequestDTO request, String userAgent);
}
