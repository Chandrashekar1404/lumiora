package com.lumiora.exception;

import com.lumiora.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(
                MethodArgumentNotValidException exception) {

                Map<String, String> errors = new LinkedHashMap<>();

                exception.getBindingResult()
                        .getFieldErrors()
                        .forEach(error ->
                                errors.put(error.getField(), error.getDefaultMessage())
                        );

                ApiResponse<Map<String, String>> response =
                        new ApiResponse<>(
                                false,
                                "Validation failed",
                                errors
                        );

                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(response);
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(
                IllegalArgumentException exception) {

                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.failure(exception.getMessage()));
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiResponse<Void>> handleGenericException(
                Exception exception) {

                return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.failure(
                                "An unexpected error occurred"
                        ));
                }


        @ExceptionHandler(UserNotFoundException.class)
        public ResponseEntity<ApiResponse<Void>> handleUserNotFoundException(
                UserNotFoundException exception) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.failure(exception.getMessage()));
        }
}