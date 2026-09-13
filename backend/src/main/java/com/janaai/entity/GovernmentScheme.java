package com.janaai.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "government_schemes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GovernmentScheme {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "scheme_name", nullable = false, length = 255)
    private String schemeName;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(length = 150)
    private String state;

    @Column(columnDefinition = "TEXT")
    private String eligibility;

    @Column(name = "income_limit")
    private Double incomeLimit;

    @Column(length = 50)
    @Builder.Default
    private String gender = "ALL";

    @Column(name = "minimum_age")
    private Integer minimumAge;

    @Column(name = "maximum_age")
    private Integer maximumAge;

    @Column(length = 255)
    private String education;

    @Column(columnDefinition = "TEXT")
    private String benefits;

    @Column(name = "required_documents", columnDefinition = "TEXT")
    private String requiredDocuments;

    @Column(name = "official_website", length = 500)
    private String officialWebsite;

    @Column(name = "apply_link", length = 500)
    private String applyLink;

    @Column(name = "deadline")
    private java.time.LocalDate deadline;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

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
