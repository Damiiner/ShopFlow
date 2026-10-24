package com.shopflow.dto.category;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class CategoryTreeResponse {
    Long id;
    String name;
    String description;
    List<CategoryTreeResponse> children;
}
