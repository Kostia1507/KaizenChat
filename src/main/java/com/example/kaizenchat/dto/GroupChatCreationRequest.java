package com.example.kaizenchat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
public class GroupChatCreationRequest {

    @NotBlank(message = "should be not blank")
    @Size(min = 3, message = "length should be 3 or longer")
    private String name;

    @NotNull(message = "should be not null")
    private boolean privacyMode;

    private String password;

}