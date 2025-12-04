package org.banking.crypto.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CryptoRequest {
    private String cryptoCurrency;
    private BigDecimal fiatAmount;
    private BigDecimal cryptoAmount;
    private String fiatCurrency;
}