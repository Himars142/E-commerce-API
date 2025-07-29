package com.example.demo3.controller;

import com.example.demo3.dto.ProductBasicDTO;
import com.example.demo3.dto.ProductRequestDTO;
import com.example.demo3.dto.UpdateProductRequestDTO;
import com.example.demo3.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/products")
@Validated
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@RequestHeader(name = "User-Agent", required = false) String userAgent,
                                            @PathVariable @Positive Long id) {
        ProductBasicDTO product = productService.getProductById(id, userAgent);
        return ResponseEntity.ok(product);
    }

    @PostMapping
    public ResponseEntity<?> addProduct(@RequestHeader("Authorization") @NotEmpty String token,
                                        @RequestHeader(name = "User-Agent", required = false) String userAgent,
                                        @Valid @RequestBody ProductRequestDTO product) {
        Long id = productService.addProduct(token, product, userAgent);
        URI location = URI.create("/api/products/" + id);
        return ResponseEntity.created(location).body("Product created! ID: " + id);
    }

    @GetMapping
    public ResponseEntity<?> getAllProducts(@RequestParam(defaultValue = "0") @PositiveOrZero int page,
                                            @RequestHeader(name = "User-Agent", required = false) String userAgent,
                                            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size) {
        return ResponseEntity.ok(productService.getAllProducts(page, size, userAgent));
    }

    @PutMapping
    public ResponseEntity<?> updateProduct(@RequestHeader("Authorization") @NotEmpty String token,
                                           @RequestHeader(name = "User-Agent", required = false) String userAgent,
                                           @Valid @RequestBody UpdateProductRequestDTO productBasicDTO) {
        productService.updateProduct(token, productBasicDTO, userAgent);
        return ResponseEntity.ok().body("Product updated successfully!");
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@RequestHeader("Authorization") @NotEmpty String token,
                                           @RequestHeader(name = "User-Agent", required = false) String userAgent,
                                           @PathVariable @Positive Long id) {
        productService.changeIsActive(token, id, userAgent);
        return ResponseEntity.ok("Product status updated!");
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<?> getProductsByCategory(@RequestHeader(name = "User-Agent", required = false) String userAgent,
                                                   @PathVariable @Positive Long categoryId,
                                                   @RequestParam(defaultValue = "0") @PositiveOrZero int page,
                                                   @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size) {
        return ResponseEntity.ok(productService.getAllProductByCategoryId(categoryId, page, size, userAgent));
    }
}