package com.shopflow.util;

import com.shopflow.exception.BusinessException;
import com.shopflow.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static UserPrincipal requireUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal up)) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return up;
    }
}
