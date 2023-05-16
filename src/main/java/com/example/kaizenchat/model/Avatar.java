package com.example.kaizenchat.model;

import org.springframework.http.MediaType;

@Deprecated
public record Avatar(String path, MediaType contentType, byte[] bytes) {
}