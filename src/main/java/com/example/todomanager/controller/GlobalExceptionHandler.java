package com.example.todomanager.controller;

import com.example.todomanager.service.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorDto> handleBusiness(BusinessException ex) {
        return ResponseEntity.badRequest().body(new ErrorDto("business_error", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> handleAll(Exception ex) {
        ex.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorDto("internal_error", ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage()));
    }

    public static class ErrorDto {
        private String code;
        private String message;

        public ErrorDto() {}
        public ErrorDto(String code, String message) { this.code = code; this.message = message; }

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}