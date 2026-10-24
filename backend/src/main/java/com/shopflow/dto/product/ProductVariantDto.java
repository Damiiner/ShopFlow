package com.shopflow.dto.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductVariantDto {
    private Long id;
    @NotBlank
    private String attributeName;
    @NotBlank
    private String attributeValue;
    @NotNull
    private Integer additionalStock;
    private BigDecimal priceDelta;
}
