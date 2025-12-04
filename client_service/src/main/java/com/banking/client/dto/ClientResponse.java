package com.banking.client.dto;

import com.banking.client.entity.Client;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ClientResponse {
    private Long id;
    private Long userId;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String address;
    private String dateOfBirth;
    private String identificationNumber;
    private Client.ClientStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ClientResponse fromEntity(Client client) {
        ClientResponse response = new ClientResponse();
        response.setId(client.getId());
        response.setUserId(client.getUserId());
        response.setFirstName(client.getFirstName());
        response.setLastName(client.getLastName());
        response.setPhoneNumber(client.getPhoneNumber());
        response.setAddress(client.getAddress());
        response.setDateOfBirth(client.getDateOfBirth());
        response.setIdentificationNumber(client.getIdentificationNumber());
        response.setStatus(client.getStatus());
        response.setCreatedAt(client.getCreatedAt());
        response.setUpdatedAt(client.getUpdatedAt());
        return response;
    }
}