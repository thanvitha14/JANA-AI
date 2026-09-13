package com.janaai.controller;

import com.janaai.dto.response.ApiResponse;
import com.janaai.dto.scholarship.ScholarshipDTO;
import com.janaai.entity.Scholarship;
import com.janaai.service.ScholarshipService;
import com.janaai.service.TranslationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/scholarships")
@RequiredArgsConstructor
public class ScholarshipController {

    private final ScholarshipService scholarshipService;
    private final TranslationService translationService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<Scholarship>>> getScholarships(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String educationLevel,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double income,
            @RequestParam(required = false) String state,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(required = false) String lang
    ) {
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Scholarship> scholarships = scholarshipService.getScholarships(
                search, educationLevel, category, income, state, pageable
        );

        if (lang != null && !lang.isBlank() && !"en".equalsIgnoreCase(lang)) {
            scholarships = scholarships.map(s -> cloneAndTranslate(s, lang));
        }

        return ResponseEntity.ok(ApiResponse.success(scholarships, "Scholarships retrieved successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Scholarship>> getScholarshipById(
            @PathVariable String id,
            @RequestParam(required = false) String lang
    ) {
        Scholarship scholarship = scholarshipService.getScholarshipById(id);
        if (lang != null && !lang.isBlank() && !"en".equalsIgnoreCase(lang)) {
            scholarship = cloneAndTranslate(scholarship, lang);
        }
        return ResponseEntity.ok(ApiResponse.success(scholarship, "Scholarship retrieved successfully"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Scholarship>> createScholarship(
            @Valid @RequestBody ScholarshipDTO dto
    ) {
        Scholarship created = scholarshipService.createScholarship(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Scholarship created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Scholarship>> updateScholarship(
            @PathVariable String id,
            @Valid @RequestBody ScholarshipDTO dto
    ) {
        Scholarship updated = scholarshipService.updateScholarship(id, dto);
        return ResponseEntity.ok(ApiResponse.success(updated, "Scholarship updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteScholarship(@PathVariable String id) {
        scholarshipService.deleteScholarship(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Scholarship deleted successfully"));
    }

    private Scholarship cloneAndTranslate(Scholarship scholarship, String lang) {
        Scholarship copy = new Scholarship();
        copy.setId(scholarship.getId());
        copy.setName(translationService.translate(scholarship.getName(), lang));
        copy.setDescription(translationService.translate(scholarship.getDescription(), lang));
        copy.setCategory(translationService.translate(scholarship.getCategory(), lang));
        copy.setState(translationService.translate(scholarship.getState(), lang));
        copy.setEligibility(translationService.translate(scholarship.getEligibility(), lang));
        copy.setIncomeLimit(scholarship.getIncomeLimit());
        copy.setEducationLevel(translationService.translate(scholarship.getEducationLevel(), lang));
        copy.setAmount(scholarship.getAmount());
        copy.setOfficialWebsite(scholarship.getOfficialWebsite());
        copy.setApplyLink(scholarship.getApplyLink());
        copy.setDeadline(scholarship.getDeadline());
        copy.setCreatedAt(scholarship.getCreatedAt());
        return copy;
    }
}
