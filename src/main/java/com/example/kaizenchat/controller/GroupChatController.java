package com.example.kaizenchat.controller;

import com.example.kaizenchat.dto.*;
import com.example.kaizenchat.entity.ChatEntity;
import com.example.kaizenchat.entity.MessageEntity;
import com.example.kaizenchat.entity.UserEntity;
import com.example.kaizenchat.exception.*;
import com.example.kaizenchat.model.Avatar;
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
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

import static com.example.kaizenchat.dto.Action.*;
import static com.example.kaizenchat.model.ChatType.GROUP;
import static com.example.kaizenchat.utils.MultipartFileUtils.validate;
import static java.lang.String.format;
import static java.time.ZonedDateTime.now;
import static java.util.Map.of;
import static org.springframework.http.HttpStatus.OK;

@Slf4j
@RestController
@RequestMapping("/user/group-chats")
public class GroupChatController {

    private static final int GET_MESSAGES_LIMIT = 50;
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

    @GetMapping("/all")
    public ResponseEntity<?> getAllChats() {
        var userDetails = (UserDetailsImpl) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Long userId = userDetails.getId();
        log.info("GroupChatController ->  getAllChats(): user-id={}", userId);

        try {
            List<Chat> chats = chatService.getAllChats(userId, GROUP);
            return ResponseEntity.ok(chats);
        } catch (UserNotFoundException e) {
            log.error("GroupChatController ->  getAllChats(): {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "user is not found"));
        }
    }

    @PostMapping("/messages")
    public ResponseEntity<Map<String, Object>> getLastMessages(@Valid @RequestBody LastMessagesRequest request)
            throws ChatNotFoundException, UserNotFoundException, UserNotFoundInChatException {
        var userDetails = (UserDetailsImpl) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        Long userId = userDetails.getId();
        log.info("GroupChatController ->  getLastMessages(): user-id={}", userId);

        if(chatService.isMemberInGroupChat(
                userService.findUserById(userId), chatService.findChatById(request.getChatId(), GROUP))){
            List<MessageEntity> messages;
            if(request.getTime() == null)
                messages = messageService.getLastMessages(request.getChatId(), GET_MESSAGES_LIMIT);
            else
                messages = messageService.getLastMessages(request.getChatId(), request.getTime(), GET_MESSAGES_LIMIT);
            return ResponseEntity.ok().body(Map.of("messages", messages));
        }else
            throw new UserNotFoundInChatException("not member of chat");
    }

    @ResponseStatus(OK)
    @PostMapping("/{chatId}/upload-avatar")
    public Map<String, String> uploadAvatar(@RequestParam("avatar") MultipartFile file,
                                            @PathVariable Long chatId)
            throws UserViolationPermissionsException, UserNotFoundException,
            ChatNotFoundException, AvatarNotExistsException {

        log.info("IN GroupChatController -> uploadAvatar(): file-size={} bytes", file.getSize());

        validate(file);

        var userDetails = (UserDetailsImpl) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        chatService.uploadAvatar(file, chatId, userDetails.getId());
        return of("message", "updated");
    }

    @GetMapping("/{chatId}/avatar")
    public ResponseEntity<byte[]> downloadAvatar(@PathVariable Long chatId)
            throws ChatNotFoundException, AvatarNotExistsException {

        log.info("IN GroupChatController -> downloadAvatar(): chat-id={}", chatId);
        Avatar avatar = chatService.downloadAvatar(chatId);
        return ResponseEntity.ok()
                .contentType(avatar.contentType())
                .body(avatar.bytes());
    }

}