package com.example.kaizenchat.service;

import com.example.kaizenchat.entity.MessageEntity;
import com.example.kaizenchat.exception.ChatNotFoundException;
import com.example.kaizenchat.exception.UserNotFoundException;
import com.example.kaizenchat.exception.UserNotFoundInChatException;

import java.util.Optional;

public interface MessageService {

    Optional<MessageEntity> findMessageById(Long id);

    void createNewMessage(Long chatId, Long senderId, String body)
            throws UserNotFoundException, ChatNotFoundException, UserNotFoundInChatException;
}
