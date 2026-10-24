package com.shopflow.dto.profile;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AddressResponse {
    Long id;
    String street;
    String city;
    String postalCode;
    String country;
    boolean principal;
}
