package com.example.kaizenchat.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static java.time.ZonedDateTime.now;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Slf4j
@RestControllerAdvice
public class CustomExceptionHandler {

    @ResponseStatus(NOT_FOUND)
    @ExceptionHandler({
            UserNotFoundException.class,
            ChatNotFoundException.class,
            UserNotFoundInChatException.class,
            AvatarNotExistsException.class
    })
    public ApiError handleException(Exception e, HttpServletRequest request) {
        log.error("IN CustomExHandler [404] ->  handleEx(): path={} , {}", request.getRequestURI(), e.getMessage());
        return new ApiError(request.getRequestURI(), e.getMessage(), NOT_FOUND.value(), now());
    }

    @ResponseStatus(FORBIDDEN)
    @ExceptionHandler({
            BadCredentialsException.class,
            InvalidRequestDataException.class
    })
    public ApiError handleExceptionWithForbidden(Exception e, HttpServletRequest request) {
        log.error("IN CustomExHandler [403] ->  handleExWithForbidden(): path={} , {}", request.getRequestURI(), e.getMessage());
        return new ApiError(request.getRequestURI(), e.getMessage(), FORBIDDEN.value(), now());
    }

}