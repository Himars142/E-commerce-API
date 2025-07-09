package com.example.demo3.repository;

import com.example.demo3.entity.OrderEntity;
import com.example.demo3.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    Page<OrderEntity> findByUserId(Long userId, Pageable pageable);

    Page<OrderEntity> findByStatus(OrderStatus status, Pageable pageable);
}
