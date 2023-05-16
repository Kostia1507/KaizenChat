package com.example.kaizenchat.service.impl;

import com.example.kaizenchat.dto.*;
import com.example.kaizenchat.entity.ChatEntity;
import com.example.kaizenchat.entity.GroupChatOptionsEntity;
import com.example.kaizenchat.entity.MessageEntity;
import com.example.kaizenchat.entity.UserEntity;
import com.example.kaizenchat.exception.*;
import com.example.kaizenchat.model.Avatar;
import com.example.kaizenchat.model.ChatType;
import com.example.kaizenchat.model.DuoChat;
import com.example.kaizenchat.model.GroupChat;
import com.example.kaizenchat.repository.ChatRepository;
import com.example.kaizenchat.repository.GroupChatOptionsRepository;
import com.example.kaizenchat.repository.UserRepository;
import com.example.kaizenchat.service.ChatService;
import com.example.kaizenchat.service.UserService;
import com.example.kaizenchat.utils.AvatarUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static com.example.kaizenchat.model.ChatType.DUO;
import static com.example.kaizenchat.model.ChatType.GROUP;
import static com.example.kaizenchat.utils.MultipartFileUtils.getFileExtension;
import static java.lang.String.format;
import static java.util.Comparator.comparing;

@Slf4j
@Service
public class ChatServiceImpl implements ChatService {

    private final static String DEFAULT_CHAT_AVATAR_PATH = "src\\main\\resources\\images\\chat_default_av.txt";

    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final GroupChatOptionsRepository groupChatOptionsRepository;
    private final UserService userService;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public ChatServiceImpl(ChatRepository chatRepository, UserRepository userRepository,
                           GroupChatOptionsRepository groupChatOptionsRepository,
                           UserService userService, PasswordEncoder passwordEncoder) {
        this.chatRepository = chatRepository;
        this.userRepository = userRepository;
        this.groupChatOptionsRepository = groupChatOptionsRepository;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    public ChatEntity findChatById(Long id, ChatType type) throws ChatNotFoundException {
        Optional<ChatEntity> chatOpt = chatRepository.findById(id);
        if (chatOpt.isEmpty() || (chatOpt.get().isGroupChat() && type.equals(DUO))) {
            throw new ChatNotFoundException(format("%s-chat:%d was not found", type.name(), id));
        }
        return chatOpt.get();
    }

    @Override
    public void deleteGroupChatById(Long id) throws ChatNotFoundException {
        ChatEntity chat = findChatById(id, GROUP);
        chatRepository.deleteGroupChatUsers(id);
        groupChatOptionsRepository.deleteById(id);
        chatRepository.delete(chat);
    }

    @Override
    public void deleteDuoChatById(Long id) throws ChatNotFoundException {
        ChatEntity chat = findChatById(id, DUO);
        chatRepository.deleteDuoChatUsersById(id);
        chatRepository.delete(chat);
    }

    @Override
    public ChatEntity findChatByUsers(Long firstUserId, Long secondUserId) throws ChatNotFoundException {
        Long chatId = chatRepository.findDuoChatByUsers(firstUserId, secondUserId)
                .orElseThrow(() -> new ChatNotFoundException(
                        format("duo-chat between [%d, %d] was not found", firstUserId, secondUserId)
                ));
        return findChatById(chatId, DUO);
    }

    @Override
    public DuoChat createDuoChat(Long fromUserId, Long toUserId)
            throws UserNotFoundException, ChatAlreadyExistsException {

        log.info("IN ChatService -> createDuoChat(): between users id:{} <-> id:{}", fromUserId, toUserId);

        // throw ex if one of them does not exist
        userService.findUserById(fromUserId);
        UserEntity toUser = userService.findUserById(toUserId);

        Optional<Long> chatIdOpt = chatRepository.findDuoChatByUsers(fromUserId, toUserId);
        if (chatIdOpt.isPresent()) {
            throw new ChatAlreadyExistsException(format("Duo chat for users [%d:%d] already exists", fromUserId, toUserId));
        }

        ChatEntity chat = ChatEntity.builder()
                .creation(ZonedDateTime.now())
                .name("Duo")
                .build();

        chat = chatRepository.save(chat);

        // add two users
        chatRepository.addDuoChatUsers(chat.getId(), fromUserId, toUserId);
        chatRepository.addDuoChatUsers(chat.getId(), toUserId, fromUserId);

        return new DuoChat(chat.getId(), toUser.getNickname());
    }

    @Override
    public GroupChat createGroupChat(GroupChatCreationRequest request, Long userId)
            throws UserNotFoundException {

        log.info("IN ChatService -> createGroupChat(): user-id={}", userId);
        UserEntity creator = userService.findUserById(userId);

        ChatEntity chat = ChatEntity.builder()
                .creation(ZonedDateTime.now())
                .name(request.getName())
                .groupChatUsers(Set.of(creator))
                .build();

        chat = chatRepository.save(chat);
        chatRepository.addUserToGroupChat(chat.getId(), creator.getId(), true);

        var groupChatOptions = GroupChatOptionsEntity.builder()
                .membersCount(1)
                .membersLimit(5000)
                .isPrivate(request.isPrivacyMode())
                .password(null)
                .chatId(chat.getId())
                .avatar(DEFAULT_CHAT_AVATAR_PATH)
                .build();

        if (request.isPrivacyMode()) {
            groupChatOptions.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        groupChatOptionsRepository.save(groupChatOptions);

        return new GroupChat(chat.getId(), chat.getName(),
                groupChatOptions.getMembersCount(),
                groupChatOptions.getMembersLimit()
        );
    }

    @Override
    public boolean addUserToGroupChat(AddMemberToChatRequest request, Long userId)
            throws ChatNotFoundException, UserNotFoundException, UserViolationPermissionsException {

        log.info("IN ChatService -> addUserToGroupChat(): chat-id:{} user-id:{}", request.getChatId(), userId);

        ChatEntity chat = findChatById(request.getChatId(), GROUP);
        UserEntity user = userService.findUserById(userId);

        GroupChatOptionsEntity groupChatOptions = chat.getGroupChatOptions();

        // check password
        if (groupChatOptions.getIsPrivate() && !passwordEncoder.matches(request.getPassword(), groupChatOptions.getPassword())) {
            String message = format("Chat[id:%d] password is incorrect", chat.getId());
            log.error("IN ChatService -> addUserToGroupChat(): {}", message);
            throw new UserViolationPermissionsException(message);
        }

        // check members-limit
        Integer membersCount = groupChatOptions.getMembersCount();
        if (membersCount + 1 > groupChatOptions.getMembersLimit()) {
            String message = format("Chat with id:%d is full", chat.getId());
            log.error("IN ChatService -> addUserToGroupChat(): {}", message);
            throw new UserViolationPermissionsException(message);
        }

        if (isMemberInGroupChat(user, chat)) {
            log.info("IN ChatService -> addUserToGroupChat(): user-id:{} already in chat-id:{}", userId, request.getChatId());
            return false;
        }
        // save user to chat
        chatRepository.addUserToGroupChat(chat.getId(), userId, false);
        groupChatOptions.setMembersCount(membersCount + 1);
        groupChatOptionsRepository.save(groupChatOptions);
        return true;
    }

    @Override
    public List<Chat> getAllChats(Long userId, ChatType type) throws UserNotFoundException {
        UserEntity user = userService.findUserById(userId);
        log.info("IN ChatService -> getAllChats(): user-id={}", user.getId());

        Set<ChatEntity> chats = type.equals(GROUP) ? user.getGroupChats() : user.getDuoChats();
        return chats.stream()
                .map(chat -> getChatWithLastMessage(chat, userId))
                .filter(chat -> chat.getLastMessage() != null)
                .sorted(comparing(Chat::getLastMessageTime).reversed())
                .toList();
    }

    private Chat getChatWithLastMessage(ChatEntity chat, Long userId) {
        Set<MessageEntity> lastMessages = chat.getMessages();
        log.info("IN ChatService -> getChatWithLastMessage(): chat-id={} list size={}", chat.getId(), lastMessages.size());

        if (lastMessages.isEmpty()) {
            return Chat.builder().id(chat.getId()).build();
        }

        MessageEntity lastMessage = lastMessages.stream()
                .max(comparing(MessageEntity::getTime))
                .orElse(new MessageEntity());

        // userId is used only we retrieve duo-chats in order to determine
        // receiver to which user with userId writes to. In any other cases, userId can be null
        UserEntity user = chat.isGroupChat() ? lastMessage.getSender() :
                chat.getUsers().stream()
                        .filter(userEntity -> !Objects.equals(userEntity.getId(), userId))
                        .findAny()
                        .get();

        return Chat.builder()
                .id(chat.getId())
                .name(chat.isGroupChat() ? chat.getName() : null)
                .adminId(chat.isGroupChat() ? chatRepository.getAdminId(chat.getId()) : null)
                .userId(user.getId())
                .username(user.getNickname())
                .lastMessage(lastMessage.getBody())
                .lastMessageTime(lastMessage.getTime())
                .build();
    }

    @Override
    public boolean isMemberInGroupChat(UserEntity user, ChatEntity chat) {
        log.info("IN ChatService -> isMemberPresentInChat(): chat-id:{} user-id:{}", chat.getId(), user.getId());
        return user.getGroupChats().contains(chat);
    }

    @Override
    public boolean isUserAdminInGroupChat(UserEntity user, ChatEntity chat) {
        log.info("IN ChatService -> isUserAdminInGroupChat(): chat-id:{} user-id:{}", chat.getId(), user.getId());
        if (!isMemberInGroupChat(user, chat)) {
            return false;
        }
        return chatRepository.isUserAdminInGroupChat(chat.getId(), user.getId());
    }

    @Override
    public void kickFromGroupChat(UserEntity admin, ChatEntity chat, UserEntity user)
            throws UserViolationPermissionsException {

        log.info("IN ChatService -> kickFromGroupChat(): chat-id:{} user-id:{}", chat.getId(), user.getId());

        if (!isUserAdminInGroupChat(admin, chat)) {
            String message = format("user-id:%d is not admin in chat-id:%d", admin.getId(), chat.getId());
            log.error("IN ChatService -> kickFromGroupChat(): {}", message);
            throw new UserViolationPermissionsException(message);
        }

        // save changes
        deleteUser(chat, user);
    }

    @Override
    public void deleteUser(ChatEntity chat, UserEntity user) {
        log.info("IN ChatService -> deleteUser(): chat-id:{} user-id:{}", chat.getId(), user.getId());
        chat.getUsers().remove(user);
        var chatOptions = chat.getGroupChatOptions();
        chatOptions.setMembersCount(chatOptions.getMembersCount() - 1);
        user.getGroupChats().remove(chat);
        chatRepository.save(chat);
        userRepository.save(user);
        groupChatOptionsRepository.save(chatOptions);
    }

    @Override
    public void uploadAvatar(String encodedContent, Long chatId, Long userId)
            throws
            UserViolationPermissionsException, UserNotFoundException,
            ChatNotFoundException, AvatarNotExistsException {

        log.info("IN ChatService -> updateAvatar()");

        try {
            UserEntity user = userService.findUserById(userId);
            ChatEntity chat = findChatById(chatId, GROUP);

            if (!isUserAdminInGroupChat(user, chat)) {
                throw new UserViolationPermissionsException("only admin can change avatar");
            }

            String filename = String.format("chat-img-%d_%d.txt", chatId, Instant.now().toEpochMilli());
            Path destination = AvatarUtils.getImageDestination(filename);
            AvatarUtils.updateAvatar(destination, encodedContent);

            // save path to chat options
            GroupChatOptionsEntity chatOptions = chat.getGroupChatOptions();
            chatOptions.setAvatar(destination.toString());
            groupChatOptionsRepository.save(chatOptions);

        } catch (IOException e) {
            log.error("IN ChatService -> updateAvatar(): {}", e.getMessage());
            throw new AvatarNotExistsException("cannot save avatar", e);
        }
    }

    @Override
    public AvatarDTO downloadAvatar(Long chatId) throws AvatarNotExistsException, ChatNotFoundException {
        log.info("IN ChatService -> downloadAvatar()");
        ChatEntity chat = findChatById(chatId, GROUP);
        try {
            String avatarPath = chat.getGroupChatOptions().getAvatar();
            String encodedContent = AvatarUtils.downloadAvatar(Path.of(avatarPath));
            return new AvatarDTO(encodedContent);
        } catch (IOException e) {
            log.error("IN ChatService -> downloadAvatar(): {}", e.getMessage());
            throw new AvatarNotExistsException(format("avatar for chat:%d was not found", chatId), e);
        }
    }

}