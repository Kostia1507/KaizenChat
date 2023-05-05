package com.example.kaizenchat.exception;

public class JWTInvalidException extends RuntimeException {
    public JWTInvalidException(String message) {
        super(message);
    }
}