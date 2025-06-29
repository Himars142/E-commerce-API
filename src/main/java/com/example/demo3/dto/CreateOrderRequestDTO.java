package com.example.demo3.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateOrderRequestDTO {
    @NotBlank(message = "Shipping address can`t be null or blank")
    private String shippingAddress;

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }
}
