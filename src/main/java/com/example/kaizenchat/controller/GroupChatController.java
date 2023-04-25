package com.example.kaizenchat.controller;

import com.example.kaizenchat.dto.AddMemberToChatRequest;
import com.example.kaizenchat.dto.IncomingMessage;
import com.example.kaizenchat.dto.OutgoingMessage;
import com.example.kaizenchat.entity.ChatEntity;
import com.example.kaizenchat.entity.UserEntity;
import com.example.kaizenchat.exception.ChatNotFoundException;
import com.example.kaizenchat.exception.UserNotFoundException;
import com.example.kaizenchat.exception.UserNotFoundInChatException;
import com.example.kaizenchat.exception.UserViolationPermissionsException;
import com.example.kaizenchat.security.jwt.UserDetailsImpl;
import com.example.kaizenchat.service.ChatService;
import com.example.kaizenchat.service.MessageService;
import com.example.kaizenchat.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.example.kaizenchat.dto.Action.*;
import static com.example.kaizenchat.model.ChatType.GROUP;
import static java.lang.String.format;
import static java.time.ZonedDateTime.now;

@Slf4j
@RestController
@RequestMapping("/group-chat")
public class GroupChatController {

    private final SimpMessagingTemplate template;
    private final ChatService chatService;
    private final MessageService messageService;
    private final UserService userService;

    @Autowired
    public GroupChatController(SimpMessagingTemplate template, ChatService chatService,
                               MessageService messageService, UserService userService) {
        this.template = template;
        this.chatService = chatService;
        this.messageService = messageService;
        this.userService = userService;
    }


    @Transactional
    @MessageMapping("/join")
    public OutgoingMessage joinIntoChat(AddMemberToChatRequest request, Authentication auth) {
        var userDetails = (UserDetailsImpl) auth.getPrincipal();
        log.info("GroupChatController ->  joinIntoChat(): user-id={} chat-id={}", userDetails.getId(), request.getChatId());
        try {
            Long userId = userDetails.getId();
            UserEntity user = userService.findUserById(userId);
            boolean isAdded = chatService.addUserToGroupChat(request, userId);

            if (!isAdded) {
                throw new UserViolationPermissionsException();
            }

            String nickname = user.getNickname();
            var outgoingMessage = new OutgoingMessage(
                    JOIN, userId, nickname, request.getChatId(),
                    format("%s joined to the chat", nickname),
                    now()
            );
            template.convertAndSend("/chatroom/" + request.getChatId(), outgoingMessage);
            return outgoingMessage;
        } catch (ChatNotFoundException | UserNotFoundException | UserViolationPermissionsException e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    @MessageMapping("/quit/{id}")
    public OutgoingMessage quitFromChat(@DestinationVariable long id, Authentication auth) {
        var userDetails = (UserDetailsImpl) auth.getPrincipal();
        Long userId = userDetails.getId();

        log.info("GroupChatController ->  quitFromChat(): user-id={} chat-id={}", userId, id);

        try {
            ChatEntity chat = chatService.findChatById(id, GROUP);
            UserEntity user = userService.findUserById(userId);

            // if not member of chat
            if (!chatService.isMemberInGroupChat(user, chat)) {
                throw new UserNotFoundInChatException();
            }

            String nickname = user.getNickname();
            chatService.deleteUser(chat, user);

            var outgoingMessage = new OutgoingMessage(QUIT, userId, nickname, id, format("%s left the chat", nickname), now());
            template.convertAndSend("/chatroom/" + id, outgoingMessage);
            return outgoingMessage;

        } catch (ChatNotFoundException | UserNotFoundException | UserNotFoundInChatException e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    @MessageMapping("/send")
    public OutgoingMessage receiveMessage(@Payload IncomingMessage message, Authentication auth) {
        var userDetails = (UserDetailsImpl) auth.getPrincipal();
        Long chatId = message.getChatId();
        Long userId = userDetails.getId();

        log.info("GroupChatController ->  receiveMessage(): {}", message);

        try {
            UserEntity user = userService.findUserById(userId);
            messageService.createNewMessage(chatId, userId, message.getBody());

            var outgoingMessage = new OutgoingMessage(SEND, userId, user.getNickname(), chatId, message.getBody(), now());
            template.convertAndSend("/chatroom/" + chatId, outgoingMessage); // /chatroom/{chat-id}
            return outgoingMessage;

        } catch (UserNotFoundException | ChatNotFoundException | UserNotFoundInChatException e) {
            throw new RuntimeException(e);
        }
    }

    @MessageExceptionHandler(RuntimeException.class)
    public void handleExceptions(RuntimeException e) {
        log.error("EXCEPTION: {}", e.getMessage());
    }

}