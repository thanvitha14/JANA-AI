package com.janaai.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "scholarships")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Scholarship {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(name = "education_level", nullable = false, length = 100)
    private String educationLevel;

    @Column(name = "income_limit")
    private Double incomeLimit;

    @Column(length = 150)
    private String state;

    @Column(columnDefinition = "TEXT")
    private String eligibility;

    private Double amount;

    private LocalDate deadline;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "official_website", length = 500)
    private String officialWebsite;

    @Column(name = "apply_link", length = 500)
    private String applyLink;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = java.util.UUID.randomUUID().toString();
        }
        this.createdAt = LocalDateTime.now();
    }
}
