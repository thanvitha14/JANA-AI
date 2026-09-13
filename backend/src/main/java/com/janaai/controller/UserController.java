package com.janaai.controller;

import com.janaai.dto.response.ApiResponse;
import com.janaai.dto.user.UpdateProfileRequest;
import com.janaai.entity.User;
import com.janaai.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<User>> getProfile(@AuthenticationPrincipal User user) {
        User currentUser = userService.getUserById(user.getId());
        return ResponseEntity.ok(ApiResponse.success(currentUser, "Profile retrieved successfully"));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<User>> updateProfile(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        User updatedUser = userService.updateProfile(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(updatedUser, "Profile updated successfully"));
    }

    @PostMapping("/profile/photo")
    public ResponseEntity<ApiResponse<User>> uploadProfilePhoto(
            @AuthenticationPrincipal User user,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file
    ) {
        User updatedUser = userService.uploadProfilePhoto(user.getId(), file);
        return ResponseEntity.ok(ApiResponse.success(updatedUser, "Profile photo uploaded successfully"));
    }
}
