package com.janaai.service;

import com.janaai.dto.scheme.GovernmentSchemeDTO;
import com.janaai.entity.GovernmentScheme;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface GovernmentSchemeService {
    
    Page<GovernmentScheme> getSchemes(
            String search, 
            String category, 
            String state, 
            String gender, 
            Double income, 
            Integer age, 
            String education, 
            Pageable pageable
    );
    
    GovernmentScheme getSchemeById(String id);
    
    GovernmentScheme createScheme(GovernmentSchemeDTO dto);
    
    GovernmentScheme updateScheme(String id, GovernmentSchemeDTO dto);
    
    void deleteScheme(String id);
}
