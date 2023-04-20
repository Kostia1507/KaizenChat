package com.example.kaizenchat.repository;

import com.example.kaizenchat.entity.MessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<MessageEntity, Long> {

    @Query(value = "SELECT * FROM message WHERE chat_id = :chatId and \"time\" < :messageTime" +
            " ORDER BY \"time\" DESC LIMIT :limit", nativeQuery = true)
    List<MessageEntity> findLastNMessagesBefore(Long chatId, ZonedDateTime messageTime, int limit);
}
