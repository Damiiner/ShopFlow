package com.shopflow.service;

import com.shopflow.domain.Role;
import com.shopflow.exception.BusinessException;
import com.shopflow.repository.UserRepository;
import com.shopflow.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;

    @Transactional
    public void setActive(Long userId, boolean active) {
        if (SecurityUtils.requireUser().getRole() != Role.ADMIN) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Admin only");
        }
        var u = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "User not found"));
        if (u.getRole() == Role.ADMIN) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Cannot deactivate admin this way");
        }
        u.setActive(active);
        userRepository.save(u);
    }
}
