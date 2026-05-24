package com.sejinzx.enrollmentSystem.user.repository;

import com.sejinzx.enrollmentSystem.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByUserIdAndUserDeletedFalse(String userID);
    boolean existsByUserIdAndUserDeletedFalse(String userId);

}
