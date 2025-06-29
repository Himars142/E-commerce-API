package com.example.demo3.controller;

import com.example.demo3.dto.ProductBasicDTO;
import com.example.demo3.dto.ProductRequestDTO;
import com.example.demo3.dto.UpdateProductRequestDTO;
import com.example.demo3.service.ProductService;
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
@RequestMapping("/api/products")
@Validated
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable @Positive Long id) {
        ProductBasicDTO product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @PostMapping
    public ResponseEntity<?> addProduct(@RequestHeader("Authorization") String token,
                                        @Valid @RequestBody ProductRequestDTO product) {
        productService.addProduct(token, product);
        return ResponseEntity.status(HttpStatus.CREATED).body("Product added successfully!");
    }

    @GetMapping
    public ResponseEntity<?> getAllProducts(@RequestParam(defaultValue = "0") @PositiveOrZero int page,
                                            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size) {
        return ResponseEntity.ok(productService.getAllProducts(page, size));
    }

    @PutMapping
    public ResponseEntity<?> updateProduct(@RequestHeader("Authorization") String token,
                                           @Valid @RequestBody UpdateProductRequestDTO productBasicDTO) {
        productService.updateProduct(token, productBasicDTO);
        return ResponseEntity.ok().body("Product updated successfully!");
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@RequestHeader("Authorization") String token,
                                           @PathVariable @Positive Long id) {
        productService.changeIsActive(token, id);
        return ResponseEntity.ok("Product status updated!");
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<?> getProductsByCategory(@PathVariable @Positive Long categoryId,
                                                   @RequestParam(defaultValue = "0") @PositiveOrZero int page,
                                                   @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size) {
        return ResponseEntity.ok(productService.getAllProductByCategoryId(categoryId, page, size));
    }
}