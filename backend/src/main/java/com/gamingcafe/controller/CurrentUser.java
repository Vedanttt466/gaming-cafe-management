package com.gamingcafe.controller;

import com.gamingcafe.entity.User;
import com.gamingcafe.security.CustomUserDetails;
import com.gamingcafe.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/** Small helper to resolve the authenticated User entity from the security context. */
@Component
@RequiredArgsConstructor
public class CurrentUser {

    private final UserService userService;

    public User resolve(Authentication authentication) {
        CustomUserDetails details = (CustomUserDetails) authentication.getPrincipal();
        return details.getUser();
    }
}
