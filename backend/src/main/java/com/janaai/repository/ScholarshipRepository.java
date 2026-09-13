package com.janaai.repository;

import com.janaai.entity.Scholarship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ScholarshipRepository extends JpaRepository<Scholarship, String>, JpaSpecificationExecutor<Scholarship> {
}
