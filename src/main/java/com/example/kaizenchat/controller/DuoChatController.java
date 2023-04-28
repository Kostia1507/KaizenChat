package com.example.kaizenchat.controller;

import com.example.kaizenchat.dto.Chat;
import com.example.kaizenchat.dto.IncomingMessage;
import com.example.kaizenchat.dto.OutgoingMessage;
import com.example.kaizenchat.entity.ChatEntity;
import com.example.kaizenchat.entity.UserEntity;
import com.example.kaizenchat.exception.ChatAlreadyExistsException;
import com.example.kaizenchat.exception.ChatNotFoundException;
import com.example.kaizenchat.exception.UserNotFoundException;
import com.example.kaizenchat.exception.UserNotFoundInChatException;
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
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.example.kaizenchat.dto.Action.*;
import static com.example.kaizenchat.model.ChatType.DUO;
import static java.time.ZonedDateTime.now;

@Slf4j
@RestController
@RequestMapping("/user/duo-chats")
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
    public ResponseEntity<?> getAllDuoChats() {
        var userDetails = (UserDetailsImpl) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Long userId = userDetails.getId();
        log.info("DuoChatController ->  getAllDuoChats: user-id={}", userId);

        try {
            List<Chat> chats = chatService.getAllChats(userId, DUO);
            return ResponseEntity.ok(chats);
        } catch (UserNotFoundException e) {
            log.error("DuoChatController ->  getAllDuoChats(): {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "user is not found"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDuoChatById(@PathVariable Long id) {
        var userDetails = (UserDetailsImpl) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Long userId = userDetails.getId();
        log.info("DuoChatController ->  getDuoChatById: user-id={}", userId);

        try {
            ChatEntity chat = chatService.findChatById(id, DUO);
            return ResponseEntity.ok().body(chat);
        } catch (ChatNotFoundException e) {
            log.error("DuoChatController ->  getDuoChatById: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "chat not found"));
        }
    }

    @GetMapping("/with/{id}")
    public ResponseEntity<?> getDuoChatWith(@PathVariable Long id) {
        var userDetails = (UserDetailsImpl) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Long userId = userDetails.getId();
        log.info("DuoChatController ->  getDuoChatWith: user-id={} with={}", userId, id);
        try {
            ChatEntity chat = chatService.findChatByUsers(userId, id);
            return ResponseEntity.ok(chat);
        } catch (UserNotFoundException | ChatNotFoundException e) {
            log.error("DuoChatController ->  getDuoChatWith: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/start/{id}")
    public ResponseEntity<Map<String, Object>> startChat(@PathVariable Long id) {
        var userDetails = (UserDetailsImpl) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Long userId = userDetails.getId();
        log.info("DuoChatController ->  startChat(): user-id={} destination-id={}", userId, id);
        try {
            Long chatId = chatService.createDuoChat(userId, id).getId();
            OutgoingMessage message = OutgoingMessage.builder().action(JOIN).chatId(chatId).build();
            template.convertAndSend("/user/"+id, message);
            return ResponseEntity.ok().body(Map.of("message", chatId));
        } catch (UserNotFoundException | ChatAlreadyExistsException e) {
            log.error("DuoChatController ->  startChat: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Something wrong"));
        }
    }

    @Transactional
    @MessageMapping("/duo-chat/send/{chatId}")
    public void sendMessage(@DestinationVariable Long chatId, @Payload IncomingMessage message, Authentication auth){
        var userDetails = (UserDetailsImpl) auth.getPrincipal();
        Long userId = userDetails.getId();
        log.info("DuoChatController ->  sendMessage(): user-id={} chat-id={}", userId, chatId);
        try {
            UserEntity user = userService.findUserById(userId);
            messageService.createNewMessage(chatId, userId, message.getBody());
            var outgoingMessage = new OutgoingMessage(SEND, userId, user.getNickname(), chatId, message.getBody(), now());
            template.convertAndSend("/duo-chat/" + chatId, outgoingMessage);
        } catch (UserNotFoundException | ChatNotFoundException | UserNotFoundInChatException e) {
            log.error("DuoChatController ->  sendMessage: {}", e.getMessage());
        }
    }


}
