package com.kds.config.server.service.exception;

import lombok.Getter;

@Getter
public class ConfigServiceException extends RuntimeException {
    private final String status;
    private final String message;

    public ConfigServiceException(String status, String message) {
        super(message);
        this.status = status;
        this.message = message;
    }
} 