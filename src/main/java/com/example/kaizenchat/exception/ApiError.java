package com.example.kaizenchat.exception;

import java.time.ZonedDateTime;

public record ApiError(
        String path,
        String message,
        int statusCode,
        ZonedDateTime timestamp
) {}