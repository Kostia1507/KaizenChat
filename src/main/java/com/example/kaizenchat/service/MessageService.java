package com.example.kaizenchat.service;

import com.example.kaizenchat.entity.MessageEntity;
import com.example.kaizenchat.exception.*;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface MessageService {

    Optional<MessageEntity> findMessageById(Long id);

    void createNewMessage(Long chatId, Long senderId, String body)
            throws UserNotFoundException, ChatNotFoundException, UserNotFoundInChatException;

    void editMessage(Long senderId, Long messageId, String body)
            throws UserNotFoundException, MessageNotFoundException, UserViolationPermissionsException;

    public void deleteMessageById(Long messageId, Long userId)
            throws MessageNotFoundException, UserViolationPermissionsException;
    List<MessageEntity> getLastMessages(Long chatId, ZonedDateTime time, int limit);

    List<MessageEntity> getLastMessages(Long chatId, int limit);
}
