package com.shopflow.controller;

import com.shopflow.dto.cart.*;
import com.shopflow.service.CartService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Cart")
@PreAuthorize("hasRole('CUSTOMER')")
public class CartController {

    private final CartService cartService;

    @GetMapping
    public CartResponse get() {
        return cartService.getMyCart();
    }

    @PostMapping("/items")
    public CartResponse add(@Valid @RequestBody AddCartItemRequest req) {
        return cartService.addItem(req);
    }

    @PutMapping("/items/{itemId}")
    public CartResponse update(@PathVariable Long itemId, @Valid @RequestBody UpdateCartItemRequest req) {
        return cartService.updateItem(itemId, req);
    }

    @DeleteMapping("/items/{itemId}")
    public CartResponse remove(@PathVariable Long itemId) {
        return cartService.removeItem(itemId);
    }

    @PostMapping("/coupon")
    public CartResponse applyCoupon(@Valid @RequestBody CouponApplyRequest req) {
        return cartService.applyCoupon(req);
    }

    @DeleteMapping("/coupon")
    public CartResponse removeCoupon() {
        return cartService.removeCoupon();
    }
}
