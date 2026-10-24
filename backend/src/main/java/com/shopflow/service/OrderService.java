package com.shopflow.service;

import com.shopflow.domain.*;
import com.shopflow.dto.order.*;
import com.shopflow.exception.BusinessException;
import com.shopflow.repository.*;
import com.shopflow.util.PricingUtil;
import com.shopflow.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final BigDecimal SHIPPING_FLAT = new BigDecimal("5.99");
    private static final BigDecimal FREE_SHIPPING_THRESHOLD = new BigDecimal("50.00");

    private final ShopOrderRepository shopOrderRepository;
    private final CartRepository cartRepository;
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final CouponRepository couponRepository;
    private final ProductRepository productRepository;

    @Transactional
    public OrderResponse placeOrder(PlaceOrderRequest req) {
        var principal = SecurityUtils.requireUser();
        if (principal.getRole() != Role.CUSTOMER) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Customers only");
        }
        var user = userRepository.findById(principal.getId()).orElseThrow();
        var cart = cartRepository.findByCustomer(user)
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "Cart is empty"));
        if (cart.getItems().isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Cart is empty");
        }

        var address = addressRepository.findById(req.getAddressId())
                .filter(a -> a.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Address not found"));

        BigDecimal subtotal = BigDecimal.ZERO;
        for (var line : cart.getItems()) {
            var p = productRepository.findById(line.getProduct().getId()).orElseThrow();
            if (!p.isActive()) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "Product no longer available: " + p.getName());
            }
            int max = PricingUtil.availableStock(p, line.getVariant());
            if (line.getQuantity() > max) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "Insufficient stock for: " + p.getName());
            }
            var unit = PricingUtil.unitPrice(p, line.getVariant());
            subtotal = subtotal.add(unit.multiply(BigDecimal.valueOf(line.getQuantity())));
        }
        subtotal = subtotal.setScale(2, RoundingMode.HALF_UP);

        BigDecimal discount = BigDecimal.ZERO;
        Coupon coupon = null;
        if (cart.getAppliedCouponCode() != null) {
            coupon = couponRepository.findByCodeIgnoreCase(cart.getAppliedCouponCode()).orElse(null);
            if (coupon != null && coupon.isActive()) {
                if (coupon.getExpiresAt() == null || coupon.getExpiresAt().isAfter(Instant.now())) {
                    if (coupon.getMaxUses() == null || coupon.getCurrentUses() < coupon.getMaxUses()) {
                        discount = computeDiscount(coupon, subtotal);
                    }
                }
            }
        }

        BigDecimal afterDiscount = subtotal.subtract(discount).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        BigDecimal shipping = afterDiscount.compareTo(FREE_SHIPPING_THRESHOLD) >= 0
                ? BigDecimal.ZERO : SHIPPING_FLAT;
        BigDecimal total = afterDiscount.add(shipping).setScale(2, RoundingMode.HALF_UP);

        var order = ShopOrder.builder()
                .customer(user)
                .status(OrderStatus.PAID)
                .orderNumber(generateOrderNumber())
                .shippingStreet(address.getStreet())
                .shippingCity(address.getCity())
                .shippingPostalCode(address.getPostalCode())
                .shippingCountry(address.getCountry())
                .subtotal(subtotal)
                .shippingFee(shipping)
                .discountAmount(discount)
                .totalTtc(total)
                .statusNotificationNew(true)
                .build();

        for (var line : cart.getItems()) {
            var p = productRepository.findById(line.getProduct().getId()).orElseThrow();
            var unit = PricingUtil.unitPrice(p, line.getVariant());
            var split = computeStockSplit(p, line.getVariant(), line.getQuantity());
            var oi = OrderItem.builder()
                    .shopOrder(order)
                    .product(p)
                    .variant(line.getVariant())
                    .quantity(line.getQuantity())
                    .deductedFromProduct(split.fromProduct)
                    .deductedFromVariant(split.fromVariant)
                    .unitPrice(unit)
                    .build();
            order.getItems().add(oi);
            applyStockDelta(p, line.getVariant(), split.fromProduct, split.fromVariant);
            p.setTotalSold(p.getTotalSold() + line.getQuantity());
            productRepository.save(p);
        }

        if (coupon != null && discount.signum() > 0) {
            coupon.setCurrentUses(coupon.getCurrentUses() + 1);
            couponRepository.save(coupon);
        }

        cart.getItems().clear();
        cart.setAppliedCouponCode(null);
        cartRepository.save(cart);

        order = shopOrderRepository.save(order);
        return toResponse(order, false);
    }

    private record StockSplit(int fromVariant, int fromProduct) {}

    private StockSplit computeStockSplit(Product p, ProductVariant variant, int quantity) {
        int remaining = quantity;
        int fromVariant = 0;
        if (variant != null) {
            fromVariant = Math.min(remaining, variant.getAdditionalStock());
            remaining -= fromVariant;
        }
        return new StockSplit(fromVariant, remaining);
    }

    private void applyStockDelta(Product p, ProductVariant variant, int fromProduct, int fromVariant) {
        p.setStock(p.getStock() - fromProduct);
        if (variant != null) {
            variant.setAdditionalStock(variant.getAdditionalStock() - fromVariant);
        }
    }

    private void restoreStock(Product p, ProductVariant variant, int fromProduct, int fromVariant) {
        p.setStock(p.getStock() + fromProduct);
        if (variant != null) {
            variant.setAdditionalStock(variant.getAdditionalStock() + fromVariant);
        }
    }

    private BigDecimal computeDiscount(Coupon coupon, BigDecimal subtotal) {
        if (coupon.getType() == CouponType.PERCENT) {
            return subtotal.multiply(coupon.getValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                    .min(subtotal);
        }
        return coupon.getValue().min(subtotal);
    }

    private String generateOrderNumber() {
        return "ORD-" + Year.now().getValue() + "-" + String.format("%05d", (int) (Math.random() * 100000));
    }

    public OrderResponse getById(Long id) {
        var principal = SecurityUtils.requireUser();
        var order = shopOrderRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Order not found"));
        if (principal.getRole() == Role.ADMIN) {
            return toResponse(order, false);
        }
        if (principal.getRole() == Role.CUSTOMER && !order.getCustomer().getId().equals(principal.getId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Not your order");
        }
        if (principal.getRole() == Role.SELLER) {
            boolean owns = order.getItems().stream()
                    .anyMatch(i -> i.getProduct().getSeller().getId().equals(principal.getId()));
            if (!owns) {
                throw new BusinessException(HttpStatus.FORBIDDEN, "No items from your shop in this order");
            }
        }
        return toResponse(order, principal.getRole() == Role.CUSTOMER);
    }

    @Transactional
    public OrderResponse getByIdAndClearFlag(Long id) {
        var res = getById(id);
        if (SecurityUtils.requireUser().getRole() == Role.CUSTOMER) {
            shopOrderRepository.findById(id).ifPresent(o -> {
                o.setStatusNotificationNew(false);
                shopOrderRepository.save(o);
            });
        }
        var order = shopOrderRepository.findById(id).orElseThrow();
        return toResponse(order, false);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> myOrders(Pageable pageable) {
        var principal = SecurityUtils.requireUser();
        if (principal.getRole() != Role.CUSTOMER) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Customers only");
        }
        var user = userRepository.findById(principal.getId()).orElseThrow();
        return shopOrderRepository.findByCustomerOrderByOrderedAtDesc(user, pageable)
                .map(o -> toResponse(o, false));
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> allOrders(Pageable pageable) {
        if (SecurityUtils.requireUser().getRole() != Role.ADMIN) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Admin only");
        }
        return shopOrderRepository.findAllByOrderByOrderedAtDesc(pageable)
                .map(o -> toResponse(o, false));
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> sellerOrders(Pageable pageable) {
        var principal = SecurityUtils.requireUser();
        if (principal.getRole() != Role.SELLER) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Seller only");
        }
        return shopOrderRepository.findBySellerProduct(principal.getId(), pageable)
                .map(o -> toResponse(o, false));
    }

    @Transactional
    public OrderResponse updateStatus(Long id, OrderStatusUpdateRequest req) {
        var principal = SecurityUtils.requireUser();
        var order = shopOrderRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Order not found"));
        if (principal.getRole() == Role.SELLER) {
            boolean owns = order.getItems().stream()
                    .anyMatch(i -> i.getProduct().getSeller().getId().equals(principal.getId()));
            if (!owns) {
                throw new BusinessException(HttpStatus.FORBIDDEN, "Not your order");
            }
        } else if (principal.getRole() != Role.ADMIN) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Seller or admin only");
        }
        order.setStatus(req.getStatus());
        order.setStatusNotificationNew(true);
        shopOrderRepository.save(order);
        return toResponse(order, false);
    }

    @Transactional
    public OrderResponse cancel(Long id) {
        var principal = SecurityUtils.requireUser();
        if (principal.getRole() != Role.CUSTOMER) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Customers only");
        }
        var order = shopOrderRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Order not found"));
        if (!order.getCustomer().getId().equals(principal.getId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Not your order");
        }
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.PAID) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Cannot cancel order in this status");
        }
        for (var oi : order.getItems()) {
            var p = productRepository.findById(oi.getProduct().getId()).orElseThrow();
            restoreStock(p, oi.getVariant(), oi.getDeductedFromProduct(), oi.getDeductedFromVariant());
            p.setTotalSold(Math.max(0, p.getTotalSold() - oi.getQuantity()));
            productRepository.save(p);
        }
        order.setStatus(OrderStatus.CANCELLED);
        order.setStatusNotificationNew(true);
        shopOrderRepository.save(order);
        return toResponse(order, false);
    }

    private OrderResponse toResponse(ShopOrder o, boolean showNewFlag) {
        var items = o.getItems().stream()
                .map(oi -> OrderItemResponse.builder()
                        .productId(oi.getProduct().getId())
                        .productName(oi.getProduct().getName())
                        .variantId(oi.getVariant() != null ? oi.getVariant().getId() : null)
                        .quantity(oi.getQuantity())
                        .unitPrice(oi.getUnitPrice())
                        .build())
                .toList();
        return OrderResponse.builder()
                .id(o.getId())
                .orderNumber(o.getOrderNumber())
                .status(o.getStatus())
                .subtotal(o.getSubtotal())
                .shippingFee(o.getShippingFee())
                .discountAmount(o.getDiscountAmount())
                .totalTtc(o.getTotalTtc())
                .orderedAt(o.getOrderedAt())
                .statusNotificationNew(showNewFlag && o.isStatusNotificationNew())
                .shippingStreet(o.getShippingStreet())
                .shippingCity(o.getShippingCity())
                .shippingPostalCode(o.getShippingPostalCode())
                .shippingCountry(o.getShippingCountry())
                .items(items)
                .build();
    }
}
