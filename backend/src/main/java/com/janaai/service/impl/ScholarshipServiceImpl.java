package com.janaai.service.impl;

import com.janaai.dto.scholarship.ScholarshipDTO;
import com.janaai.entity.Scholarship;
import com.janaai.exception.ResourceNotFoundException;
import com.janaai.repository.ScholarshipRepository;
import com.janaai.service.ScholarshipService;
import com.janaai.service.EmailService;
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

@lombok.extern.slf4j.Slf4j
@Service
@RequiredArgsConstructor
public class ScholarshipServiceImpl implements ScholarshipService {

    private final ScholarshipRepository scholarshipRepository;
    private final EmailService emailService;

    @Override
    @Transactional(readOnly = true)
    public Page<Scholarship> getScholarships(
            String search,
            String educationLevel,
            String category,
            Double income,
            String state,
            Pageable pageable
    ) {
        return scholarshipRepository.findAll((Specification<Scholarship>) (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.isBlank()) {
                String searchPattern = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), searchPattern),
                        cb.like(cb.lower(root.get("description")), searchPattern),
                        cb.like(cb.lower(root.get("category")), searchPattern)
                ));
            }

            if (educationLevel != null && !educationLevel.isBlank() && !"ALL".equalsIgnoreCase(educationLevel)) {
                predicates.add(cb.equal(root.get("educationLevel"), educationLevel));
            }

            if (category != null && !category.isBlank()) {
                predicates.add(cb.equal(root.get("category"), category));
            }

            if (income != null) {
                predicates.add(cb.or(
                        cb.isNull(root.get("incomeLimit")),
                        cb.greaterThanOrEqualTo(root.get("incomeLimit"), income)
                ));
            }

            if (state != null && !state.isBlank()) {
                predicates.add(cb.or(
                        cb.isNull(root.get("state")),
                        cb.equal(root.get("state"), "ALL"),
                        cb.equal(root.get("state"), state)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        }, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Scholarship getScholarshipById(String id) {
        return scholarshipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Scholarship not found with ID: " + id));
    }

    @Override
    @Transactional
    public Scholarship createScholarship(ScholarshipDTO dto) {
        Scholarship scholarship = Scholarship.builder()
                .name(dto.getName())
                .category(dto.getCategory())
                .educationLevel(dto.getEducationLevel())
                .incomeLimit(dto.getIncomeLimit())
                .state(dto.getState())
                .eligibility(dto.getEligibility())
                .amount(dto.getAmount())
                .deadline(dto.getDeadline())
                .description(dto.getDescription())
                .officialWebsite(dto.getOfficialWebsite())
                .applyLink(dto.getApplyLink())
                .build();
        Scholarship saved = scholarshipRepository.save(scholarship);
        try {
            emailService.sendNewScholarshipNotification(
                    saved.getName(),
                    saved.getDescription(),
                    saved.getEligibility(),
                    saved.getDeadline(),
                    saved.getApplyLink()
            );
        } catch (Exception e) {
            log.error("Failed to send new scholarship email notification: {}", e.getMessage());
        }
        return saved;
    }

    @Override
    @Transactional
    public Scholarship updateScholarship(String id, ScholarshipDTO dto) {
        Scholarship scholarship = getScholarshipById(id);
        scholarship.setName(dto.getName());
        scholarship.setCategory(dto.getCategory());
        scholarship.setEducationLevel(dto.getEducationLevel());
        scholarship.setIncomeLimit(dto.getIncomeLimit());
        scholarship.setState(dto.getState());
        scholarship.setEligibility(dto.getEligibility());
        scholarship.setAmount(dto.getAmount());
        scholarship.setDeadline(dto.getDeadline());
        scholarship.setDescription(dto.getDescription());
        scholarship.setOfficialWebsite(dto.getOfficialWebsite());
        scholarship.setApplyLink(dto.getApplyLink());
        return scholarshipRepository.save(scholarship);
    }

    @Override
    @Transactional
    public void deleteScholarship(String id) {
        Scholarship scholarship = getScholarshipById(id);
        scholarshipRepository.delete(scholarship);
    }
}
