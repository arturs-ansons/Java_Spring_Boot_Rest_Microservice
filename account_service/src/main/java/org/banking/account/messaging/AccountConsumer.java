package org.banking.account.messaging;

import org.banking.account.dto.UserRegistrationEvent;
import org.banking.account.entity.Account;
import org.banking.account.repository.AccountRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Random;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountConsumer {

    private final AccountRepository accountRepository;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "user.registration.account.queue")
    public void handleUserRegistration(String message) {
        try {
            UserRegistrationEvent event = objectMapper.readValue(message, UserRegistrationEvent.class);
            log.info("Received user registration event for userId: {}", event.getUserId());

            if (accountRepository.countByUserId(event.getUserId()) > 0) {
                log.info("Account already exists for userId: {}", event.getUserId());
                return;
            }

            // Create a default account for new users
            Account account = new Account();
            account.setUserId(event.getUserId());
            account.setAccountNumber(generateAccountNumber());
            account.setBalance(BigDecimal.ZERO);
            account.setStatus(Account.AccountStatus.ACTIVE);
            account.setType(Account.AccountType.TRADING);
            account.setBalance(BigDecimal.valueOf(3500));

            accountRepository.save(account);
            log.info("Created default checking account {} for userId: {}",
                    account.getAccountNumber(), event.getUserId());

        } catch (Exception e) {
            log.error("Error processing user registration event: {}", e.getMessage(), e);
        }
    }

    private String generateAccountNumber() {
        Random random = new Random();
        return String.format("ACC%010d", random.nextLong(1_000_000_000L, 10_000_000_000L));
    }
}