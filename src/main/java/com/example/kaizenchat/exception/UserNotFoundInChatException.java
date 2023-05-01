package com.example.kaizenchat.exception;

public class UserNotFoundInChatException extends Exception{
    public UserNotFoundInChatException(String message) {
        super(message);
    }

    public UserNotFoundInChatException(){
    }
}
