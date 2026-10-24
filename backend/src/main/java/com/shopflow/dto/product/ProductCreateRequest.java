package com.shopflow.dto.product;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class ProductCreateRequest {

    @NotBlank
    @Size(max = 255)
    private String name;

    @Size(max = 4000)
    private String description;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal price;

    @DecimalMin("0")
    private BigDecimal promoPrice;

    @NotNull
    @Min(0)
    private Integer stock;

    @NotEmpty
    private List<Long> categoryIds = new ArrayList<>();

    private List<String> imageUrls = new ArrayList<>();

    private List<ProductVariantDto> variants = new ArrayList<>();
}
