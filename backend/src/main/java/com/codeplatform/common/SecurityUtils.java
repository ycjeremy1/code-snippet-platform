package com.codeplatform.common;

import com.codeplatform.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    /** Returns the authenticated user's id, or {@code null} for anonymous requests to public endpoints. */
    public static Long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            return null;
        }
        return principal.getId();
    }

    public static Long requireUserId() {
        Long userId = currentUserId();
        if (userId == null) {
            throw new IllegalStateException("Endpoint requires authentication");
        }
        return userId;
    }
}
