package com.shopflow.service;

import com.shopflow.domain.Address;
import com.shopflow.domain.Role;
import com.shopflow.dto.profile.AddressRequest;
import com.shopflow.dto.profile.AddressResponse;
import com.shopflow.exception.BusinessException;
import com.shopflow.repository.AddressRepository;
import com.shopflow.repository.UserRepository;
import com.shopflow.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<AddressResponse> myAddresses() {
        var user = currentCustomer();
        return addressRepository.findByUserOrderByPrincipalDescIdAsc(user).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public AddressResponse add(AddressRequest req) {
        var user = currentCustomer();
        if (req.isPrincipal()) {
            unsetPrincipal(user);
        }
        var a = Address.builder()
                .user(user)
                .street(req.getStreet())
                .city(req.getCity())
                .postalCode(req.getPostalCode())
                .country(req.getCountry())
                .principal(req.isPrincipal())
                .build();
        if (!req.isPrincipal() && addressRepository.findByUserOrderByPrincipalDescIdAsc(user).isEmpty()) {
            a.setPrincipal(true);
        }
        return toResponse(addressRepository.save(a));
    }

    @Transactional
    public AddressResponse update(Long id, AddressRequest req) {
        var user = currentCustomer();
        var a = addressRepository.findById(id)
                .filter(x -> x.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Address not found"));
        if (req.isPrincipal()) {
            unsetPrincipal(user);
        }
        a.setStreet(req.getStreet());
        a.setCity(req.getCity());
        a.setPostalCode(req.getPostalCode());
        a.setCountry(req.getCountry());
        a.setPrincipal(req.isPrincipal());
        return toResponse(addressRepository.save(a));
    }

    @Transactional
    public void delete(Long id) {
        var user = currentCustomer();
        var a = addressRepository.findById(id)
                .filter(x -> x.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Address not found"));
        addressRepository.delete(a);
    }

    private void unsetPrincipal(com.shopflow.domain.User user) {
        for (var a : addressRepository.findByUserOrderByPrincipalDescIdAsc(user)) {
            if (a.isPrincipal()) {
                a.setPrincipal(false);
                addressRepository.save(a);
            }
        }
    }

    private com.shopflow.domain.User currentCustomer() {
        var p = SecurityUtils.requireUser();
        if (p.getRole() != Role.CUSTOMER) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Customers only");
        }
        return userRepository.findById(p.getId()).orElseThrow();
    }

    private AddressResponse toResponse(Address a) {
        return AddressResponse.builder()
                .id(a.getId())
                .street(a.getStreet())
                .city(a.getCity())
                .postalCode(a.getPostalCode())
                .country(a.getCountry())
                .principal(a.isPrincipal())
                .build();
    }
}
