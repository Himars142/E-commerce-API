package com.example.demo3.repository;

import com.example.demo3.entity.CategoryEntity;
import com.example.demo3.entity.ProductEntity;
import com.example.demo3.testutil.BaseRepositoryTest;
import com.example.demo3.testutil.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductsRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private ProductsRepository underTest;

    @Test
    void findBySku_ShouldFindProductBySku() {
        String sku = "TEST-SKU";
        ProductEntity product = TestDataFactory.createAndPersistProduct(entityManager, sku);

        Optional<ProductEntity> result = underTest.findBySku(sku);

        assertTrue(result.isPresent());
        assertEquals(product.getId(), result.get().getId());
    }

    @Test
    void findBySku_ShouldNotFindProductBySku() {
        Optional<ProductEntity> result = underTest.findBySku("TEST-SKU");

        assertTrue(result.isEmpty());
    }


    @Test
    void findByCategoryId_ShouldFindProductsByCategoryId() {
        CategoryEntity category = TestDataFactory.createAndPersistCategory(entityManager);
        ProductEntity product = TestDataFactory.createAndPersistProductWithCategory(entityManager, category);

        Page<ProductEntity> result = underTest.findByCategoryId(category.getId(), PageRequest.of(0, 10));

        assertTrue(result.getContent().contains(product));
        assertEquals(0, result.getNumber());
        assertEquals(10, result.getSize());
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void findByCategoryId_ShouldNotFindProductsByCategoryId() {
        CategoryEntity category = TestDataFactory.createAndPersistCategory(entityManager);

        Page<ProductEntity> result = underTest.findByCategoryId(category.getId(), PageRequest.of(0, 10));

        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getNumber());
        assertEquals(10, result.getSize());
        assertEquals(0, result.getTotalElements());
    }
}