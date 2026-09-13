package com.janaai.repository;

import com.janaai.entity.GovernmentScheme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface GovernmentSchemeRepository extends JpaRepository<GovernmentScheme, String>, JpaSpecificationExecutor<GovernmentScheme> {
}
