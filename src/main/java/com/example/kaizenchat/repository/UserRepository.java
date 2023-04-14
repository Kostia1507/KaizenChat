package com.example.kaizenchat.repository;

import com.example.kaizenchat.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByPhoneNumber(String phoneNumber);

    Optional<UserEntity> findByRefreshToken(String refreshToken);
}