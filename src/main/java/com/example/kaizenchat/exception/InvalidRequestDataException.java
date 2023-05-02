package com.example.kaizenchat.exception;

public class InvalidRequestDataException extends Exception {
    public InvalidRequestDataException() {
    }

    public InvalidRequestDataException(String message) {
        super(message);
    }
}
