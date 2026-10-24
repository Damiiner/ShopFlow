package com.shopflow.dto.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddCartItemRequest {
    @NotNull
    private Long productId;
    private Long variantId;

    @NotNull
    @Min(1)
    private Integer quantity;
}
