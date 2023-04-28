package com.example.kaizenchat.controller;

import com.example.kaizenchat.dto.Chat;
import com.example.kaizenchat.entity.ChatEntity;
import com.example.kaizenchat.exception.UserNotFoundException;
import com.example.kaizenchat.security.jwt.UserDetailsImpl;
import com.example.kaizenchat.service.ChatService;
import com.example.kaizenchat.service.MessageService;
import com.example.kaizenchat.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

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

}
