package com.example.kaizenchat.exception;

public class UserViolationPermissionsException extends Exception{
    public UserViolationPermissionsException(){
    }

    public UserViolationPermissionsException(String message) {
        super(message);
    }
}
