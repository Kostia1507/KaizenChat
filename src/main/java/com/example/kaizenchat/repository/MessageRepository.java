package com.example.kaizenchat.repository;

import com.example.kaizenchat.entity.MessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<MessageEntity, Long> {

    @Override
    Optional<MessageEntity> findById(@NonNull Long messageId);

}
