package com.truong.common.utils;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public class SecurityUtils {

    public static Jwt getCurrentUserJwt() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            return jwtAuth.getToken();
        }
        return null;
    }

    public static Integer getCurrentUserId() {
        Jwt jwt = getCurrentUserJwt();
        if (jwt != null) {
            // Lấy id từ claim custom của bạn
            return  Integer.parseInt(jwt.getClaimAsString("id"));
        }
        return null;
    }
}