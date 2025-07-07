package com.example.demo3.controller;

import com.example.demo3.dto.UpdateCartItemRequestDTO;
import com.example.demo3.service.CartService;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import java.util.UUID;
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
    public ResponseEntity<?> getCart(@RequestHeader("Authorization") String token,
                                     @RequestHeader(name = "User-Agent", required = false) String userAgent) {
        return ResponseEntity.ok(cartService.getCart(token, userAgent));
    }

    @PostMapping("/items/{productId}")
    public ResponseEntity<?> addItemToCart(@RequestHeader("Authorization") String token,
                                           @RequestHeader(name = "User-Agent", required = false) String userAgent,
                                           @PathVariable @Positive Long productId) {
        cartService.addItemToCart(token, productId, userAgent);
        return ResponseEntity.ok("Item added to cart");

    }

    @PutMapping("/items/{productId}")
    public ResponseEntity<?> updateCartItem(@RequestHeader("Authorization") String token,
                                            @RequestHeader(name = "User-Agent", required = false) String userAgent,
                                            @PathVariable @Positive Long productId,
                                            @RequestBody UpdateCartItemRequestDTO request) {
        cartService.updateCartItem(token, productId, request, userAgent);
        return ResponseEntity.ok("Cart item updated");

    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<?> removeItemFromCart(@RequestHeader("Authorization") String token,
                                                @RequestHeader(name = "User-Agent", required = false) String userAgent,
                                                @PathVariable @Positive Long productId) {
        cartService.removeItemFromCart(token, productId, userAgent);
        return ResponseEntity.ok("Item removed from cart");

    }

    @DeleteMapping
    public ResponseEntity<?> clearCart(@RequestHeader("Authorization") String token,
                                       @RequestHeader(name = "User-Agent", required = false) String userAgent) {
        int deletedItems = cartService.clearCart(token, userAgent);
        return ResponseEntity.ok("Cart cleared. Deleted items: " + deletedItems);
    }
}