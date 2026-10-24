package com.shopflow.service;

import com.shopflow.domain.*;
import com.shopflow.dto.product.*;
import com.shopflow.exception.BusinessException;
import com.shopflow.repository.CategoryRepository;
import com.shopflow.repository.ProductRepository;
import com.shopflow.repository.ReviewRepository;
import com.shopflow.repository.UserRepository;
import com.shopflow.repository.spec.ProductSpecification;
import com.shopflow.util.PricingUtil;
import com.shopflow.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<ProductSummaryResponse> list(
            Long categoryId,
            BigDecimal priceMin,
            BigDecimal priceMax,
            Long sellerId,
            Boolean promo,
            String sort,
            int page,
            int size
    ) {
        Specification<Product> spec = Specification.allOf(
                ProductSpecification.base(),
                ProductSpecification.categoryId(categoryId),
                ProductSpecification.sellerId(sellerId),
                ProductSpecification.priceMin(priceMin),
                ProductSpecification.priceMax(priceMax),
                ProductSpecification.onPromoOnly(promo)
        );
        Sort s = resolveSort(sort);
        Pageable pageable = PageRequest.of(page, size, s);
        return productRepository.findAll(spec, pageable).map(this::toSummary);
    }

    @Transactional(readOnly = true)
    public Page<ProductSummaryResponse> search(String q, String sort, int page, int size) {
        Specification<Product> spec = Specification.allOf(
                ProductSpecification.base(),
                ProductSpecification.searchQuery(q)
        );
        Sort s = resolveSort(sort);
        return productRepository.findAll(spec, PageRequest.of(page, size, s)).map(this::toSummary);
    }

    private Sort resolveSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        var parts = sort.split(",");
        String field = parts[0].trim();
        Sort.Direction dir = parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim())
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        if ("price".equals(field)) {
            return Sort.by(dir, "price");
        }
        if ("popular".equals(field)) {
            return Sort.by(dir, "totalSold");
        }
        return Sort.by(dir, "createdAt");
    }

    @Transactional(readOnly = true)
    public ProductDetailResponse getById(Long id) {
        var p = productRepository.findById(id)
                .filter(Product::isActive)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Product not found"));
        return toDetail(p, true);
    }

    @Transactional(readOnly = true)
    public ProductDetailResponse getByIdForOwner(Long id) {
        var p = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Product not found"));
        return toDetail(p, true);
    }

    @Transactional(readOnly = true)
    public java.util.List<ProductSummaryResponse> topSelling() {
        return productRepository.findAll(
                        Specification.allOf(ProductSpecification.base()),
                        PageRequest.of(0, 10, Sort.Direction.DESC, "totalSold")
                ).stream()
                .map(this::toSummary)
                .toList();
    }

    @Transactional
    public ProductDetailResponse createProduct(ProductCreateRequest req, Long sellerUserIdOverride) {
        var principal = SecurityUtils.requireUser();
        User seller;
        if (principal.getRole() == Role.ADMIN && sellerUserIdOverride != null) {
            seller = userRepository.findById(sellerUserIdOverride)
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Seller not found"));
            if (seller.getRole() != Role.SELLER) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "Target user must be a seller");
            }
        } else if (principal.getRole() == Role.SELLER) {
            seller = userRepository.findById(principal.getId()).orElseThrow();
        } else if (principal.getRole() == Role.ADMIN) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Provide sellerUserId query param for admin");
        } else {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Seller or admin only");
        }

        var product = new Product();
        applyRequest(product, seller, req);
        product = productRepository.save(product);
        return toDetail(product, false);
    }

    @Transactional
    public ProductDetailResponse update(Long id, ProductCreateRequest req) {
        var principal = SecurityUtils.requireUser();
        var product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Product not found"));
        if (principal.getRole() != Role.ADMIN && !product.getSeller().getId().equals(principal.getId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Not your product");
        }
        applyRequest(product, product.getSeller(), req);
        product = productRepository.save(product);
        return toDetail(product, false);
    }

    @Transactional
    public void deactivate(Long id) {
        var principal = SecurityUtils.requireUser();
        var product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Product not found"));
        if (principal.getRole() != Role.ADMIN && !product.getSeller().getId().equals(principal.getId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Not your product");
        }
        product.setActive(false);
        productRepository.save(product);
    }

    private void applyRequest(Product product, User seller, ProductCreateRequest req) {
        product.setSeller(seller);
        product.setName(req.getName());
        product.setDescription(req.getDescription() != null ? req.getDescription() : "");
        product.setPrice(req.getPrice());
        product.setPromoPrice(req.getPromoPrice());
        product.setStock(req.getStock());
        product.setActive(true);

        var cats = req.getCategoryIds().stream()
                .map(cid -> categoryRepository.findById(cid)
                        .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "Unknown category: " + cid)))
                .collect(Collectors.toSet());
        product.getCategories().clear();
        product.getCategories().addAll(cats);

        product.getImageUrls().clear();
        if (req.getImageUrls() != null) {
            product.getImageUrls().addAll(req.getImageUrls());
        }

        product.getVariants().clear();
        if (req.getVariants() != null) {
            for (var vd : req.getVariants()) {
                var v = ProductVariant.builder()
                        .product(product)
                        .attributeName(vd.getAttributeName())
                        .attributeValue(vd.getAttributeValue())
                        .additionalStock(vd.getAdditionalStock() != null ? vd.getAdditionalStock() : 0)
                        .priceDelta(vd.getPriceDelta() != null ? vd.getPriceDelta() : BigDecimal.ZERO)
                        .build();
                product.getVariants().add(v);
            }
        }
    }

    private ProductSummaryResponse toSummary(Product p) {
        var avg = productRepository.averageRatingByProductId(p.getId());
        String img = p.getImageUrls().isEmpty() ? null : p.getImageUrls().get(0);
        String shop = p.getSeller().getSellerProfile() != null
                ? p.getSeller().getSellerProfile().getShopName()
                : p.getSeller().getEmail();
        return ProductSummaryResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .price(p.getPrice())
                .promoPrice(p.getPromoPrice())
                .onPromo(p.isOnPromo())
                .discountPercent(PricingUtil.discountPercent(p))
                .mainImageUrl(img)
                .averageRating(avg != null ? Math.round(avg * 10.0) / 10.0 : null)
                .shopName(shop)
                .sellerId(p.getSeller().getId())
                .build();
    }

    private ProductDetailResponse toDetail(Product p, boolean includeReviewsMeta) {
        var avg = productRepository.averageRatingByProductId(p.getId());
        long rc = includeReviewsMeta
                ? reviewRepository.findByProductAndApprovedTrue(p).size()
                : 0;
        var variants = p.getVariants().stream()
                .map(v -> {
                    var dto = new ProductVariantDto();
                    dto.setId(v.getId());
                    dto.setAttributeName(v.getAttributeName());
                    dto.setAttributeValue(v.getAttributeValue());
                    dto.setAdditionalStock(v.getAdditionalStock());
                    dto.setPriceDelta(v.getPriceDelta());
                    return dto;
                })
                .toList();
        var cats = p.getCategories().stream()
                .map(c -> CategoryTinyDto.builder().id(c.getId()).name(c.getName()).build())
                .toList();
        String shop = p.getSeller().getSellerProfile() != null
                ? p.getSeller().getSellerProfile().getShopName()
                : p.getSeller().getEmail();
        return ProductDetailResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .price(p.getPrice())
                .promoPrice(p.getPromoPrice())
                .onPromo(p.isOnPromo())
                .discountPercent(PricingUtil.discountPercent(p))
                .stock(p.getStock())
                .imageUrls(new ArrayList<>(p.getImageUrls()))
                .variants(variants)
                .categories(cats)
                .averageRating(avg != null ? Math.round(avg * 10.0) / 10.0 : null)
                .reviewCount(rc)
                .shopName(shop)
                .sellerId(p.getSeller().getId())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
