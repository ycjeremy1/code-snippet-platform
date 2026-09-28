package com.codeplatform.controller;

import com.codeplatform.common.SecurityUtils;
import com.codeplatform.dto.UserResponse;
import com.codeplatform.service.UserService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserResponse me() {
        return userService.getById(SecurityUtils.requireUserId());
    }

    @GetMapping("/{username}")
    public UserResponse getByUsername(@PathVariable String username) {
        return userService.getByUsername(username);
    }

    @PutMapping("/me")
    public UserResponse updateProfile(@RequestBody ProfileUpdateRequest request) {
        return userService.updateProfile(SecurityUtils.requireUserId(), request.getBio(), request.getAvatarUrl());
    }

    @Data
    public static class ProfileUpdateRequest {
        private String bio;
        private String avatarUrl;
    }
}
