package com.example.backend.common.util;

import com.example.backend.common.exception.BusinessException;
import com.example.backend.common.config.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {
    private SecurityUtils() {
    }

    public static Long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal)) {
            throw new BusinessException(401, "未登录");
        }
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return principal.getId();
    }
}
