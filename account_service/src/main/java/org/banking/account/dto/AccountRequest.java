package org.banking.account.dto;

import org.banking.account.entity.Account;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AccountRequest {

    @NotNull(message = "Account type is required")
    private Account.AccountType type;

    private String initialDeposit;
    private String account_number;
}