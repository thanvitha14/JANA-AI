package com.janaai.dto.auth;

import lombok.*;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private UUID id;
    private String fullName;
    private String email;
    private String role;
    private String profilePhotoUrl;
}
