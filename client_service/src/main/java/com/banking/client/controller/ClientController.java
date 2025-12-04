package com.banking.client.controller;

import com.banking.client.dto.ClientRequest;
import com.banking.client.dto.ClientResponse;
import com.banking.client.service.ClientService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
@Slf4j
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @PostMapping
    public ResponseEntity<ClientResponse> createClient(
            @Valid @RequestBody ClientRequest request,
            HttpServletRequest httpRequest) {

        Long userId = (Long) httpRequest.getAttribute("userId");
        ClientResponse response = clientService.createClient(userId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-profile")
    public ResponseEntity<ClientResponse> getMyProfile(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        ClientResponse response = clientService.getClientByUserId(userId);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/{clientId}")
    public ResponseEntity<ClientResponse> getClient(@PathVariable Long clientId) {
        ClientResponse response = clientService.getClientById(clientId);
        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<ClientResponse> updateClient(
            @Valid @RequestBody ClientRequest request,
            HttpServletRequest httpRequest) {

        Long userId = (Long) httpRequest.getAttribute("userId");
        ClientResponse response = clientService.updateClient(userId, request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin/deactivate")
    public ResponseEntity<Void> deactivateClient(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        clientService.deactivateClient(userId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public ResponseEntity<List<ClientResponse>> getAllClients() {
        List<ClientResponse> clients = clientService.getAllClients();
        return ResponseEntity.ok(clients);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Client Service is healthy");
    }

    @GetMapping("/check")
    public ResponseEntity<Boolean> checkClientExists(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        boolean exists = clientService.clientExists(userId);
        return ResponseEntity.ok(exists);
    }
}