package com.example.demo3.repository;

import com.example.demo3.entity.OrderEntity;
import com.example.demo3.entity.OrderItemEntity;
import com.example.demo3.entity.ProductEntity;
import com.example.demo3.entity.UserEntity;
import com.example.demo3.testutil.BaseRepositoryTest;
import com.example.demo3.testutil.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderItemRepositoryTest extends BaseRepositoryTest<OrderItemRepository> {

    @Nested
    @DisplayName("Find by order id tests")
    class FindByOrder_Id {

        @Test
        @DisplayName("Should find order items by order id")
        void findByOrder_Id_ShouldFindOrderItemsByOrderId() {
            UserEntity user = TestDataFactory.createAndPersistUser(entityManager);
            OrderEntity order = TestDataFactory.createAndPersistOrder(entityManager, user);
            ProductEntity product = TestDataFactory.createAndPersistProduct(entityManager);
            OrderItemEntity orderItemEntity1 = TestDataFactory.createAndPersistOrderItem(entityManager, order, product);
            OrderItemEntity orderItemEntity2 = TestDataFactory.createAndPersistOrderItem(entityManager, order, product);
            OrderItemEntity orderItemEntity3 = TestDataFactory.createAndPersistOrderItem(entityManager, order, product);

            underTest.findByOrderId(order.getId());

            List<OrderItemEntity> result = underTest.findByOrderId(order.getId());

            assertFalse(result.isEmpty());
            assertTrue(result.contains(orderItemEntity1));
            assertTrue(result.contains(orderItemEntity2));
            assertTrue(result.contains(orderItemEntity3));
        }

        @Test
        @DisplayName("Should not find order items by order id")
        void findByOrder_Id_ShouldNotFindOrderItemsByOrderId() {
            UserEntity user = TestDataFactory.createAndPersistUser(entityManager);
            OrderEntity order = TestDataFactory.createAndPersistOrder(entityManager, user);

            List<OrderItemEntity> result = underTest.findByOrderId(order.getId());

            assertTrue(result.isEmpty());
        }
    }
}