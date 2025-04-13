package com.kds.config.server.app.exception;

import lombok.Getter;

@Getter
public class ConfigAPIException extends RuntimeException {
    private final String status;
    private final String message;

    public ConfigAPIException(String status, String message) {
        super(message);
        this.status = status;
        this.message = message;
    }
} 