package org.banking.crypto.dto;

import lombok.Data;
import org.banking.crypto.entity.CryptoTransaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CryptoTransactionDTO {
    private Long id;
    private Long accountId;
    private String transactionType;
    private String cryptoCurrency;
    private BigDecimal cryptoAmount;
    private String fiatCurrency;
    private BigDecimal fiatAmount;
    private BigDecimal pricePerUnit;
    private String status;
    private BigDecimal cryptoBalanceBefore;
    private BigDecimal cryptoBalanceAfter;
    private BigDecimal fiatBalanceBefore;
    private BigDecimal fiatBalanceAfter;
    private String description;
    private LocalDateTime createdAt;

    public CryptoTransactionDTO(CryptoTransaction transaction) {
        this.id = transaction.getId();
        this.accountId = transaction.getAccount().getId();
        this.transactionType = transaction.getTransactionType().name();
        this.cryptoCurrency = transaction.getCryptoCurrency();
        this.cryptoAmount = transaction.getCryptoAmount();
        this.fiatCurrency = transaction.getFiatCurrency();
        this.fiatAmount = transaction.getFiatAmount();
        this.pricePerUnit = transaction.getPricePerUnit();
        this.status = transaction.getStatus().name();
        this.cryptoBalanceBefore = transaction.getCryptoBalanceBefore();
        this.cryptoBalanceAfter = transaction.getCryptoBalanceAfter();
        this.fiatBalanceBefore = transaction.getFiatBalanceBefore();
        this.fiatBalanceAfter = transaction.getFiatBalanceAfter();
        this.description = transaction.getDescription();
        this.createdAt = transaction.getCreatedAt();
    }
}