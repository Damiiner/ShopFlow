package com.shopflow.repository;

import com.shopflow.domain.Product;
import com.shopflow.domain.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    List<ProductVariant> findByProduct(Product product);
}
