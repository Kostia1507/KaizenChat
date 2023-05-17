package com.example.kaizenchat.repository;

import com.example.kaizenchat.entity.ChatEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<ChatEntity, Long> {

    @Transactional
    @Modifying
    @Query(
            value = """
                    INSERT INTO duo_chat_members (chat_id, first_user_id, second_user_id)
                    VALUES (:chatId, :fromUserId, :toUserId)
                    """,
            nativeQuery = true
    )
    void addDuoChatUsers(Long chatId, Long fromUserId, Long toUserId);

    @Query(
            value = """
                    SELECT chat_id FROM duo_chat_members
                    WHERE first_user_id = :fromUserId AND second_user_id = :toUserId
                    LIMIT 1
                    """,
            nativeQuery = true
    )
    Optional<Long> findDuoChatByUsers(Long fromUserId, Long toUserId);

    @Transactional
    @Modifying
    @Query(
            value = "DELETE FROM duo_chat_members WHERE chat_id = :id",
            nativeQuery = true
    )
    void deleteDuoChatUsersById(Long id);

    @Transactional
    @Modifying
    @Query(
            value = "DELETE FROM group_chat_members WHERE chat_id = :chatId",
            nativeQuery = true
    )
    void deleteGroupChatUsers(Long chatId);

    @Transactional
    @Modifying
    @Query(
            value = """
                    INSERT INTO group_chat_members (chat_id, user_id, is_admin)
                    VALUES (:chatId, :userId, :isAdmin);
                    """,
            nativeQuery = true
    )
    void addUserToGroupChat(Long chatId, Long userId, boolean isAdmin);

    @Query(
            value = """
                    SELECT is_admin FROM group_chat_members
                    WHERE chat_id = :chatId AND user_id = :userId
                    """,
            nativeQuery = true
    )
    boolean isUserAdminInGroupChat(Long chatId, Long userId);

    @Query(
            value = """
                    SELECT user_id FROM group_chat_members
                    WHERE chat_id = :chatId AND is_admin = true
                    LIMIT 1
                    """,
            nativeQuery = true
    )
    Long getAdminId(Long chatId);

}
