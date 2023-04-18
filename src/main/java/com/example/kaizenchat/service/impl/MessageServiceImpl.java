package com.example.kaizenchat.service.impl;

import com.example.kaizenchat.entity.MessageEntity;
import com.example.kaizenchat.repository.MessageRepository;
import com.example.kaizenchat.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class MessageServiceImpl implements MessageService {

    MessageRepository messageRepository;

    @Autowired
    public MessageServiceImpl(MessageRepository messageRepository){
        this.messageRepository = messageRepository;
    }

    public Optional<MessageEntity> findMessageById(Long id){
        return messageRepository.findById(id);
    }

}
