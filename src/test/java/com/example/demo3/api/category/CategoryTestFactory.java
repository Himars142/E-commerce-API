package com.example.demo3.api.category;

import com.example.demo3.dto.CategoryCreateRequestDTO;

import java.util.concurrent.atomic.AtomicLong;

public class CategoryTestFactory {

    private static final AtomicLong CATEGORY_COUNTER = new AtomicLong(1);

    public static CategoryCreateRequestDTO createCategoryCreateRequestDTO() {
        var request = new CategoryCreateRequestDTO();
        request.setName("TEST-CATEGORY-" + CATEGORY_COUNTER.getAndIncrement());
        return request;
    }

    public static CategoryCreateRequestDTO createCategoryCreateRequestDTO(String name) {
        var request = new CategoryCreateRequestDTO();
        request.setName(name);
        return request;
    }
}
