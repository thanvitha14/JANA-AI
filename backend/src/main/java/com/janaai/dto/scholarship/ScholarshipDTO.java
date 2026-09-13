package com.janaai.dto.scholarship;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScholarshipDTO {

    private UUID id;

    @NotBlank(message = "Scholarship name is required")
    @Size(max = 255, message = "Scholarship name must not exceed 255 characters")
    private String name;

    @NotBlank(message = "Category is required")
    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;

    @NotBlank(message = "Education level is required")
    @Size(max = 100, message = "Education level must not exceed 100 characters")
    private String educationLevel;

    private Double incomeLimit;

    @Size(max = 150, message = "State name must not exceed 150 characters")
    private String state;

    private String eligibility;

    private Double amount;

    private LocalDate deadline;

    private String description;

    @Size(max = 500, message = "Official website URL must not exceed 500 characters")
    private String officialWebsite;

    @Size(max = 500, message = "Apply link URL must not exceed 500 characters")
    private String applyLink;

    private LocalDateTime createdAt;
}
