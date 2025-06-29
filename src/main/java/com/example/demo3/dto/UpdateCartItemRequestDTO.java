package com.example.demo3.dto;

public class UpdateCartItemRequestDTO {
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
