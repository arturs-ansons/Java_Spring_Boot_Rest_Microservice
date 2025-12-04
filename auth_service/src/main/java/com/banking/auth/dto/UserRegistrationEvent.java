package com.banking.auth.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class UserRegistrationEvent {
    private Long userId;
    private String username;
    private String email;
    private LocalDateTime registeredAt;

    public UserRegistrationEvent(Long userId, String username, String email) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.registeredAt = LocalDateTime.now();
    }
}