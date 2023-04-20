package com.example.kaizenchat.service;

import com.example.kaizenchat.dto.AddMemberToChatRequest;
import com.example.kaizenchat.dto.DuoChatCreationRequest;
import com.example.kaizenchat.dto.GroupChatCreationRequest;
import com.example.kaizenchat.entity.ChatEntity;
import com.example.kaizenchat.entity.UserEntity;
import com.example.kaizenchat.exception.ChatAlreadyExistsException;
import com.example.kaizenchat.exception.ChatNotFoundException;
import com.example.kaizenchat.exception.UserNotFoundException;
import com.example.kaizenchat.exception.UserViolationPermissionsException;
import com.example.kaizenchat.model.ChatType;
import com.example.kaizenchat.model.DuoChat;
import com.example.kaizenchat.model.GroupChat;

public interface ChatService {

    ChatEntity findChatById(Long id, ChatType type) throws ChatNotFoundException;

    void deleteGroupChatById(Long id) throws ChatNotFoundException;

    void deleteDuoChatById(Long id) throws ChatNotFoundException;

    DuoChat createDuoChat(DuoChatCreationRequest request, Long fromUserId, Long toUserId)
            throws UserNotFoundException, ChatAlreadyExistsException;

    GroupChat createGroupChat(GroupChatCreationRequest request, Long userId) throws UserNotFoundException;

    void addUserToGroupChat(AddMemberToChatRequest request, Long userId)
            throws ChatNotFoundException, UserNotFoundException, UserViolationPermissionsException;

    boolean isMemberInGroupChat(UserEntity user, ChatEntity chat);

    boolean isUserAdminInGroupChat(UserEntity user, ChatEntity chat);

    void kickFromGroupChat(UserEntity admin, ChatEntity chat, UserEntity user)
            throws UserViolationPermissionsException;

}