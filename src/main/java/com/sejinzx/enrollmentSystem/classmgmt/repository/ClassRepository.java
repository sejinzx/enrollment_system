package com.sejinzx.enrollmentSystem.classmgmt.repository;

import com.sejinzx.enrollmentSystem.classmgmt.entity.ClassEntity;
import com.sejinzx.enrollmentSystem.classmgmt.entity.ClassState;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ClassRepository extends JpaRepository<ClassEntity, Long> {

    Page<ClassEntity> findByClassDeletedFalse(Pageable pageable);
    Page<ClassEntity> findByClassStateAndClassDeletedFalse(ClassState tate, Pageable pageable);
    Optional<ClassEntity> findByClassSeqAndClassDeletedFalse(Long classSeq);
    Optional<ClassEntity>
    findByClassSeqAndUser_UserSeqAndClassDeletedFalse(
            Long classSeq,
            Long userSeq
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select c
            from ClassEntity c
            where c.classSeq = :classSeq
                    and c.classDeleted = false
        """)
    Optional<ClassEntity> findByIdWithLock(Long classSeq);

    List<ClassEntity> findByClassStateAndClassStartDateLessThanEqual(
            ClassState classState,
            LocalDateTime datetime
    );

    List<ClassEntity> findByClassStateAndClassEndDateLessThanEqual(
            ClassState classState,
            LocalDateTime datetime
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update ClassEntity c
                set c.classCurrApps = c.classCurrApps + 1
            where c.classSeq = :classSeq
                and c.classDeleted = false
                and c.classState = com.sejinzx.enrollmentSystem.classmgmt.entity.ClassState.OPEN
                and c.classCurrApps < c.classMaxCap
    """)
    int increaseCurrAppsIfAvailable(@Param("classSeq") Long classSeq);

}
