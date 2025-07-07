package com.example.demo3.controller;

import com.example.demo3.dto.CategoryCreateRequestDTO;
import com.example.demo3.service.CategoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping
    public ResponseEntity<?> createCategory(@RequestHeader("Authorization") String token,
                                            @RequestHeader(name = "User-Agent", required = false) String userAgent,
                                            @Valid @RequestBody CategoryCreateRequestDTO request) {
        categoryService.createCategory(token, request, userAgent);
        return ResponseEntity.status(HttpStatus.CREATED).body("Category created");
    }
}
