package com.shopflow.controller;

import com.shopflow.dto.review.ReviewRequest;
import com.shopflow.dto.review.ReviewResponse;
import com.shopflow.service.ReviewService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ReviewResponse create(@Valid @RequestBody ReviewRequest req) {
        return reviewService.create(req);
    }

    @GetMapping("/product/{productId}")
    public Page<ReviewResponse> forProduct(
            @PathVariable Long productId,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return reviewService.forProduct(productId, pageable);
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ReviewResponse approve(@PathVariable Long id) {
        return reviewService.approve(id);
    }
}
