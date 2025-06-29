package com.example.demo3.repository;

import com.example.demo3.entity.OrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItemEntity, Long> {
    Optional<List<OrderItemEntity>> findByOrder_Id(Long id);
}
