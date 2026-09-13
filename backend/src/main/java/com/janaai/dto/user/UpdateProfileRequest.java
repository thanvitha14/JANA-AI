package com.janaai.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {
    
    @NotBlank(message = "Full name is required")
    @Size(max = 150, message = "Full name must be at most 150 characters")
    private String fullName;

    @Size(max = 20, message = "Phone number must be at most 20 characters")
    private String phoneNumber;

    private LocalDate dateOfBirth;

    @Size(max = 20, message = "Gender must be at most 20 characters")
    private String gender;

    private String city;

    private String state;

    @Size(max = 10, message = "Language must be at most 10 characters")
    private String preferredLanguage;
}
