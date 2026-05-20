package com.sejinzx.enrollmentSystem.user.repository;

import com.sejinzx.enrollmentSystem.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, String> {

}
