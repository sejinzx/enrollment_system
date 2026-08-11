package com.sejinzx.enrollmentSystem.error;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException e
    ) {
        ErrorCode code = e.getErrorCode();

        return createResponse(code);
    }

    private ResponseEntity<ErrorResponse> createResponse(ErrorCode code) {
        return ResponseEntity
                .status(code.getStatus())
                .body(new ErrorResponse(
                        code.getStatus().value(),
                        code.getError()
                ));
    }
}