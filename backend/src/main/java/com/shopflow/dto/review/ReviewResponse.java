package com.shopflow.dto.review;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class ReviewResponse {
    Long id;
    int rating;
    String comment;
    String authorName;
    Instant createdAt;
    boolean approved;
}
