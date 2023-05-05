package com.example.kaizenchat.service.impl;

import com.example.kaizenchat.entity.ChatEntity;
import com.example.kaizenchat.entity.MessageEntity;
import com.example.kaizenchat.entity.UserEntity;
import com.example.kaizenchat.exception.*;
import com.example.kaizenchat.repository.ChatRepository;
import com.example.kaizenchat.repository.MessageRepository;
import com.example.kaizenchat.repository.UserRepository;
import com.example.kaizenchat.service.ChatService;
import com.example.kaizenchat.service.MessageService;
import com.example.kaizenchat.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static java.lang.String.format;

@Slf4j
@Service
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;
    private final UserService userService;
    private final ChatService chatService;

    @Autowired
    public MessageServiceImpl(MessageRepository messageRepository, ChatRepository chatRepository,
                              UserService userService, @Lazy ChatService chatService){
        this.messageRepository = messageRepository;
        this.chatRepository = chatRepository;
        this.userService = userService;
        this.chatService = chatService;
    }

    public Optional<MessageEntity> findMessageById(Long id){
        return messageRepository.findById(id);
    }

    public void createNewMessage(Long chatId, Long senderId, String body)
            throws UserNotFoundException, ChatNotFoundException, UserNotFoundInChatException {
        UserEntity user = userService.findUserById(senderId);
        ChatEntity chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ChatNotFoundException(format("chat:%d was not found", chatId)));

        Set<UserEntity> users = chat.getUsers();
        if(users.contains(user)){
            messageRepository.save(MessageEntity.builder()
                    .chat(chat)
                    .sender(user)
                    .body(body)
                    .isPinned(false)
                    .time(ZonedDateTime.now())
                    .likes(0).build()
            );
        }else
            throw new UserNotFoundInChatException();
    }

    public void editMessage(Long senderId, Long messageId, String body)
            throws UserNotFoundException, MessageNotFoundException, UserViolationPermissionsException {

        UserEntity user = userService.findUserById(senderId);
        MessageEntity message = messageRepository.findById(messageId).orElseThrow(MessageNotFoundException::new);

        if(message.getSender().equals(user)){
            message.setBody(body);
            messageRepository.save(message);
        }else throw new UserViolationPermissionsException();
    }

    public void deleteMessageById(Long messageId, Long userId)
            throws MessageNotFoundException, UserViolationPermissionsException {
        MessageEntity message = messageRepository.findById(messageId).orElseThrow(MessageNotFoundException::new);
        if(Objects.equals(message.getSender().getId(), userId)
                || chatService.isUserAdminInGroupChat(message.getSender(), message.getChat())){
            messageRepository.delete(message);
        }else throw new UserViolationPermissionsException();
    }

    public List<MessageEntity> getLastMessages(Long chatId, ZonedDateTime time, int limit){
        return messageRepository.findLastNMessagesBefore(chatId, time, limit);
    }

    public List<MessageEntity> getLastMessages(Long chatId, int limit){
        return messageRepository.findLastNMessagesBefore(chatId, ZonedDateTime.now(), limit);
    }
}
