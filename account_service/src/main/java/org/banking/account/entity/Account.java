package org.banking.account.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.banking.crypto.entity.CryptoAccount;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "accounts")
@Data
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, unique = true)
    private String accountNumber;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status = AccountStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType type;

    @Column(name = "crypto_enabled")
    private Boolean cryptoEnabled = true;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CryptoAccount> cryptoAccounts = new ArrayList<>();

    public enum AccountStatus {
        ACTIVE, INACTIVE, SUSPENDED, CLOSED, FROZEN
    }

    public enum AccountType {
        CHECKING, SAVINGS, BUSINESS, LOAN, CREDIT, TRADING, CRYPTO
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}