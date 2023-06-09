package com.example.kaizenchat.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
public class KickMemberRequest {
    @NotNull(message= "should be not null")
    private Long chatId;
    @NotNull(message= "should be not null")
    private Long userId;
}
