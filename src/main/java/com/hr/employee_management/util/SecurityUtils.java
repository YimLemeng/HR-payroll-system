package com.hr.employee_management.util;

import com.hr.employee_management.Security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class SecurityUtils {
    private SecurityUtils(){}

    public static Optional<UserPrincipal> getCurrentUserPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return Optional.empty();
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof UserPrincipal userPrincipal) {
            return Optional.of(userPrincipal);
        }
        return Optional.empty();
    }

    public static String getCurrentUsername() {
        return getCurrentUserPrincipal().map(UserPrincipal::getUsername).orElse("SYSTEM");
    }

    public static Long getCurrentUserId() {
        return getCurrentUserPrincipal().map(UserPrincipal::getId).orElse(null);
    }
}
