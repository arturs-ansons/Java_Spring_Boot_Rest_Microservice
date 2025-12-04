package org.banking.crypto.repository;

import org.banking.crypto.entity.CryptoAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface CryptoAccountRepository extends JpaRepository<CryptoAccount, Long> {

    Optional<CryptoAccount> findByAccountIdAndCryptoCurrency(Long accountId, String cryptoCurrency);

    List<CryptoAccount> findByAccountId(Long accountId);

    Optional<CryptoAccount> findByWalletAddress(String walletAddress);

    @Query("SELECT ca FROM CryptoAccount ca WHERE ca.account.id = :accountId AND ca.balance > 0")
    List<CryptoAccount> findNonZeroBalanceAccounts(@Param("accountId") Long accountId);
}