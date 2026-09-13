package com.janaai.service;

import com.janaai.dto.user.UpdateProfileRequest;
import com.janaai.entity.User;

public interface UserService {
    User updateProfile(String userId, UpdateProfileRequest request);
    User getUserById(String userId);
    User uploadProfilePhoto(String userId, org.springframework.web.multipart.MultipartFile file);
}
