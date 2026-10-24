package com.shopflow.controller;

import com.shopflow.dto.profile.AddressRequest;
import com.shopflow.dto.profile.AddressResponse;
import com.shopflow.dto.profile.UserActiveRequest;
import com.shopflow.service.AddressService;
import com.shopflow.service.AdminUserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Addresses & admin users")
public class AddressController {

    private final AddressService addressService;
    private final AdminUserService adminUserService;

    @GetMapping("/api/addresses")
    @PreAuthorize("hasRole('CUSTOMER')")
    public List<AddressResponse> list() {
        return addressService.myAddresses();
    }

    @PostMapping("/api/addresses")
    @PreAuthorize("hasRole('CUSTOMER')")
    public AddressResponse add(@Valid @RequestBody AddressRequest req) {
        return addressService.add(req);
    }

    @PutMapping("/api/addresses/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public AddressResponse update(@PathVariable Long id, @Valid @RequestBody AddressRequest req) {
        return addressService.update(id, req);
    }

    @DeleteMapping("/api/addresses/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        addressService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/api/users/{id}/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> setActive(@PathVariable Long id, @Valid @RequestBody UserActiveRequest req) {
        adminUserService.setActive(id, Boolean.TRUE.equals(req.getActive()));
        return ResponseEntity.noContent().build();
    }
}
