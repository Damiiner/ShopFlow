package com.shopflow.service;

import com.shopflow.domain.OrderStatus;
import com.shopflow.domain.Role;
import com.shopflow.exception.BusinessException;
import com.shopflow.repository.*;
import com.shopflow.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ShopOrderRepository shopOrderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;

    @Transactional(readOnly = true)
    public Map<String, Object> admin() {
        if (SecurityUtils.requireUser().getRole() != Role.ADMIN) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Admin only");
        }
        var revenue = shopOrderRepository.sumRevenueGlobal();
        var recent = shopOrderRepository.findAllByOrderByOrderedAtDesc(PageRequest.of(0, 5)).getContent().stream()
                .map(o -> Map.<String, Object>of(
                        "id", o.getId(),
                        "orderNumber", o.getOrderNumber(),
                        "status", o.getStatus().name(),
                        "total", o.getTotalTtc(),
                        "orderedAt", o.getOrderedAt().toString()
                ))
                .toList();
        var topP = orderItemRepository.topProducts(PageRequest.of(0, 5)).stream()
                .map(row -> {
                    Long pid = (Long) row[0];
                    Number qty = (Number) row[1];
                    return productRepository.findById(pid)
                            .map(p -> Map.<String, Object>of(
                                    "productId", pid,
                                    "name", p.getName(),
                                    "quantitySold", qty.longValue()
                            ))
                            .orElse(Map.of());
                })
                .filter(m -> !m.isEmpty())
                .toList();
        var topS = orderItemRepository.topSellersByRevenue(PageRequest.of(0, 5)).stream()
                .map(row -> {
                    Long sid = (Long) row[0];
                    BigDecimal rev = (BigDecimal) row[1];
                    return userRepository.findById(sid)
                            .map(u -> Map.<String, Object>of(
                                    "sellerId", sid,
                                    "email", u.getEmail(),
                                    "revenue", rev
                            ))
                            .orElse(Map.of());
                })
                .filter(m -> !m.isEmpty())
                .toList();
        Map<String, Object> out = new HashMap<>();
        out.put("totalRevenue", revenue);
        out.put("recentOrders", recent);
        out.put("topProducts", topP);
        out.put("topSellers", topS);
        out.put("userCounts", Map.of(
                "customers", userRepository.countByRole(Role.CUSTOMER),
                "sellers", userRepository.countByRole(Role.SELLER)
        ));
        return out;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> seller() {
        var principal = SecurityUtils.requireUser();
        if (principal.getRole() != Role.SELLER) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Seller only");
        }
        var rev = shopOrderRepository.sumRevenueBySeller(principal.getId());
        var pending = shopOrderRepository.findBySellerProduct(principal.getId(), PageRequest.of(0, 100)).getContent().stream()
                .filter(o -> o.getStatus() == OrderStatus.PAID || o.getStatus() == OrderStatus.PENDING
                        || o.getStatus() == OrderStatus.PROCESSING)
                .count();
        var lowStock = productRepository.findAll().stream()
                .filter(p -> p.getSeller().getId().equals(principal.getId()) && p.isActive() && p.getStock() < 5)
                .map(p -> Map.<String, Object>of("id", p.getId(), "name", p.getName(), "stock", p.getStock()))
                .toList();
        return Map.of(
                "revenue", rev,
                "pendingOrdersCount", pending,
                "lowStockAlerts", lowStock
        );
    }

    @Transactional(readOnly = true)
    public Map<String, Object> customer() {
        var principal = SecurityUtils.requireUser();
        if (principal.getRole() != Role.CUSTOMER) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Customer only");
        }
        var user = userRepository.findById(principal.getId()).orElseThrow();
        var orders = shopOrderRepository.findByCustomerOrderByOrderedAtDesc(user, PageRequest.of(0, 5))
                .stream()
                .filter(o -> o.getStatus() != OrderStatus.DELIVERED && o.getStatus() != OrderStatus.CANCELLED)
                .map(o -> Map.<String, Object>of(
                        "id", o.getId(),
                        "orderNumber", o.getOrderNumber(),
                        "status", o.getStatus().name()
                ))
                .toList();
        var reviews = reviewRepository.findByCustomerOrderByCreatedAtDesc(user, PageRequest.of(0, 5))
                .map(r -> Map.<String, Object>of(
                        "id", r.getId(),
                        "productId", r.getProduct().getId(),
                        "rating", r.getRating(),
                        "approved", r.isApproved()
                ))
                .toList();
        return Map.of("activeOrders", orders, "recentReviews", reviews);
    }
}
