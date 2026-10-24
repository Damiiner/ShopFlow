package com.shopflow.repository;

import com.shopflow.domain.OrderItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("""
            SELECT oi.product.id, SUM(oi.quantity)
            FROM OrderItem oi JOIN oi.shopOrder o
            WHERE o.status <> com.shopflow.domain.OrderStatus.CANCELLED
            GROUP BY oi.product.id
            ORDER BY SUM(oi.quantity) DESC
            """)
    List<Object[]> topProducts(Pageable pageable);

    @Query("""
            SELECT oi.product.seller.id, SUM(oi.quantity * oi.unitPrice)
            FROM OrderItem oi JOIN oi.shopOrder o
            WHERE o.status <> com.shopflow.domain.OrderStatus.CANCELLED
            GROUP BY oi.product.seller.id
            ORDER BY SUM(oi.quantity * oi.unitPrice) DESC
            """)
    List<Object[]> topSellersByRevenue(Pageable pageable);
}
