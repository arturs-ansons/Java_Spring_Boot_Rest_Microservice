package org.banking.crypto.repository;

import org.banking.crypto.entity.CryptoTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CryptoTransactionRepository extends JpaRepository<CryptoTransaction, Long> {

    List<CryptoTransaction> findByAccountId(Long accountId);

    List<CryptoTransaction> findByAccountIdAndCryptoCurrency(Long accountId, String cryptoCurrency);

    Optional<CryptoTransaction> findByTransactionHash(String transactionHash);

    List<CryptoTransaction> findByStatus(CryptoTransaction.TransactionStatus status);

    @Query("SELECT ct FROM CryptoTransaction ct WHERE ct.account.id = :accountId AND ct.cryptoCurrency = :currency ORDER BY ct.createdAt DESC")
    List<CryptoTransaction> findRecentTransactions(@Param("accountId") Long accountId, @Param("currency") String currency, Pageable pageable);

    @Query("SELECT SUM(ct.cryptoAmount) FROM CryptoTransaction ct WHERE ct.account.id = :accountId AND ct.cryptoCurrency = :currency AND ct.transactionType = 'BUY' AND ct.status = 'COMPLETED'")
    BigDecimal getTotalBought(@Param("accountId") Long accountId, @Param("currency") String currency);

    @Query("SELECT SUM(ct.cryptoAmount) FROM CryptoTransaction ct WHERE ct.account.id = :accountId AND ct.cryptoCurrency = :currency AND ct.transactionType = 'SELL' AND ct.status = 'COMPLETED'")
    BigDecimal getTotalSold(@Param("accountId") Long accountId, @Param("currency") String currency);
}