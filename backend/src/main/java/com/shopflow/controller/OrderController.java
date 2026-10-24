package com.shopflow.controller;

import com.shopflow.dto.order.OrderResponse;
import com.shopflow.dto.order.OrderStatusUpdateRequest;
import com.shopflow.dto.order.PlaceOrderRequest;
import com.shopflow.service.OrderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public OrderResponse place(@Valid @RequestBody PlaceOrderRequest req) {
        return orderService.placeOrder(req);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Page<OrderResponse> my(@PageableDefault(size = 20) Pageable pageable) {
        return orderService.myOrders(pageable);
    }

    @GetMapping("/seller/mine")
    @PreAuthorize("hasRole('SELLER')")
    public Page<OrderResponse> sellerOrders(@PageableDefault(size = 20) Pageable pageable) {
        return orderService.sellerOrders(pageable);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<OrderResponse> all(@PageableDefault(size = 20) Pageable pageable) {
        return orderService.allOrders(pageable);
    }

    @GetMapping("/{id}")
    public OrderResponse get(@PathVariable Long id) {
        return orderService.getByIdAndClearFlag(id);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    public OrderResponse status(@PathVariable Long id, @Valid @RequestBody OrderStatusUpdateRequest req) {
        return orderService.updateStatus(id, req);
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    public OrderResponse cancel(@PathVariable Long id) {
        return orderService.cancel(id);
    }
}
