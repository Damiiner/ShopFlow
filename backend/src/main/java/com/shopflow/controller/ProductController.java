package com.shopflow.controller;

import com.shopflow.dto.product.ProductCreateRequest;
import com.shopflow.dto.product.ProductDetailResponse;
import com.shopflow.dto.product.ProductSummaryResponse;
import com.shopflow.service.ProductService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public Page<ProductSummaryResponse> list(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal priceMin,
            @RequestParam(required = false) BigDecimal priceMax,
            @RequestParam(required = false) Long sellerId,
            @RequestParam(required = false) Boolean promo,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        return productService.list(categoryId, priceMin, priceMax, sellerId, promo, sort, page, size);
    }

    @GetMapping("/search")
    public Page<ProductSummaryResponse> search(
            @RequestParam String q,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        return productService.search(q, sort, page, size);
    }

    @GetMapping("/top-selling")
    public List<ProductSummaryResponse> topSelling() {
        return productService.topSelling();
    }

    @GetMapping("/{id}")
    public ProductDetailResponse get(@PathVariable Long id) {
        return productService.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    public ProductDetailResponse create(
            @Valid @RequestBody ProductCreateRequest req,
            @RequestParam(required = false) Long sellerUserId
    ) {
        return productService.createProduct(req, sellerUserId);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    public ProductDetailResponse update(@PathVariable Long id, @Valid @RequestBody ProductCreateRequest req) {
        return productService.update(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        productService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
