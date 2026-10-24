package com.shopflow.service;

import com.shopflow.domain.*;
import com.shopflow.dto.cart.*;
import com.shopflow.exception.BusinessException;
import com.shopflow.repository.*;
import com.shopflow.util.PricingUtil;
import com.shopflow.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class CartService {

    private static final BigDecimal SHIPPING_FLAT = new BigDecimal("5.99");
    private static final BigDecimal FREE_SHIPPING_THRESHOLD = new BigDecimal("50.00");

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final CouponRepository couponRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public CartResponse getMyCart() {
        var principal = SecurityUtils.requireUser();
        if (principal.getRole() != Role.CUSTOMER) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Customers only");
        }
        var user = userRepository.findById(principal.getId()).orElseThrow();
        var cart = cartRepository.findByCustomer(user).orElse(null);
        if (cart == null) {
            return CartResponse.builder()
                    .cartId(null)
                    .lines(java.util.List.of())
                    .subtotal(BigDecimal.ZERO)
                    .discount(BigDecimal.ZERO)
                    .shippingFee(BigDecimal.ZERO)
                    .totalTtc(BigDecimal.ZERO)
                    .couponCode(null)
                    .build();
        }
        return buildResponse(cart);
    }

    @Transactional
    public CartResponse addItem(AddCartItemRequest req) {
        var principal = SecurityUtils.requireUser();
        if (principal.getRole() != Role.CUSTOMER) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Customers only");
        }
        var user = userRepository.findById(principal.getId()).orElseThrow();
        var cart = cartRepository.findByCustomer(user).orElseGet(() -> {
            var c = Cart.builder().customer(user).build();
            return cartRepository.save(c);
        });

        var product = productRepository.findById(req.getProductId())
                .filter(Product::isActive)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Product not found"));
        ProductVariant variant = null;
        if (req.getVariantId() != null) {
            variant = variantRepository.findById(req.getVariantId())
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Variant not found"));
            if (!variant.getProduct().getId().equals(product.getId())) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "Variant does not match product");
            }
        }

        int max = PricingUtil.availableStock(product, variant);
        if (req.getQuantity() > max) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Not enough stock (available: " + max + ")");
        }

        CartItem existing = null;
        for (var line : cart.getItems()) {
            boolean sameVariant = (variant == null && line.getVariant() == null)
                    || (variant != null && line.getVariant() != null && line.getVariant().getId().equals(variant.getId()));
            if (line.getProduct().getId().equals(product.getId()) && sameVariant) {
                existing = line;
                break;
            }
        }
        if (existing != null) {
            int nq = existing.getQuantity() + req.getQuantity();
            if (nq > max) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "Not enough stock (available: " + max + ")");
            }
            existing.setQuantity(nq);
        } else {
            var line = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .variant(variant)
                    .quantity(req.getQuantity())
                    .build();
            cart.getItems().add(line);
        }
        cartRepository.save(cart);
        return buildResponse(cart);
    }

    @Transactional
    public CartResponse updateItem(Long itemId, UpdateCartItemRequest req) {
        var cart = getCartOrThrow();
        var item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Cart line not found"));
        int max = PricingUtil.availableStock(item.getProduct(), item.getVariant());
        if (req.getQuantity() > max) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Not enough stock (available: " + max + ")");
        }
        item.setQuantity(req.getQuantity());
        cartRepository.save(cart);
        return buildResponse(cart);
    }

    @Transactional
    public CartResponse removeItem(Long itemId) {
        var cart = getCartOrThrow();
        cart.getItems().removeIf(i -> i.getId().equals(itemId));
        cartRepository.save(cart);
        return buildResponse(cart);
    }

    @Transactional
    public CartResponse applyCoupon(CouponApplyRequest req) {
        var cart = getCartOrThrow();
        var coupon = couponRepository.findByCodeIgnoreCase(req.getCode().trim())
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "Invalid coupon"));
        validateCoupon(coupon);
        cart.setAppliedCouponCode(coupon.getCode());
        cartRepository.save(cart);
        return buildResponse(cart);
    }

    @Transactional
    public CartResponse removeCoupon() {
        var cart = getCartOrThrow();
        cart.setAppliedCouponCode(null);
        cartRepository.save(cart);
        return buildResponse(cart);
    }

    private void validateCoupon(Coupon coupon) {
        if (!coupon.isActive()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Coupon inactive");
        }
        if (coupon.getExpiresAt() != null && coupon.getExpiresAt().isBefore(java.time.Instant.now())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Coupon expired");
        }
        if (coupon.getMaxUses() != null && coupon.getCurrentUses() >= coupon.getMaxUses()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Coupon usage limit reached");
        }
    }

    private Cart getCartOrThrow() {
        var principal = SecurityUtils.requireUser();
        if (principal.getRole() != Role.CUSTOMER) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Customers only");
        }
        var user = userRepository.findById(principal.getId()).orElseThrow();
        return cartRepository.findByCustomer(user)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Cart empty"));
    }

    private CartResponse buildResponse(Cart cart) {
        var lines = cart.getItems().stream().map(this::toLine).toList();
        BigDecimal subtotal = lines.stream()
                .map(CartLineResponse::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal discount = BigDecimal.ZERO;
        Coupon coupon = null;
        if (cart.getAppliedCouponCode() != null) {
            coupon = couponRepository.findByCodeIgnoreCase(cart.getAppliedCouponCode()).orElse(null);
            if (coupon != null && coupon.isActive()) {
                try {
                    validateCoupon(coupon);
                    discount = computeDiscount(coupon, subtotal);
                } catch (BusinessException e) {
                    discount = BigDecimal.ZERO;
                }
            }
        }

        BigDecimal afterDiscount = subtotal.subtract(discount).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        BigDecimal shipping = afterDiscount.compareTo(FREE_SHIPPING_THRESHOLD) >= 0
                ? BigDecimal.ZERO : SHIPPING_FLAT;
        BigDecimal total = afterDiscount.add(shipping).setScale(2, RoundingMode.HALF_UP);

        return CartResponse.builder()
                .cartId(cart.getId())
                .lines(lines)
                .subtotal(subtotal)
                .discount(discount)
                .shippingFee(shipping)
                .totalTtc(total)
                .couponCode(cart.getAppliedCouponCode())
                .build();
    }

    private BigDecimal computeDiscount(Coupon coupon, BigDecimal subtotal) {
        if (coupon.getType() == CouponType.PERCENT) {
            return subtotal.multiply(coupon.getValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                    .min(subtotal);
        }
        return coupon.getValue().min(subtotal);
    }

    private CartLineResponse toLine(CartItem item) {
        var p = item.getProduct();
        var unit = PricingUtil.unitPrice(p, item.getVariant());
        var max = PricingUtil.availableStock(p, item.getVariant());
        String vLabel = item.getVariant() == null ? null
                : item.getVariant().getAttributeName() + ": " + item.getVariant().getAttributeValue();
        String img = p.getImageUrls().isEmpty() ? null : p.getImageUrls().get(0);
        return CartLineResponse.builder()
                .itemId(item.getId())
                .productId(p.getId())
                .productName(p.getName())
                .variantId(item.getVariant() != null ? item.getVariant().getId() : null)
                .variantLabel(vLabel)
                .quantity(item.getQuantity())
                .maxQuantity(max)
                .unitPrice(unit)
                .lineTotal(unit.multiply(BigDecimal.valueOf(item.getQuantity())).setScale(2, RoundingMode.HALF_UP))
                .imageUrl(img)
                .build();
    }
}
