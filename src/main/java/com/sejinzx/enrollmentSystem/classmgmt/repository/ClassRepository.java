package com.sejinzx.enrollmentSystem.classmgmt.repository;

import com.sejinzx.enrollmentSystem.classmgmt.entity.ClassEntity;
import com.sejinzx.enrollmentSystem.classmgmt.entity.ClassState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ClassRepository extends JpaRepository<ClassEntity, Long> {

    Page<ClassEntity> findByClassDeletedFalse(Pageable pageable);
    Page<ClassEntity> findByClassStateAndClassDeletedFalse(ClassState tate, Pageable pageable);
    Optional<ClassEntity> findByClassSeqAndClassDeletedFalse(Long classSeq);

}
