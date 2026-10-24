package com.shopflow.dto.product;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CategoryTinyDto {
    Long id;
    String name;
}
