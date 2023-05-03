package com.example.kaizenchat.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "group_chat_options")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupChatOptionsEntity {

    @Id
    @Column(name = "chat_id")
    private Long chatId;

    @Column(name = "is_private")
    private Boolean isPrivate;

    private String password;

    private String avatar;

    @Column(name = "members_limit")
    private Integer membersLimit;

    @Column(name = "members_count")
    private Integer membersCount;

}