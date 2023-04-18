package com.example.kaizenchat.service.impl;

import com.example.kaizenchat.entity.ChatEntity;
import com.example.kaizenchat.entity.MessageEntity;
import com.example.kaizenchat.entity.UserEntity;
import com.example.kaizenchat.exception.ChatNotFoundException;
import com.example.kaizenchat.exception.UserNotFoundException;
import com.example.kaizenchat.exception.UserNotFoundInChatException;
import com.example.kaizenchat.repository.ChatRepository;
import com.example.kaizenchat.repository.MessageRepository;
import com.example.kaizenchat.repository.UserRepository;
import com.example.kaizenchat.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
public class MessageServiceImpl implements MessageService {

    MessageRepository messageRepository;
    ChatRepository chatRepository;
    UserRepository userRepository;

    @Autowired
    public MessageServiceImpl(MessageRepository messageRepository, ChatRepository chatRepository,
                              UserRepository userRepository){
        this.messageRepository = messageRepository;
        this.chatRepository = chatRepository;
        this.userRepository = userRepository;
    }

    public Optional<MessageEntity> findMessageById(Long id){
        return messageRepository.findById(id);
    }

    public void createNewMessage(Long chatId, Long senderId, String body)
            throws UserNotFoundException, ChatNotFoundException, UserNotFoundInChatException {
        UserEntity user = userRepository.findById(senderId).orElseThrow(UserNotFoundException::new);
        ChatEntity chat = chatRepository.findById(chatId).orElseThrow(ChatNotFoundException::new);

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

}
