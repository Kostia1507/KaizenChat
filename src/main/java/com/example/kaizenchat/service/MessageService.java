package com.example.kaizenchat.service;

import com.example.kaizenchat.entity.MessageEntity;

import java.util.Optional;

public interface MessageService {

    Optional<MessageEntity> findMessageById(Long id);
}
