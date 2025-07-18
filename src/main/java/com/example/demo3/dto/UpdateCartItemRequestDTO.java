package com.example.demo3.dto;

import jakarta.validation.constraints.Positive;

public class UpdateCartItemRequestDTO {
    @Positive
    private Integer quantity;

    public UpdateCartItemRequestDTO(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
