package com.example.demo3.dto;

import com.example.demo3.entity.OrderStatus;

public class UpdateOrderStatusRequestDTO {
    private OrderStatus status;

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "UpdateOrderStatusRequestDTO{" +
                "status=" + status +
                '}';
    }
}
