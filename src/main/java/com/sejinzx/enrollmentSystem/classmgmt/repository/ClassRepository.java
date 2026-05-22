package com.sejinzx.enrollmentSystem.classmgmt.repository;

import com.sejinzx.enrollmentSystem.classmgmt.entity.ClassEntity;
import com.sejinzx.enrollmentSystem.classmgmt.entity.ClassState;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ClassRepository extends JpaRepository<ClassEntity, Long> {

    Page<ClassEntity> findByClassDeletedFalse(Pageable pageable);
    Page<ClassEntity> findByClassStateAndClassDeletedFalse(ClassState tate, Pageable pageable);
    Optional<ClassEntity> findByClassSeqAndClassDeletedFalse(Long classSeq);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select c
            from ClassEntity c
            where c.classSeq = :classSeq
                    and c.classDeleted = false
        """)
    Optional<ClassEntity> findByIdWithLock(Long classSeq);

}
