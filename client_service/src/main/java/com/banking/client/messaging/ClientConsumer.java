package com.banking.client.messaging;

import com.banking.client.dto.UserRegistrationEvent;
import com.banking.client.entity.Client;
import com.banking.client.repository.ClientRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClientConsumer {

    private final ClientRepository clientRepository;

    @Transactional
    @RabbitListener(queues = "user.registration.client.queue")
    public void handleUserRegistration(UserRegistrationEvent event) {
        log.info("Received user registration event for userId: {}", event.getUserId());

        // Idempotent check
        if (clientRepository.existsByUserId(event.getUserId())) {
            log.info("Client already exists for userId: {}", event.getUserId());
            return;
        }

        Client client = new Client();
        client.setUserId(event.getUserId());
        clientRepository.save(client);

        log.info("Created new client profile for userId: {}", event.getUserId());
    }
}
