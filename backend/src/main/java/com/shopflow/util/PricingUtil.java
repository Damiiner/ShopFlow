package com.shopflow.util;

import com.shopflow.domain.Product;
import com.shopflow.domain.ProductVariant;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class PricingUtil {

    private PricingUtil() {
    }

    public static int availableStock(Product product, ProductVariant variant) {
        if (variant != null) {
            return product.getStock() + variant.getAdditionalStock();
        }
        return product.getStock();
    }

    public static BigDecimal unitPrice(Product product, ProductVariant variant) {
        BigDecimal base = product.isOnPromo() ? product.getPromoPrice() : product.getPrice();
        if (base == null) {
            base = BigDecimal.ZERO;
        }
        if (variant != null && variant.getPriceDelta() != null) {
            base = base.add(variant.getPriceDelta());
        }
        return base.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    public static int discountPercent(Product product) {
        if (!product.isOnPromo() || product.getPrice() == null || product.getPrice().signum() == 0) {
            return 0;
        }
        var diff = product.getPrice().subtract(product.getPromoPrice());
        return diff.multiply(BigDecimal.valueOf(100))
                .divide(product.getPrice(), 0, RoundingMode.HALF_UP)
                .intValue();
    }
}
