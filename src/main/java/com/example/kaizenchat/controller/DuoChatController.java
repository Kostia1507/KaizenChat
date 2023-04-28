package com.example.kaizenchat.controller;

import com.example.kaizenchat.dto.Chat;
import com.example.kaizenchat.dto.OutgoingMessage;
import com.example.kaizenchat.entity.ChatEntity;
import com.example.kaizenchat.exception.ChatAlreadyExistsException;
import com.example.kaizenchat.exception.UserNotFoundException;
import com.example.kaizenchat.model.DuoChat;
import com.example.kaizenchat.security.jwt.UserDetailsImpl;
import com.example.kaizenchat.service.ChatService;
import com.example.kaizenchat.service.MessageService;
import com.example.kaizenchat.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static com.example.kaizenchat.dto.Action.*;
import static com.example.kaizenchat.model.ChatType.DUO;

@Slf4j
@RestController
@RequestMapping("/duo-chats")
public class DuoChatController {

    private final SimpMessagingTemplate template;
    private final ChatService chatService;
    private final MessageService messageService;
    private final UserService userService;

    @Autowired
    public DuoChatController(SimpMessagingTemplate template, ChatService chatService,
                             MessageService messageService, UserService userService) {
        this.template = template;
        this.chatService = chatService;
        this.messageService = messageService;
        this.userService = userService;
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllDuoChats(){
        var userDetails = (UserDetailsImpl) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Long userId = userDetails.getId();
        log.info("PrivateChatController ->  getAllDuoChats: user-id={}", userId);

        try {
            List<Chat> chats = chatService.getAllChats(userId, DUO);
            return ResponseEntity.ok(chats);
        } catch (UserNotFoundException e) {
            log.error("PrivateChatController ->  getAllDuoChats(): {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "user is not found"));
        }
    }

    @Transactional
    @MessageMapping("/start/{id}")
    public OutgoingMessage startChat(@DestinationVariable Long id, Authentication auth){
        var userDetails = (UserDetailsImpl) auth.getPrincipal();
        Long userId = userDetails.getId();
        log.info("DuoChatController ->  startChat(): user-id={} destination-id={}", userId, id);
        try{
            DuoChat chat = chatService.createDuoChat(userId, id);
            String nickname = userService.findUserById(userId).getNickname();
            var outgoingMessage = new OutgoingMessage(JOIN, userId, nickname, chat.getId(),
                    String.format("%s start a chat", nickname), ZonedDateTime.now());
            template.convertAndSend("/user/" + userId, outgoingMessage);
            return outgoingMessage;
        }catch(UserNotFoundException | ChatAlreadyExistsException e) {
            throw new RuntimeException(e);
        }
    }

}
