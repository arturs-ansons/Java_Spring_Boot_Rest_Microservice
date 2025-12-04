package org.banking.crypto.dto;

import lombok.Data;
import org.banking.crypto.entity.CryptoAccount;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CryptoPortfolioDTO {
    private Long id;
    private Long accountId;
    private String cryptoCurrency;
    private BigDecimal availableBalance;
    private BigDecimal lockedBalance;
    private BigDecimal balance;
    private BigDecimal averageBuyPrice;
    private BigDecimal totalInvested;
    private String walletAddress;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    public CryptoPortfolioDTO(CryptoAccount entity) {
        this.id = entity.getId();
        this.accountId = entity.getId();
        this.cryptoCurrency = entity.getCryptoCurrency();
        this.availableBalance = entity.getAvailableBalance();
        this.lockedBalance = entity.getLockedBalance();
        this.balance = entity.getBalance();
        this.averageBuyPrice = entity.getAverageBuyPrice();
        this.totalInvested = entity.getTotalInvested();
        this.walletAddress = entity.getWalletAddress();
        this.createdAt = entity.getCreatedAt();
        this.updatedAt = entity.getUpdatedAt();
    }

}