package com.janaai.service;

import com.janaai.dto.scholarship.ScholarshipDTO;
import com.janaai.entity.Scholarship;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ScholarshipService {
    
    Page<Scholarship> getScholarships(
            String search, 
            String educationLevel, 
            String category, 
            Double income, 
            String state, 
            Pageable pageable
    );
    
    Scholarship getScholarshipById(String id);
    
    Scholarship createScholarship(ScholarshipDTO dto);
    
    Scholarship updateScholarship(String id, ScholarshipDTO dto);
    
    void deleteScholarship(String id);
}
