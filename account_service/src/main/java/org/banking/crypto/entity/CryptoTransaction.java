package org.banking.crypto.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.banking.account.entity.Account;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "crypto_transactions")
@Data
public class CryptoTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @Column(name = "crypto_currency", nullable = false, length = 10)
    private String cryptoCurrency;

    @Column(name = "crypto_amount", precision = 36, scale = 18, nullable = false)
    private BigDecimal cryptoAmount;

    @Column(name = "fiat_currency", nullable = false, length = 3)
    private String fiatCurrency = "USD";

    @Column(name = "fiat_amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal fiatAmount;

    @Column(name = "price_per_unit", precision = 36, scale = 18, nullable = false)
    private BigDecimal pricePerUnit;

    // Wallet addresses
    @Column(name = "from_address")
    private String fromAddress;

    @Column(name = "to_address")
    private String toAddress;

    @Column(name = "transaction_hash")
    private String transactionHash;

    // Network information
    @Column(name = "network")
    private String network;

    @Column(name = "network_fee", precision = 36, scale = 18)
    private BigDecimal networkFee;

    @Column(name = "network_fee_fiat", precision = 19, scale = 2)
    private BigDecimal networkFeeFiat;

    // Balance tracking
    @Column(name = "crypto_balance_before", precision = 36, scale = 18, nullable = false)
    private BigDecimal cryptoBalanceBefore;

    @Column(name = "crypto_balance_after", precision = 36, scale = 18, nullable = false)
    private BigDecimal cryptoBalanceAfter;

    @Column(name = "fiat_balance_before", precision = 19, scale = 2, nullable = false)
    private BigDecimal fiatBalanceBefore;

    @Column(name = "fiat_balance_after", precision = 19, scale = 2, nullable = false)
    private BigDecimal fiatBalanceAfter;

    // Status and confirmations
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus status = TransactionStatus.PENDING;

    @Column(name = "confirmation_count")
    private Integer confirmationCount = 0;

    @Column(name = "required_confirmations")
    private Integer requiredConfirmations = 1;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    // Additional fields
    @Column(name = "description")
    private String description;

    @Column(name = "reference", length = 100)
    private String reference;

    @Column(name = "external_id")
    private String externalId; // For exchange/third-party reference

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum TransactionType {
        BUY, SELL, DEPOSIT, WITHDRAWAL, TRANSFER, SWAP, STAKE, UNSTAKE, REWARD
    }

    public enum TransactionStatus {
        PENDING, CONFIRMING, COMPLETED, FAILED, CANCELLED, EXPIRED
    }
}