package org.banking.crypto.dto;


import lombok.Data;
import org.banking.crypto.entity.CryptoTransaction;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Component
@Data
public class CryptoTransactionResponse {
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

    public static CryptoTransactionResponse toResponse(CryptoTransaction transaction) {

        CryptoTransactionResponse response = new CryptoTransactionResponse();

        response.setId(transaction.getId());
        response.setAccountId(transaction.getAccount().getId());
        response.setTransactionType(transaction.getTransactionType().name());
        response.setCryptoCurrency(transaction.getCryptoCurrency());
        response.setCryptoAmount(transaction.getCryptoAmount());
        response.setFiatCurrency(transaction.getFiatCurrency());
        response.setFiatAmount(transaction.getFiatAmount());
        response.setPricePerUnit(transaction.getPricePerUnit());
        response.setStatus(transaction.getStatus().name());
        response.setCryptoBalanceBefore(transaction.getCryptoBalanceBefore());
        response.setCryptoBalanceAfter(transaction.getCryptoBalanceAfter());
        response.setFiatBalanceBefore(transaction.getFiatBalanceBefore());
        response.setFiatBalanceAfter(transaction.getFiatBalanceAfter());
        response.setDescription(transaction.getDescription());
        response.setCreatedAt(transaction.getCreatedAt());

        return response;
    }
}