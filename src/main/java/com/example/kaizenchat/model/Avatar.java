package com.example.kaizenchat.model;

import org.springframework.http.MediaType;

public record Avatar(String path, MediaType contentType, byte[] bytes) {
}