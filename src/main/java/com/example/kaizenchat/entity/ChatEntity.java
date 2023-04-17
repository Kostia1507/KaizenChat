package com.example.kaizenchat.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;
import java.util.Set;

@Entity
@Table(name = "chat")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "creation_time")
    private ZonedDateTime creation;

    @JsonIgnore
    @ManyToMany(mappedBy = "groupChats")
    private Set<UserEntity> groupChatUsers;

    @JsonIgnore
    @ManyToMany(mappedBy = "duoChats")
    private Set<UserEntity> duoChatUsers;

    @ManyToOne
    @JoinColumn(name = "id", insertable = false, updatable = false)
    private GroupChatOptionsEntity groupChatOptions;

    @OneToMany(mappedBy = "chat")
    private Set<MessageEntity> messages;

    public Set<UserEntity> getUsers() {
        return groupChatUsers.isEmpty() ? duoChatUsers : groupChatUsers;
    }

}