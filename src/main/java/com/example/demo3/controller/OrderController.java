package com.example.demo3.controller;

import com.example.demo3.dto.CreateOrderRequestDTO;
import com.example.demo3.dto.UpdateOrderStatusRequestDTO;
import com.example.demo3.entity.OrderStatus;
import com.example.demo3.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
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
    public ResponseEntity<?> createOrder(@RequestHeader(name = "User-Agent", required = false) String userAgent,
                                         @Valid @RequestBody CreateOrderRequestDTO request) {
        Long id = orderService.createOrder(request, userAgent);
        return ResponseEntity.created(java.net.URI.create("/api/orders/" + id)).body("Order created! ID: " + id);
    }

    @GetMapping
    public ResponseEntity<?> getUserOrders(@RequestHeader(name = "User-Agent", required = false) String userAgent,
                                           @RequestParam(defaultValue = "0") @PositiveOrZero int page,
                                           @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size) {
        return ResponseEntity.ok(orderService.getUserOrders(page, size, userAgent));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderDetails(@RequestHeader(name = "User-Agent", required = false) String userAgent,
                                             @PathVariable @Positive Long orderId) {
        return ResponseEntity.ok(orderService.getOrderDetails(orderId, userAgent));
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(@RequestHeader(name = "User-Agent", required = false) String userAgent,
                                         @PathVariable @Positive Long orderId) {
        orderService.cancelOrder(orderId, userAgent);
        return ResponseEntity.ok("Order cancelled");
    }

    @GetMapping("/admin/all")
    public ResponseEntity<?> getAllOrders(@RequestHeader(name = "User-Agent", required = false) String userAgent,
                                          @RequestParam(defaultValue = "0") @PositiveOrZero int page,
                                          @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size,
                                          @RequestParam(required = false) String status) {
        return ResponseEntity.ok(orderService.getAllOrders(page, size, status, userAgent));
    }

    @PutMapping("/admin/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(@RequestHeader(name = "User-Agent", required = false) String userAgent,
                                               @PathVariable @Positive Long orderId,
                                               @RequestBody UpdateOrderStatusRequestDTO request) {
        if (request.getStatus().equals(OrderStatus.CANCELLED)) {
            orderService.cancelOrder(orderId, userAgent);
        } else {
            orderService.updateOrderStatus(orderId, request, userAgent);
        }
        return ResponseEntity.ok("Order status updated");
    }
}
