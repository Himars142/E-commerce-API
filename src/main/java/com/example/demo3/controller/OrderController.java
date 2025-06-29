package com.example.demo3.controller;

import com.example.demo3.dto.CreateOrderRequestDTO;
import com.example.demo3.dto.UpdateOrderStatusRequestDTO;
import com.example.demo3.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@Validated
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestHeader("Authorization") String token,
                                         @Valid @RequestBody CreateOrderRequestDTO request) {
        orderService.createOrder(token, request);
        return ResponseEntity.status(HttpStatus.CREATED).body("Order created successfully");
    }

    @GetMapping
    public ResponseEntity<?> getUserOrders(@RequestHeader("Authorization") String token,
                                           @RequestParam(defaultValue = "0") @PositiveOrZero int page,
                                           @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size) {
        return ResponseEntity.ok(orderService.getUserOrders(token, page, size));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderDetails(@RequestHeader("Authorization") String token,
                                             @PathVariable @Positive Long orderId) {
        return ResponseEntity.ok(orderService.getOrderDetails(token, orderId));
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(@RequestHeader("Authorization") String token,
                                         @PathVariable @Positive Long orderId) {
        orderService.cancelOrder(token, orderId);
        return ResponseEntity.ok("Order cancelled");
    }

    @GetMapping("/admin/all")
    public ResponseEntity<?> getAllOrders(@RequestHeader("Authorization") String token,
                                          @RequestParam(defaultValue = "0") @PositiveOrZero int page,
                                          @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size,
                                          @RequestParam(required = false) String status) {
        return ResponseEntity.ok(orderService.getAllOrders(token, page, size, status));
    }

    @PutMapping("/admin/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(@RequestHeader("Authorization") String token,
                                               @PathVariable @Positive Long orderId,
                                               @RequestBody UpdateOrderStatusRequestDTO request) {
        orderService.updateOrderStatus(token, orderId, request);
        return ResponseEntity.ok("Order status updated");
    }
}
