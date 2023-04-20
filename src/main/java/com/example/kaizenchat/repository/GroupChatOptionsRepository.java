package com.example.kaizenchat.repository;

import com.example.kaizenchat.entity.GroupChatOptionsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupChatOptionsRepository extends JpaRepository<GroupChatOptionsEntity, Long> {
}