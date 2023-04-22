package com.example.kaizenchat.model;

import org.springframework.http.MediaType;

public record Model(String path, MediaType contentType, byte[] bytes) {
}