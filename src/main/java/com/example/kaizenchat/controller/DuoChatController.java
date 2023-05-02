package com.example.kaizenchat.controller;

import com.example.kaizenchat.dto.*;
import com.example.kaizenchat.entity.ChatEntity;
import com.example.kaizenchat.entity.MessageEntity;
import com.example.kaizenchat.entity.UserEntity;
import com.example.kaizenchat.exception.*;
import com.example.kaizenchat.model.DuoChat;
import com.example.kaizenchat.security.jwt.UserDetailsImpl;
import com.example.kaizenchat.service.ChatService;
import com.example.kaizenchat.service.MessageService;
import com.example.kaizenchat.service.UserService;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.*;

import static com.example.kaizenchat.dto.Action.*;
import static com.example.kaizenchat.model.ChatType.DUO;
import static java.time.ZonedDateTime.now;

@Slf4j
@RestController
@RequestMapping("/user/duo-chats")
public class DuoChatController {

    private final int GET_MESSAGES_LIMIT = 50;
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
            template.convertAndSend("/user/"+id+"/start", message);
            return ResponseEntity.ok().body(Map.of("chatId", chatId));
        } catch (UserNotFoundException | ChatAlreadyExistsException e) {
            log.error("DuoChatController ->  startChat: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Something wrong"));
        }
    }

    @PostMapping("/messages")
    public ResponseEntity<Map<String, Object>> getLastMessages(@Valid @RequestBody LastMessagesRequest request) throws ChatNotFoundException, UserNotFoundInChatException {
        var userDetails = (UserDetailsImpl) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        Long userId = userDetails.getId();
        log.info("DuoChatController ->  getLastMessages(): user-id={}", userId);
        Set<UserEntity> users = chatService.findChatById(request.getChatId(), DUO).getUsers();
        if(users.stream().anyMatch(member -> member.getId().equals(userId))){
            List<MessageEntity> messages;
            if(request.getTime() == null)
                messages = messageService.getLastMessages(request.getChatId(), GET_MESSAGES_LIMIT);
            else
                messages = messageService.getLastMessages(request.getChatId(), request.getTime(), GET_MESSAGES_LIMIT);
            return ResponseEntity.ok().body(Map.of("messages", messages));
        }else
            throw new UserNotFoundInChatException("not member of chat");
    }

    @Transactional
    @MessageMapping("/duo-chat/send")
    public void sendMessage(@Payload IncomingMessage message, Authentication auth){
        var userDetails = (UserDetailsImpl) auth.getPrincipal();
        Long userId = userDetails.getId();
        log.info("DuoChatController ->  sendMessage(): user-id={} chat-id={}", userId, message.getChatId());
        try {
            UserEntity user = userService.findUserById(userId);
            messageService.createNewMessage(message.getChatId(), userId, message.getBody());
            var outgoingMessage = new OutgoingMessage(SEND, userId, user.getNickname(), message.getChatId(), message.getBody(), now());
            template.convertAndSend("/duo-chat/" + message.getChatId(), outgoingMessage);
        } catch (UserNotFoundException | ChatNotFoundException | UserNotFoundInChatException e) {
            log.error("DuoChatController ->  sendMessage: {}", e.getMessage());
        }
    }

    @Transactional
    @MessageMapping("/duo-chat/edit}")
    public void editMessage(@Payload EditMessageRequest request, Authentication auth)
            throws UserNotFoundException, MessageNotFoundException, UserViolationPermissionsException {
        var userDetails = (UserDetailsImpl) auth.getPrincipal();
        Long userId = userDetails.getId();
        log.info("DuoChatController ->  editMessage(): user-id={}", userId);
        MessageEntity message = messageService.findMessageById(request.getMessageId())
                .orElseThrow(MessageNotFoundException::new);
        messageService.editMessage(userId, request.getMessageId(), request.getBody());
        OutgoingMessage outgoingMessage = OutgoingMessage.builder()
                .action(EDIT)
                .chatId(message.getChat().getId())
                .body(request.getBody())
                .messageId(message.getId())
                .build();
        template.convertAndSend("/duo-chat/" + message.getChat().getId(), outgoingMessage);
    }

    @Transactional
    @MessageMapping("/duo-chat/delete/{messageId}")
    public void deleteMessage(@DestinationVariable Long messageId, Authentication auth)
            throws MessageNotFoundException, UserViolationPermissionsException {
        var userDetails = (UserDetailsImpl) auth.getPrincipal();
        Long userId = userDetails.getId();
        Long chatId = messageService.findMessageById(messageId).orElseThrow(MessageNotFoundException::new).getChat().getId();
        messageService.deleteMessageById(messageId, userId);
        OutgoingMessage outgoingMessage = OutgoingMessage.builder()
                .action(DELETE)
                .chatId(chatId)
                .messageId(messageId)
                .build();
        template.convertAndSend("/duo-chat/" + chatId, outgoingMessage);
    }


}
