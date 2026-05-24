package com.sejinzx.enrollmentSystem.error;

public record ErrorResponse(
        int status,
        String error
) {}