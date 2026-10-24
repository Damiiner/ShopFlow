package com.shopflow.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShopOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id")
    private User customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false, unique = true)
    private String orderNumber;

    @Column(nullable = false, length = 500)
    private String shippingStreet;

    @Column(nullable = false)
    private String shippingCity;

    @Column(nullable = false)
    private String shippingPostalCode;

    @Column(nullable = false)
    private String shippingCountry;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal shippingFee;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal discountAmount;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalTtc;

    @Column(nullable = false)
    private Instant orderedAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean statusNotificationNew = true;

    @OneToMany(mappedBy = "shopOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @PrePersist
    void prePersist() {
        if (orderedAt == null) {
            orderedAt = Instant.now();
        }
    }
}
