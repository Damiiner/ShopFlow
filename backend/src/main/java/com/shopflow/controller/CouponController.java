package com.shopflow.controller;

import com.shopflow.dto.coupon.CouponRequest;
import com.shopflow.dto.coupon.CouponResponse;
import com.shopflow.service.CouponService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
@Tag(name = "Coupons")
public class CouponController {

    private final CouponService couponService;

    @GetMapping("/validate/{code}")
    public CouponResponse validate(@PathVariable String code) {
        return couponService.validate(code);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<CouponResponse> list() {
        return couponService.listAll();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public CouponResponse create(@Valid @RequestBody CouponRequest req) {
        return couponService.create(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public CouponResponse update(@PathVariable Long id, @Valid @RequestBody CouponRequest req) {
        return couponService.update(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        couponService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
