package com.merging.chunks.exception;

public class InvalidOTPException extends RuntimeException{
    public InvalidOTPException(String message) {
        super(message);
    }
}
