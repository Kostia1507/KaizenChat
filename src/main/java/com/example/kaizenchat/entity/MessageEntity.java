package com.example.kaizenchat.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;

@Entity
@Table(name = "message")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "chat_id")
    private ChatEntity chat;

    @JsonIgnoreProperties(value = {"avatar", "registration", "bio"})
    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity sender;

    private String body;

    private ZonedDateTime time;

    @Column(name = "is_pinned")
    private Boolean isPinned;

    private Integer likes;

}