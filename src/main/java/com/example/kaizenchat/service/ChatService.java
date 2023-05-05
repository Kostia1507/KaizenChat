package com.example.kaizenchat.service;

import com.example.kaizenchat.dto.AddMemberToChatRequest;
import com.example.kaizenchat.dto.Chat;
import com.example.kaizenchat.dto.DuoChatCreationRequest;
import com.example.kaizenchat.dto.GroupChatCreationRequest;
import com.example.kaizenchat.entity.ChatEntity;
import com.example.kaizenchat.entity.UserEntity;
import com.example.kaizenchat.exception.*;
import com.example.kaizenchat.model.Avatar;
import com.example.kaizenchat.model.ChatType;
import com.example.kaizenchat.model.DuoChat;
import com.example.kaizenchat.model.GroupChat;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ChatService {

    ChatEntity findChatById(Long id, ChatType type) throws ChatNotFoundException;

    ChatEntity findChatByUsers(Long firstUserId,Long secondUserId) throws ChatNotFoundException, UserNotFoundException;

    void deleteGroupChatById(Long id) throws ChatNotFoundException;

    void deleteDuoChatById(Long id) throws ChatNotFoundException;

    DuoChat createDuoChat(Long fromUserId, Long toUserId)
            throws UserNotFoundException, ChatAlreadyExistsException;

    GroupChat createGroupChat(GroupChatCreationRequest request, Long userId) throws UserNotFoundException;

    boolean addUserToGroupChat(AddMemberToChatRequest request, Long userId)
            throws ChatNotFoundException, UserNotFoundException, UserViolationPermissionsException;

    List<Chat> getAllChats(Long userId, ChatType type) throws UserNotFoundException;

    boolean isMemberInGroupChat(UserEntity user, ChatEntity chat);

    boolean isUserAdminInGroupChat(UserEntity user, ChatEntity chat);

    void kickFromGroupChat(UserEntity admin, ChatEntity chat, UserEntity user)
            throws UserViolationPermissionsException;

    void deleteUser(ChatEntity chat, UserEntity user);

    void uploadAvatar(MultipartFile avatar, Long chatId, Long userId)
            throws UserViolationPermissionsException, UserNotFoundException,
            ChatNotFoundException, AvatarNotExistsException;

    Avatar downloadAvatar(Long chatId) throws AvatarNotExistsException, ChatNotFoundException;

}