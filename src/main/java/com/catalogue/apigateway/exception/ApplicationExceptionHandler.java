package com.catalogue.apigateway.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApplicationExceptionHandler {
    @ExceptionHandler(AppException.class)
    public ResponseEntity<?> handleAPIException(AppException appException){
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("error message", appException.getMessage());
        errorMap.put("status", HttpStatus.BAD_REQUEST.toString());
        return ResponseEntity.ok(errorMap);
    }
}
