package com.merging.chunks.exception;

public class AccountLockException extends RuntimeException{
    public AccountLockException(String message) {
        super(message);
    }
}
