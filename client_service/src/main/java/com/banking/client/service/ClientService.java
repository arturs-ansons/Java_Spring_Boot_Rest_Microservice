package com.banking.client.service;

import com.banking.client.dto.ClientRequest;
import com.banking.client.dto.ClientResponse;
import com.banking.client.entity.Client;
import com.banking.client.exception.ResourceAlreadyExistsException;
import com.banking.client.exception.ResourceNotFoundException;
import com.banking.client.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;

    @Transactional
    public ClientResponse createClient(Long userId, ClientRequest request) {
        if (clientRepository.existsByUserId(userId)) {
            throw new ResourceAlreadyExistsException("Client profile already exists for this user");
        }

        if (request.getIdentificationNumber() != null &&
                clientRepository.existsByIdentificationNumber(request.getIdentificationNumber())) {
            throw new ResourceAlreadyExistsException("Identification number already exists");
        }

        if (request.getPhoneNumber() != null &&
                clientRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new ResourceAlreadyExistsException("Phone number already exists");
        }

        Client client = new Client();
        client.setUserId(userId);
        client.setFirstName(request.getFirstName());
        client.setLastName(request.getLastName());
        client.setPhoneNumber(request.getPhoneNumber());
        client.setAddress(request.getAddress());
        client.setDateOfBirth(request.getDateOfBirth());
        client.setIdentificationNumber(request.getIdentificationNumber());

        Client savedClient = clientRepository.save(client);
        log.info("Created client profile for userId: {}", userId);

        return ClientResponse.fromEntity(savedClient);
    }

    public ClientResponse getClientByUserId(Long userId) {
        Client client = clientRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Client profile not found for user ID: " + userId)
                );

        return ClientResponse.fromEntity(client);
    }

    public ClientResponse getClientById(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Client not found with ID: " + clientId)
                );

        return ClientResponse.fromEntity(client);
    }

    @Transactional
    public ClientResponse updateClient(Long userId, ClientRequest request) {
        Client client = clientRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Client profile not found for user ID: " + userId)
                );

        // Check unique constraints for updated fields
        if (request.getIdentificationNumber() != null &&
                !request.getIdentificationNumber().equals(client.getIdentificationNumber()) &&
                clientRepository.existsByIdentificationNumber(request.getIdentificationNumber())) {
            throw new ResourceAlreadyExistsException("Identification number already exists");
        }

        if (request.getPhoneNumber() != null &&
                !request.getPhoneNumber().equals(client.getPhoneNumber()) &&
                clientRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new ResourceAlreadyExistsException("Phone number already exists");
        }

        // Update client fields
        client.setFirstName(request.getFirstName());
        client.setLastName(request.getLastName());
        client.setPhoneNumber(request.getPhoneNumber());
        client.setAddress(request.getAddress());
        client.setDateOfBirth(request.getDateOfBirth());
        client.setIdentificationNumber(request.getIdentificationNumber());
        client.setUpdatedAt(java.time.LocalDateTime.now());

        Client updatedClient = clientRepository.save(client);
        log.info("Updated client profile for userId: {}", userId);

        return ClientResponse.fromEntity(updatedClient);
    }

    @Transactional
    public void deactivateClient(Long userId) {
        Client client = clientRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Client profile not found for user ID: " + userId)
                );

        client.setStatus(Client.ClientStatus.INACTIVE);
        client.setUpdatedAt(java.time.LocalDateTime.now());

        clientRepository.save(client);
        log.info("Deactivated client profile for userId: {}", userId);
    }

    public List<ClientResponse> getAllClients() {
        return clientRepository.findAll().stream()
                .map(ClientResponse::fromEntity)
                .toList();
    }

    public boolean clientExists(Long userId) {
        return clientRepository.existsByUserId(userId);
    }
}
