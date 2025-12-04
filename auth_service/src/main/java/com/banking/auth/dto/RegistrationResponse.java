package com.banking.auth.dto;

public class RegistrationResponse {
    private final String message;

    public RegistrationResponse(String message) {
        this.message = message;
    }

    // getter
    public String getMessage() { return message; }
}
