package com.example.demo3.exception;

import org.springframework.http.HttpStatus;

public class NoContentException extends BaseException {
    public NoContentException(String message) {
        super(message, HttpStatus.NO_CONTENT);
    }
}
