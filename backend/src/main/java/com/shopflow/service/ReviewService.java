package com.shopflow.service;

import com.shopflow.domain.*;
import com.shopflow.dto.review.ReviewRequest;
import com.shopflow.dto.review.ReviewResponse;
import com.shopflow.exception.BusinessException;
import com.shopflow.repository.ProductRepository;
import com.shopflow.repository.ReviewRepository;
import com.shopflow.repository.UserRepository;
import com.shopflow.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
    public ReviewResponse create(ReviewRequest req) {
        var principal = SecurityUtils.requireUser();
        if (principal.getRole() != Role.CUSTOMER) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Customers only");
        }
        var user = userRepository.findById(principal.getId()).orElseThrow();
        var product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Product not found"));
        if (!reviewRepository.customerHasPurchasedProduct(user.getId(), product.getId())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "You can only review purchased products");
        }
        if (reviewRepository.existsByCustomerAndProduct(user, product)) {
            throw new BusinessException(HttpStatus.CONFLICT, "You already reviewed this product");
        }
        var r = Review.builder()
                .customer(user)
                .product(product)
                .rating(req.getRating())
                .comment(req.getComment() != null ? req.getComment() : "")
                .approved(false)
                .build();
        r = reviewRepository.save(r);
        return toResponse(r);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> forProduct(Long productId, Pageable pageable) {
        var product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Product not found"));
        return reviewRepository.findByProductAndApprovedTrueOrderByCreatedAtDesc(product, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public ReviewResponse approve(Long id) {
        var r = reviewRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Review not found"));
        r.setApproved(true);
        reviewRepository.save(r);
        return toResponse(r);
    }

    private ReviewResponse toResponse(Review r) {
        var c = r.getCustomer();
        return ReviewResponse.builder()
                .id(r.getId())
                .rating(r.getRating())
                .comment(r.getComment())
                .authorName(c.getFirstName() + " " + (c.getLastName() != null && !c.getLastName().isEmpty()
                        ? c.getLastName().charAt(0) + "." : ""))
                .createdAt(r.getCreatedAt())
                .approved(r.isApproved())
                .build();
    }
}
