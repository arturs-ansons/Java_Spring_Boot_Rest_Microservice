package com.banking.client.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserRegistrationEvent {
    private Long userId;
    private String username;
    private String email;
    private LocalDateTime registeredAt;
}