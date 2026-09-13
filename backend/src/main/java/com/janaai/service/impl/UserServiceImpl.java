package com.janaai.service.impl;

import com.janaai.dto.user.UpdateProfileRequest;
import com.janaai.entity.User;
import com.janaai.exception.DuplicateResourceException;
import com.janaai.exception.ResourceNotFoundException;
import com.janaai.repository.UserRepository;
import com.janaai.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public User getUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
    }

    @Override
    @Transactional
    public User updateProfile(String userId, UpdateProfileRequest request) {
        User user = getUserById(userId);

        // Check if phone number is updated and if it is already taken by another user
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank() 
                && !request.getPhoneNumber().equals(user.getPhoneNumber())) {
            if (userRepository.findByPhoneNumber(request.getPhoneNumber()).isPresent()) {
                throw new DuplicateResourceException("Phone number already registered to another account");
            }
        }

        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setGender(request.getGender());
        user.setCity(request.getCity());
        user.setState(request.getState());
        user.setPreferredLanguage(request.getPreferredLanguage() != null ? request.getPreferredLanguage() : "en");

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User uploadProfilePhoto(String userId, org.springframework.web.multipart.MultipartFile file) {
        User user = getUserById(userId);

        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot upload empty file");
        }

        // Validate size (5 MB max)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("File size exceeds maximum limit of 5MB");
        }

        // Validate type (JPG, JPEG, PNG, WEBP)
        String contentType = file.getContentType();
        if (contentType == null || !(contentType.equals("image/jpeg") || contentType.equals("image/jpg") || 
                contentType.equals("image/png") || contentType.equals("image/webp"))) {
            throw new IllegalArgumentException("Only JPG, JPEG, PNG, and WEBP image formats are supported");
        }

        try {
            // Ensure uploads/profile-images directory exists
            java.nio.file.Path uploadPath = java.nio.file.Paths.get("uploads", "profile-images");
            if (!java.nio.file.Files.exists(uploadPath)) {
                java.nio.file.Files.createDirectories(uploadPath);
            }

            // Get original extension
            String originalFilename = file.getOriginalFilename();
            String extension = ".jpg"; // default fallback
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            // Generate unique filename
            String filename = java.util.UUID.randomUUID().toString() + extension;
            java.nio.file.Path targetPath = uploadPath.resolve(filename);

            // Copy file to target path
            java.nio.file.Files.copy(file.getInputStream(), targetPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            // Delete old file if exists
            String oldPhotoUrl = user.getProfilePhotoUrl();
            if (oldPhotoUrl != null && oldPhotoUrl.startsWith("/uploads/profile-images/")) {
                String oldFilename = oldPhotoUrl.substring(oldPhotoUrl.lastIndexOf("/") + 1);
                java.nio.file.Path oldFilePath = uploadPath.resolve(oldFilename);
                if (java.nio.file.Files.exists(oldFilePath)) {
                    java.nio.file.Files.delete(oldFilePath);
                }
            }

            // Save new path in DB
            user.setProfilePhotoUrl("/uploads/profile-images/" + filename);
            return userRepository.save(user);

        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to store profile image", e);
        }
    }
}
