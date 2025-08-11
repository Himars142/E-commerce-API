package com.example.demo3.controller;

import com.example.demo3.dto.UpdateCartItemRequestDTO;
import com.example.demo3.service.CartService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@Validated
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<?> getCart(@RequestHeader(name = "User-Agent", required = false) String userAgent) {
        return ResponseEntity.ok(cartService.getCart(userAgent));
    }

    @PostMapping("/items/{productId}")
    public ResponseEntity<?> addItemToCart(@RequestHeader(name = "User-Agent", required = false) String userAgent,
                                           @PathVariable @Positive Long productId) {
        cartService.addItemToCart(productId, userAgent);
        return ResponseEntity.ok("Item added to cart");

    }

    @PutMapping("/items/{productId}")
    public ResponseEntity<?> updateCartItem(@RequestHeader(name = "User-Agent", required = false) String userAgent,
                                            @PathVariable @Positive Long productId,
                                            @Valid @RequestBody UpdateCartItemRequestDTO request) {
        cartService.updateCartItem(productId, request, userAgent);
        return ResponseEntity.ok("Cart item updated");

    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<?> removeItemFromCart(@RequestHeader(name = "User-Agent", required = false) String userAgent,
                                                @PathVariable @Positive Long productId) {
        cartService.removeItemFromCart(productId, userAgent);
        return ResponseEntity.ok("Item removed from cart");

    }

    @DeleteMapping
    public ResponseEntity<?> clearCart(@RequestHeader(name = "User-Agent", required = false) String userAgent) {
        int deletedItems = cartService.clearCart(userAgent);
        return ResponseEntity.ok("Cart cleared. Deleted items: " + deletedItems);
    }
}