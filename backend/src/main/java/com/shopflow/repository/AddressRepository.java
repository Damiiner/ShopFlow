package com.shopflow.repository;

import com.shopflow.domain.Address;
import com.shopflow.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUserOrderByPrincipalDescIdAsc(User user);
}
