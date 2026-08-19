package com.merging.chunks.exception;

//catches every exception in the entir app and converts to consistent
//JSON API response, maps each exception to the correct HTTP STATUS

import com.merging.chunks.dto.apiresponse.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

//@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return ApiResponse.error(message, "VALIDATION_ERROR");
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponse<Void> handleAuth(AuthenticationException exception) {
        return ApiResponse.error(exception.getMessage(), "AUTHENTICATION FAILED");
    }

    @ExceptionHandler(AccountLockException.class)
    @ResponseStatus(HttpStatus.LOCKED)
    public ApiResponse<Void> handleLocked(AccountLockException exception) {
        return ApiResponse.error(exception.getMessage(), "ACCOUNT LOCKED");
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiResponse<Void> handleConflict(ConflictException exception) {
        return ApiResponse.error(exception.getMessage(), "USERNAME OR EMAIL TAKEN");
    }

    @ExceptionHandler(EmailNotVerifiedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResponse<Void> handleEmailNotVerified(EmailNotVerifiedException exception) {
        return ApiResponse.error(exception.getMessage(), "INVALID EMAIL");
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Void> handleResourceNotFound(ResourceNotFoundException exception) {
        return ApiResponse.error(exception.getMessage(), "USERNAME OR EMAIL NOT FOUND");
    }

    @ExceptionHandler(InvalidTokenException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponse<Void> handleToken(InvalidTokenException exception) {
        return ApiResponse.error(exception.getMessage(), "INVALID TOKEN");
    }

    @ExceptionHandler(InvalidOTPException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleOtp(InvalidOTPException exception) {
        return ApiResponse.error(exception.getMessage(), "INVALID OTP");
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResponse<Void> handleAccessDenied(AccessDeniedException exception) {
        return ApiResponse.error(exception.getMessage(), "ACCESS DENIED");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleGeneric(Exception exception) {
        log.error("UNEXPECTED ERROR : {}", exception.getMessage());
        return ApiResponse.error(exception.getMessage(), "INTERNAL ERROR");
    }
}
