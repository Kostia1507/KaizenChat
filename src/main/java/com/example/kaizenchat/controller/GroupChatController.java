package com.example.kaizenchat.controller;

import com.example.kaizenchat.dto.*;
import com.example.kaizenchat.entity.ChatEntity;
import com.example.kaizenchat.entity.MessageEntity;
import com.example.kaizenchat.entity.UserEntity;
import com.example.kaizenchat.exception.*;
import com.example.kaizenchat.model.GroupChat;
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

import java.util.List;
import java.util.Map;

import static com.example.kaizenchat.dto.Action.*;
import static com.example.kaizenchat.model.ChatType.GROUP;
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
    @MessageMapping("/group-chats/join")
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
    @MessageMapping("/group-chats/quit/{id}")
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
    @MessageMapping("/group-chats/send")
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

    @Transactional
    @MessageMapping("/group-chats/edit")
    public void editMessage(@Payload EditMessageRequest request, Authentication auth)
            throws UserNotFoundException, UserViolationPermissionsException {
        var userDetails = (UserDetailsImpl) auth.getPrincipal();
        Long userId = userDetails.getId();
        log.info("GroupChatController ->  editMessage(): user-id={}", userId);
        try {
            MessageEntity message = messageService.findMessageById(request.getMessageId())
                    .orElseThrow(MessageNotFoundException::new);
            messageService.editMessage(userId, request.getMessageId(), request.getBody());
            OutgoingMessage outgoingMessage = OutgoingMessage.builder()
                    .action(EDIT)
                    .chatId(message.getChat().getId())
                    .body(request.getBody())
                    .messageId(message.getId())
                    .build();
            template.convertAndSend("/chatroom/" + message.getChat().getId(), outgoingMessage);
        }catch(MessageNotFoundException e){
            log.error("GroupChatController ->  editMessage(): {}", e.getMessage());
        }
    }

    @Transactional
    @MessageMapping("/group-chats/delete/{messageId}")
    public void deleteMessage(@DestinationVariable Long messageId, Authentication auth) {
        var userDetails = (UserDetailsImpl) auth.getPrincipal();
        Long userId = userDetails.getId();
        try {
            MessageEntity message = messageService.findMessageById(messageId).orElseThrow(MessageNotFoundException::new);
            UserEntity user = userService.findUserById(userId);
            if(userId.equals(message.getSender().getId())||chatService.isUserAdminInGroupChat(user,message.getChat())){
                messageService.deleteMessageById(messageId, userId);
                OutgoingMessage outgoingMessage = OutgoingMessage.builder()
                        .action(DELETE)
                        .chatId(message.getChat().getId())
                        .messageId(messageId)
                        .build();
                template.convertAndSend("/chatroom/" + message.getChat().getId(), outgoingMessage);
            }
        } catch (MessageNotFoundException | UserNotFoundException | UserViolationPermissionsException e) {
            log.error("GroupChatController ->  deleteMessage(): {}", e.getMessage());
        }
    }

    @Transactional
    @MessageMapping("/group-chats/kick")
    public void kickMember(@Payload KickMemberRequest request, Authentication auth){
        var userDetails = (UserDetailsImpl) auth.getPrincipal();
        Long userId = userDetails.getId();
        try {
            UserEntity admin = userService.findUserById(userId);
            UserEntity user = userService.findUserById(request.getUserId());
            ChatEntity chat = chatService.findChatById(request.getChatId(), GROUP);
            chatService.kickFromGroupChat(admin, chat, user);
            OutgoingMessage outgoingMessage = OutgoingMessage.builder()
                    .action(QUIT)
                    .chatId(request.getChatId())
                    .senderId(request.getUserId())
                    .body("user was kicked by "+ admin.getNickname())
                    .build();
            template.convertAndSend("/chatroom/" + request.getChatId(), outgoingMessage);
        }catch (UserNotFoundException | UserViolationPermissionsException | ChatNotFoundException e) {
            log.error("GroupChatController ->  deleteMessage(): {}", e.getMessage());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllChats() {
        var userDetails = getUserDetails();

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

    @ResponseStatus(OK)
    @PostMapping("/new")
    public Map<String, Object> createChat(@Valid @RequestBody GroupChatCreationRequest request)
            throws InvalidRequestDataException, UserNotFoundException {

        var userDetails = getUserDetails();
        Long userId = userDetails.getId();
        log.info("GroupChatController ->  createChat(): user-id={}", userId);

        if (request.isPrivacyMode() && request.getPassword() == null) {
            throw new InvalidRequestDataException("password is not defined");
        }

        GroupChat chat = chatService.createGroupChat(request, userId);
        return chat.map();
    }

    @PostMapping("/messages")
    public ResponseEntity<Map<String, Object>> getLastMessages(@Valid @RequestBody LastMessagesRequest request)
            throws ChatNotFoundException, UserNotFoundException, UserNotFoundInChatException {
        var userDetails = getUserDetails();
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
    public Map<String, String> uploadAvatar(@PathVariable Long chatId, @Valid @RequestBody AvatarDTO avatar)
            throws UserViolationPermissionsException, UserNotFoundException,
            ChatNotFoundException, AvatarNotExistsException {

        log.info("IN GroupChatController -> uploadAvatar(): chatId={}", chatId);

        var userDetails = getUserDetails();
        chatService.uploadAvatar(avatar.getEncodedContent(), chatId, userDetails.getId());
        return of("message", "updated");
    }

    @ResponseStatus(OK)
    @GetMapping("/{chatId}/avatar")
    public AvatarDTO downloadAvatar(@PathVariable Long chatId)
            throws ChatNotFoundException, AvatarNotExistsException {

        log.info("IN GroupChatController -> downloadAvatar(): chat-id={}", chatId);
        return chatService.downloadAvatar(chatId);
    }

    public UserDetailsImpl getUserDetails() {
        return (UserDetailsImpl) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
    }

}