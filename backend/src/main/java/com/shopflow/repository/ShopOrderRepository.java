package com.shopflow.repository;

import com.shopflow.domain.ShopOrder;
import com.shopflow.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ShopOrderRepository extends JpaRepository<ShopOrder, Long> {

    Optional<ShopOrder> findByOrderNumber(String orderNumber);

    Page<ShopOrder> findByCustomerOrderByOrderedAtDesc(User customer, Pageable pageable);

    List<ShopOrder> findByCustomerOrderByOrderedAtDesc(User customer);

    Page<ShopOrder> findAllByOrderByOrderedAtDesc(Pageable pageable);

    @Query("SELECT DISTINCT o FROM ShopOrder o JOIN o.items i WHERE i.product.seller.id = :sellerId ORDER BY o.orderedAt DESC")
    Page<ShopOrder> findBySellerProduct(@Param("sellerId") Long sellerId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(o.totalTtc), 0) FROM ShopOrder o WHERE o.status <> com.shopflow.domain.OrderStatus.CANCELLED")
    BigDecimal sumRevenueGlobal();

    @Query("SELECT COALESCE(SUM(o.totalTtc), 0) FROM ShopOrder o JOIN o.items i WHERE i.product.seller.id = :sellerId AND o.status <> com.shopflow.domain.OrderStatus.CANCELLED")
    BigDecimal sumRevenueBySeller(@Param("sellerId") Long sellerId);
}
