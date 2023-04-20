package com.example.kaizenchat.exception;

public class ChatAlreadyExistsException extends Exception {
    public ChatAlreadyExistsException(String message) {
        super(message);
    }
}