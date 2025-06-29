package com.example.demo3.dto;

public class CartItemDTO {
    private ProductBasicDTO product;
    private Integer quantity;

    public CartItemDTO() {
    }

    public CartItemDTO(ProductBasicDTO product, Integer quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public ProductBasicDTO getProduct() {
        return product;
    }

    public void setProduct(ProductBasicDTO product) {
        this.product = product;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
