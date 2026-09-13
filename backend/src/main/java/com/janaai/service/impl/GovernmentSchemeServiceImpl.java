package com.janaai.service.impl;

import com.janaai.dto.scheme.GovernmentSchemeDTO;
import com.janaai.entity.GovernmentScheme;
import com.janaai.exception.ResourceNotFoundException;
import com.janaai.repository.GovernmentSchemeRepository;
import com.janaai.service.GovernmentSchemeService;
import com.janaai.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GovernmentSchemeServiceImpl implements GovernmentSchemeService {

    private final GovernmentSchemeRepository schemeRepository;
    private final EmailService emailService;

    @Override
    @Transactional(readOnly = true)
    public Page<GovernmentScheme> getSchemes(
            String search,
            String category,
            String state,
            String gender,
            Double income,
            Integer age,
            String education,
            Pageable pageable
    ) {
        return schemeRepository.findAll((Specification<GovernmentScheme>) (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.isBlank()) {
                String searchPattern = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("schemeName")), searchPattern),
                        cb.like(cb.lower(root.get("description")), searchPattern),
                        cb.like(cb.lower(root.get("category")), searchPattern)
                ));
            }

            if (category != null && !category.isBlank()) {
                predicates.add(cb.equal(root.get("category"), category));
            }

            if (state != null && !state.isBlank()) {
                predicates.add(cb.or(
                        cb.isNull(root.get("state")),
                        cb.equal(root.get("state"), "ALL"),
                        cb.equal(root.get("state"), state)
                ));
            }

            if (gender != null && !gender.isBlank() && !"ALL".equalsIgnoreCase(gender)) {
                predicates.add(cb.or(
                        cb.equal(root.get("gender"), "ALL"),
                        cb.equal(root.get("gender"), gender)
                ));
            }

            if (income != null) {
                predicates.add(cb.or(
                        cb.isNull(root.get("incomeLimit")),
                        cb.greaterThanOrEqualTo(root.get("incomeLimit"), income)
                ));
            }

            if (age != null) {
                Predicate minAgePredicate = cb.or(cb.isNull(root.get("minimumAge")), cb.lessThanOrEqualTo(root.get("minimumAge"), age));
                Predicate maxAgePredicate = cb.or(cb.isNull(root.get("maximumAge")), cb.greaterThanOrEqualTo(root.get("maximumAge"), age));
                predicates.add(cb.and(minAgePredicate, maxAgePredicate));
            }

            if (education != null && !education.isBlank()) {
                predicates.add(cb.or(
                        cb.isNull(root.get("education")),
                        cb.equal(root.get("education"), "ALL"),
                        cb.like(cb.lower(root.get("education")), "%" + education.toLowerCase() + "%")
                ));
            }

            predicates.add(cb.equal(root.get("active"), true));

            return cb.and(predicates.toArray(new Predicate[0]));
        }, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public GovernmentScheme getSchemeById(String id) {
        return schemeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Government Scheme not found with ID: " + id));
    }

    @Override
    @Transactional
    public GovernmentScheme createScheme(GovernmentSchemeDTO dto) {
        GovernmentScheme scheme = GovernmentScheme.builder()
                .schemeName(dto.getSchemeName())
                .category(dto.getCategory())
                .description(dto.getDescription())
                .state(dto.getState())
                .eligibility(dto.getEligibility())
                .incomeLimit(dto.getIncomeLimit())
                .gender(dto.getGender() != null ? dto.getGender() : "ALL")
                .minimumAge(dto.getMinimumAge())
                .maximumAge(dto.getMaximumAge())
                .education(dto.getEducation())
                .benefits(dto.getBenefits())
                .requiredDocuments(dto.getRequiredDocuments())
                .officialWebsite(dto.getOfficialWebsite())
                .applyLink(dto.getApplyLink())
                .deadline(dto.getDeadline())
                .active(true)
                .build();
        GovernmentScheme saved = schemeRepository.save(scheme);
        try {
            emailService.sendNewSchemeNotification(
                    saved.getSchemeName(),
                    saved.getDescription(),
                    saved.getEligibility(),
                    saved.getDeadline(),
                    saved.getApplyLink()
            );
        } catch (Exception e) {
            log.error("Failed to send new scheme email notification: {}", e.getMessage());
        }
        return saved;
    }

    @Override
    @Transactional
    public GovernmentScheme updateScheme(String id, GovernmentSchemeDTO dto) {
        GovernmentScheme scheme = getSchemeById(id);
        scheme.setSchemeName(dto.getSchemeName());
        scheme.setCategory(dto.getCategory());
        scheme.setDescription(dto.getDescription());
        scheme.setState(dto.getState());
        scheme.setEligibility(dto.getEligibility());
        scheme.setIncomeLimit(dto.getIncomeLimit());
        scheme.setGender(dto.getGender() != null ? dto.getGender() : "ALL");
        scheme.setMinimumAge(dto.getMinimumAge());
        scheme.setMaximumAge(dto.getMaximumAge());
        scheme.setEducation(dto.getEducation());
        scheme.setBenefits(dto.getBenefits());
        scheme.setRequiredDocuments(dto.getRequiredDocuments());
        scheme.setOfficialWebsite(dto.getOfficialWebsite());
        scheme.setApplyLink(dto.getApplyLink());
        scheme.setDeadline(dto.getDeadline());
        scheme.setActive(dto.isActive());
        return schemeRepository.save(scheme);
    }

    @Override
    @Transactional
    public void deleteScheme(String id) {
        GovernmentScheme scheme = getSchemeById(id);
        schemeRepository.delete(scheme);
    }
}
