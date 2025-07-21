package com.example.demo3.controller;

import com.example.demo3.dto.CategoryCreateRequestDTO;
import com.example.demo3.service.CategoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/categories")
@Validated
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<?> getAllCategories(@RequestParam(defaultValue = "0") @PositiveOrZero int page,
                                              @RequestHeader(name = "User-Agent", required = false) String userAgent,
                                              @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size) {
        return ResponseEntity.ok(categoryService.getAllCategories(page, size, userAgent));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCategoryById(@RequestHeader(name = "User-Agent", required = false) String userAgent,
                                             @PathVariable(name = "id") @Positive Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id, userAgent));
    }

    @PostMapping
    public ResponseEntity<?> createCategory(@RequestHeader("Authorization") @NotEmpty String token,
                                            @RequestHeader(name = "User-Agent", required = false) String userAgent,
                                            @Valid @RequestBody CategoryCreateRequestDTO request) {
        Long id = categoryService.createCategory(token, request, userAgent);
        URI location = URI.create("/api/categories/" + id);
        return ResponseEntity.created(location).body("Category created! ID: " + id);
    }
}
