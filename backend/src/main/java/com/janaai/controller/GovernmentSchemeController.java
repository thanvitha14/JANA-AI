package com.janaai.controller;

import com.janaai.dto.response.ApiResponse;
import com.janaai.dto.scheme.GovernmentSchemeDTO;
import com.janaai.entity.GovernmentScheme;
import com.janaai.service.GovernmentSchemeService;
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
@RequestMapping("/api/schemes")
@RequiredArgsConstructor
public class GovernmentSchemeController {

    private final GovernmentSchemeService schemeService;
    private final TranslationService translationService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<GovernmentScheme>>> getSchemes(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) Double income,
            @RequestParam(required = false) Integer age,
            @RequestParam(required = false) String education,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "schemeName") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(required = false) String lang
    ) {
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<GovernmentScheme> schemes = schemeService.getSchemes(
                search, category, state, gender, income, age, education, pageable
        );

        if (lang != null && !lang.isBlank() && !"en".equalsIgnoreCase(lang)) {
            schemes = schemes.map(s -> cloneAndTranslate(s, lang));
        }

        return ResponseEntity.ok(ApiResponse.success(schemes, "Government schemes retrieved successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GovernmentScheme>> getSchemeById(
            @PathVariable String id,
            @RequestParam(required = false) String lang
    ) {
        GovernmentScheme scheme = schemeService.getSchemeById(id);
        if (lang != null && !lang.isBlank() && !"en".equalsIgnoreCase(lang)) {
            scheme = cloneAndTranslate(scheme, lang);
        }
        return ResponseEntity.ok(ApiResponse.success(scheme, "Government scheme retrieved successfully"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<GovernmentScheme>> createScheme(
            @Valid @RequestBody GovernmentSchemeDTO dto
    ) {
        GovernmentScheme created = schemeService.createScheme(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Government scheme created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<GovernmentScheme>> updateScheme(
            @PathVariable String id,
            @Valid @RequestBody GovernmentSchemeDTO dto
    ) {
        GovernmentScheme updated = schemeService.updateScheme(id, dto);
        return ResponseEntity.ok(ApiResponse.success(updated, "Government scheme updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteScheme(@PathVariable String id) {
        schemeService.deleteScheme(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Government scheme deleted successfully"));
    }

    private GovernmentScheme cloneAndTranslate(GovernmentScheme scheme, String lang) {
        GovernmentScheme copy = new GovernmentScheme();
        copy.setId(scheme.getId());
        copy.setSchemeName(translationService.translate(scheme.getSchemeName(), lang));
        copy.setDescription(translationService.translate(scheme.getDescription(), lang));
        copy.setCategory(translationService.translate(scheme.getCategory(), lang));
        copy.setState(translationService.translate(scheme.getState(), lang));
        copy.setEligibility(translationService.translate(scheme.getEligibility(), lang));
        copy.setIncomeLimit(scheme.getIncomeLimit());
        copy.setGender(scheme.getGender());
        copy.setMinimumAge(scheme.getMinimumAge());
        copy.setMaximumAge(scheme.getMaximumAge());
        copy.setEducation(translationService.translate(scheme.getEducation(), lang));
        copy.setBenefits(translationService.translate(scheme.getBenefits(), lang));
        copy.setRequiredDocuments(translationService.translate(scheme.getRequiredDocuments(), lang));
        copy.setOfficialWebsite(scheme.getOfficialWebsite());
        copy.setApplyLink(scheme.getApplyLink());
        copy.setActive(scheme.isActive());
        copy.setDeadline(scheme.getDeadline());
        copy.setCreatedAt(scheme.getCreatedAt());
        return copy;
    }
}
