package com.shopflow.repository;

import com.shopflow.domain.Product;
import com.shopflow.domain.Review;
import com.shopflow.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @org.springframework.data.jpa.repository.Query("""
            SELECT CASE WHEN COUNT(oi) > 0 THEN true ELSE false END
            FROM OrderItem oi JOIN oi.shopOrder o
            WHERE o.customer.id = :customerId AND oi.product.id = :productId
            AND o.status IN (com.shopflow.domain.OrderStatus.PAID, com.shopflow.domain.OrderStatus.PROCESSING, com.shopflow.domain.OrderStatus.SHIPPED, com.shopflow.domain.OrderStatus.DELIVERED)
            """)
    boolean customerHasPurchasedProduct(@org.springframework.data.repository.query.Param("customerId") Long customerId,
                                        @org.springframework.data.repository.query.Param("productId") Long productId);
    Page<Review> findByProductAndApprovedTrueOrderByCreatedAtDesc(Product product, Pageable pageable);

    List<Review> findByProductAndApprovedTrue(Product product);

    boolean existsByCustomerAndProduct(User customer, Product product);

    Page<Review> findByCustomerOrderByCreatedAtDesc(User customer, Pageable pageable);
}
