package com.shopflow.repository.spec;

import com.shopflow.domain.Category;
import com.shopflow.domain.Product;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public final class ProductSpecification {

    private ProductSpecification() {
    }

    public static Specification<Product> base() {
        return (root, q, cb) -> cb.isTrue(root.get("active"));
    }

    public static Specification<Product> categoryId(Long categoryId) {
        if (categoryId == null) {
            return (root, q, cb) -> cb.conjunction();
        }
        return (root, q, cb) -> {
            Join<Product, Category> cat = root.join("categories", JoinType.INNER);
            return cb.equal(cat.get("id"), categoryId);
        };
    }

    public static Specification<Product> sellerId(Long sellerId) {
        if (sellerId == null) {
            return (root, q, cb) -> cb.conjunction();
        }
        return (root, q, cb) -> cb.equal(root.get("seller").get("id"), sellerId);
    }

    public static Specification<Product> priceMin(BigDecimal min) {
        if (min == null) {
            return (root, q, cb) -> cb.conjunction();
        }
        return (root, q, cb) -> {
            var promo = root.<BigDecimal>get("promoPrice");
            var price = root.<BigDecimal>get("price");
            return cb.or(
                    cb.and(cb.isNotNull(promo), cb.greaterThanOrEqualTo(promo, min)),
                    cb.and(cb.isNull(promo), cb.greaterThanOrEqualTo(price, min))
            );
        };
    }

    public static Specification<Product> priceMax(BigDecimal max) {
        if (max == null) {
            return (root, q, cb) -> cb.conjunction();
        }
        return (root, q, cb) -> {
            var promo = root.<BigDecimal>get("promoPrice");
            var price = root.<BigDecimal>get("price");
            return cb.or(
                    cb.and(cb.isNotNull(promo), cb.lessThanOrEqualTo(promo, max)),
                    cb.and(cb.isNull(promo), cb.lessThanOrEqualTo(price, max))
            );
        };
    }

    public static Specification<Product> onPromoOnly(Boolean promo) {
        if (promo == null || !promo) {
            return (root, q, cb) -> cb.conjunction();
        }
        return (root, q, cb) -> cb.and(
                cb.isNotNull(root.<BigDecimal>get("promoPrice")),
                cb.lessThan(
                        root.<BigDecimal>get("promoPrice"),
                        root.<BigDecimal>get("price"))
        );
    }

    public static Specification<Product> searchQuery(String q) {
        if (q == null || q.isBlank()) {
            return (root, query, cb) -> cb.conjunction();
        }
        String pattern = "%" + q.toLowerCase() + "%";
        return (root, query, cb) -> {
            List<Predicate> preds = new ArrayList<>();
            preds.add(cb.like(cb.lower(root.get("name")), pattern));
            preds.add(cb.like(cb.lower(root.get("description")), pattern));
            Join<Product, Category> cat = root.join("categories", JoinType.LEFT);
            preds.add(cb.like(cb.lower(cat.get("name")), pattern));
            query.distinct(true);
            return cb.or(preds.toArray(new Predicate[0]));
        };
    }
}
