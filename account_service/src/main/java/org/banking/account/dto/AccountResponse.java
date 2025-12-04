package org.banking.account.dto;

import org.banking.account.entity.Account;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AccountResponse {
    private Long id;
    private Long userId;
    private String accountNumber;
    private BigDecimal balance;
    private Boolean cryptoEnabled;
    private Account.AccountStatus status;
    private Account.AccountType type;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AccountResponse fromEntity(Account account) {
        AccountResponse response = new AccountResponse();
        response.setId(account.getId());
        response.setUserId(account.getUserId());
        response.setCryptoEnabled(account.getCryptoEnabled());
        response.setAccountNumber(account.getAccountNumber());
        response.setBalance(account.getBalance());
        response.setStatus(account.getStatus());
        response.setType(account.getType());
        response.setCreatedAt(account.getCreatedAt());
        response.setUpdatedAt(account.getUpdatedAt());
        return response;
    }
}