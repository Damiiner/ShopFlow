package com.shopflow.service;

import com.shopflow.domain.Coupon;
import com.shopflow.domain.Role;
import com.shopflow.dto.coupon.CouponRequest;
import com.shopflow.dto.coupon.CouponResponse;
import com.shopflow.exception.BusinessException;
import com.shopflow.repository.CouponRepository;
import com.shopflow.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;

    @Transactional(readOnly = true)
    public CouponResponse validate(String code) {
        var c = couponRepository.findByCodeIgnoreCase(code.trim())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Unknown coupon"));
        if (!c.isActive()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Coupon inactive");
        }
        if (c.getExpiresAt() != null && c.getExpiresAt().isBefore(Instant.now())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Expired");
        }
        if (c.getMaxUses() != null && c.getCurrentUses() >= c.getMaxUses()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Usage limit reached");
        }
        return toResponse(c);
    }

    @Transactional
    public CouponResponse create(CouponRequest req) {
        requireAdmin();
        if (couponRepository.findByCodeIgnoreCase(req.getCode().trim()).isPresent()) {
            throw new BusinessException(HttpStatus.CONFLICT, "Code already exists");
        }
        var c = Coupon.builder()
                .code(req.getCode().trim().toUpperCase())
                .type(req.getType())
                .value(req.getValue())
                .expiresAt(req.getExpiresAt())
                .maxUses(req.getMaxUses())
                .currentUses(0)
                .active(req.isActive())
                .build();
        return toResponse(couponRepository.save(c));
    }

    @Transactional
    public CouponResponse update(Long id, CouponRequest req) {
        requireAdmin();
        var c = couponRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Coupon not found"));
        c.setCode(req.getCode().trim().toUpperCase());
        c.setType(req.getType());
        c.setValue(req.getValue());
        c.setExpiresAt(req.getExpiresAt());
        c.setMaxUses(req.getMaxUses());
        c.setActive(req.isActive());
        return toResponse(couponRepository.save(c));
    }

    @Transactional
    public void delete(Long id) {
        requireAdmin();
        couponRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<CouponResponse> listAll() {
        requireAdmin();
        return couponRepository.findAll().stream().map(this::toResponse).toList();
    }

    private void requireAdmin() {
        if (SecurityUtils.requireUser().getRole() != Role.ADMIN) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Admin only");
        }
    }

    private CouponResponse toResponse(Coupon c) {
        return CouponResponse.builder()
                .id(c.getId())
                .code(c.getCode())
                .type(c.getType())
                .value(c.getValue())
                .expiresAt(c.getExpiresAt())
                .maxUses(c.getMaxUses())
                .currentUses(c.getCurrentUses())
                .active(c.isActive())
                .build();
    }
}
