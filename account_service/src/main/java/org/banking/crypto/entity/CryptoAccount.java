package org.banking.crypto.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.banking.account.entity.Account;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "crypto_accounts",
uniqueConstraints = @UniqueConstraint(columnNames = {"account_id", "crypto_currency"}))
@Data
public class CryptoAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "crypto_currency", nullable = false, length = 10)
    private String cryptoCurrency;

    @Column(name = "wallet_address")
    private String walletAddress;

    @Column(name = "balance", precision = 36, scale = 18, nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "available_balance", precision = 36, scale = 18, nullable = false)
    private BigDecimal availableBalance = BigDecimal.ZERO;

    @Column(name = "locked_balance", precision = 36, scale = 18, nullable = false)
    private BigDecimal lockedBalance = BigDecimal.ZERO;

    @Column(name = "average_buy_price", precision = 36, scale = 18)
    private BigDecimal averageBuyPrice;

    @Column(name = "total_invested", precision = 19, scale = 2)
    private BigDecimal totalInvested = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}