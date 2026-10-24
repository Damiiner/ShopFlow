package com.shopflow.dto.profile;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserActiveRequest {
    @NotNull
    private Boolean active;
}
