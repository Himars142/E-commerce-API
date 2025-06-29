package com.example.demo3.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CartDTO {
    private List<CartItemDTO> cartItems;

    public CartDTO() {
    }

    public CartDTO(List<CartItemDTO> cartItems) {
        this.cartItems = cartItems;
    }

    public List<CartItemDTO> getCartItems() {
        return cartItems;
    }

    public void setCartItems(List<CartItemDTO> cartItems) {
        this.cartItems = cartItems;
    }
}
