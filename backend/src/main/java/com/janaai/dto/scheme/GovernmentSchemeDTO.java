package com.janaai.dto.scheme;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GovernmentSchemeDTO {

    private String id;
    private java.time.LocalDate deadline;

    @NotBlank(message = "Scheme name is required")
    @Size(max = 255, message = "Scheme name must not exceed 255 characters")
    private String schemeName;

    @NotBlank(message = "Category is required")
    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;

    @NotBlank(message = "Description is required")
    private String description;

    @Size(max = 150, message = "State name must not exceed 150 characters")
    private String state;

    private String eligibility;

    private Double incomeLimit;

    @Size(max = 50, message = "Gender must not exceed 50 characters")
    private String gender;

    private Integer minimumAge;

    private Integer maximumAge;

    @Size(max = 255, message = "Education must not exceed 255 characters")
    private String education;

    private String benefits;

    private String requiredDocuments;

    @Size(max = 500, message = "Official website URL must not exceed 500 characters")
    private String officialWebsite;

    @Size(max = 500, message = "Apply link URL must not exceed 500 characters")
    private String applyLink;

    private boolean active;

    private LocalDateTime createdAt;
}
