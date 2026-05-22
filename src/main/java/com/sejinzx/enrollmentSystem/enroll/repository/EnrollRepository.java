package com.sejinzx.enrollmentSystem.enroll.repository;

import com.sejinzx.enrollmentSystem.enroll.entity.EnrollEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EnrollRepository extends JpaRepository<EnrollEntity, Long>  {

    Page<EnrollEntity> findByUser_UserSeq(Long userSeq, Pageable pageable);
    Optional<EnrollEntity> findByUser_UserSeqAndClassEntity_ClassSeq(Long userSeq, Long classSeq);

}
