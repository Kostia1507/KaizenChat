package com.example.kaizenchat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class EditMessageRequest {
    @NotNull(message= "should be not null")
    private Long chatId;
    @NotBlank(message= "should be not blank")
    private String body;
}
