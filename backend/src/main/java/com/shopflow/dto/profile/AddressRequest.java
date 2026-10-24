package com.shopflow.dto.profile;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddressRequest {
    @NotBlank
    private String street;
    @NotBlank
    private String city;
    @NotBlank
    private String postalCode;
    @NotBlank
    private String country;
    private boolean principal;
}
